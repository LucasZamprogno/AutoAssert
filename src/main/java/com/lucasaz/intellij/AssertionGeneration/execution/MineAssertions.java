package com.lucasaz.intellij.AssertionGeneration.execution;

import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.lucasaz.intellij.AssertionGeneration.exceptions.PluginException;
import com.lucasaz.intellij.AssertionGeneration.model.AssertionGenerationResponse;
import com.lucasaz.intellij.AssertionGeneration.model.DynamicAnalysisResult;
import com.lucasaz.intellij.AssertionGeneration.model.assertion.*;
import com.lucasaz.intellij.AssertionGeneration.model.task.Dredd;
import com.lucasaz.intellij.AssertionGeneration.model.task.Nock;
import com.lucasaz.intellij.AssertionGeneration.model.task.Typeset;
import com.lucasaz.intellij.AssertionGeneration.model.task.Task;
import com.lucasaz.intellij.AssertionGeneration.services.EqualitySpecifier;
import com.lucasaz.intellij.AssertionGeneration.services.IsolatedAssertionGeneration;
import com.lucasaz.intellij.AssertionGeneration.util.Util;
import com.lucasaz.intellij.AssertionGeneration.visitors.ProjectVisitor;
import com.lucasaz.intellij.AssertionGeneration.visitors.impl.TypeScriptVisitor;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MineAssertions {
	static boolean DYNAMIC_RUN = true;

	static long orphanAssertions = 0;
	static long numberOfProjects = 0;
	static long nonZeroTestRepoCount = 0;
	static long expectCount = 0;
	static long assertOnIdentifierCount = 0;
	static long assertOnExpression = 0;
	static long assertOnExpWithCall = 0;
	static long assertOnExpWithPropAccess = 0;
	static long expectedValueCount = 0;
	static long expectedValueIsIdentifier = 0;
	static long expectedValueIsLiteral = 0;
	static long expectedValueIsCall = 0;
	static long generateCounter = 0;
	static List<Integer> propertyAccessDepths = new ArrayList<>();
	static List<Integer> testAssertionCounts = new ArrayList<>();
	static List<Integer> expectKeywordCounts = new ArrayList<>();
	static List<Integer> repoTestCounts = new ArrayList<>();
	static Map<String, Integer> propertyCounts = new HashMap<>();
	static Map<String, Integer> propertyProjectCounts = new HashMap<>();

	public static void main(String[] args) {
		List<String> repoUrls = getRepos();

		for (String repoUrl : repoUrls) {
			try {
				System.out.println("Getting assertions for " + repoUrl);
				BufferedWriter bw = createEqualityAssertionsFile(repoUrl);
				Path repoPath = cloneRepo(repoUrl);
				Repo repo = mineRepo(repoPath, repoUrl);
				saveAssertions(repo, repoUrl, bw);
				deleteRepo(repoPath);
				closeEqualityAssertionsFile(bw);
			} catch (Exception exception) {
				System.out.println("Failed to get assertions for " +  repoUrl);
				exception.printStackTrace();
				// continue
			}
		}

		printDetails();
	}

	private static BufferedWriter createEqualityAssertionsFile(String repoUrl) {
		BufferedWriter bufferedWriter = null;
		try {
			File file = new File("./save/equality/" + repoUrl
					.replace("https://github.com/", "")
					.replaceAll("/", "-"));
			if(!file.exists()) {
				file.createNewFile();
			}
			FileWriter fileWriter = new FileWriter(file.getPath(), true);
			bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write("INCLUSION,NULL,UNDEFINED,BOOL,TYPEOF,INSTANCEOF,NUMERIC,TRUTHINESS,ORIGINALASSN");
		} catch(IOException e){
			e.printStackTrace();
		}
		return bufferedWriter;
	}

	private static void closeEqualityAssertionsFile(BufferedWriter bufferedWriter) {
		try {
			bufferedWriter.close();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	private static Path cloneRepo(String repoUrl) throws Exception {
		String cloneUrl = repoUrl + ".git";
		String cacheRepositoryPath = "./temp/" + repoUrl
				.replace("https://github.com/", "")
				.replace("/","-");
		Path repoPath = Paths.get(cacheRepositoryPath);
		File cloneDirectoryFile = repoPath.toFile();

		if (cloneDirectoryFile.exists()) {
			return repoPath;
		}

		try {
			Git.cloneRepository()
					.setURI(cloneUrl)
					.setDirectory(cloneDirectoryFile)
					.setCredentialsProvider(new UsernamePasswordCredentialsProvider("token", ""))
					.call()
					.getRepository()
					.close();
			return repoPath;
		} catch (GitAPIException gitAPIException) {
			System.out.println("Error cloning repo");
			throw new Exception();
		}
	}

	private static Repo mineRepo(Path repo, String name) throws Exception {
		try {
			List<Assertion> orphanAssertions = new ArrayList<>();
			List<Test> tests = new ArrayList<>();

			ProjectVisitor projectVisitor = new ProjectVisitor(null) {
				@Override
				protected boolean shouldVisitFile(Path filePath) {
					String filePathString = filePath.toString();
					boolean shouldVisit = super.shouldVisitFile(filePath) &&
							(filePathString.endsWith(".ts") || filePathString.endsWith(".js"))  &&
							(filePathString.contains("test") || filePathString.contains("spec"));
					if (shouldVisit) {
						System.out.println(filePathString);
					}
					return shouldVisit;
				}

				@Override
				protected boolean shouldVisitDirectory(Path dirPath) {
					return super.shouldVisitDirectory(dirPath) && !dirPath.toString().contains("node_modules") && !dirPath.toString().contains(".git");
				}

				@Override
				protected void visitFile(Path filePath) {
					List<Test> testsInThisFile = new ArrayList<>();
					try {
						TypeScriptVisitor sourceVisitor = new TypeScriptVisitor() {
							private V8Object sourceFile;

							@Override
							public void visit(String source) {
								sourceFile = getSource(source);
								visit(sourceFile);
							}

							@Override
							public void close() {
								sourceFile.release();
								super.close();
							}

							private void visitExpressionImpl(V8Object expressionStatement) {
								try {
									if (isTest(expressionStatement)) {
										Test test = getTest(expressionStatement, filePath.toString(), sourceFile);
										tests.add(test);
										testsInThisFile.add(test);
										return; // Don't continue with children
									} else if (isAssertion(expressionStatement)) {
										orphanAssertions.add(getAssertion(expressionStatement, filePath.toString(), sourceFile));
										return; // Don't continue with children
									}
								} catch (Exception e) {
									System.out.println(e.toString());
									// Do nothing
								}
								visitChildren(expressionStatement);
							}

							@Override
							protected void visitExpressionStatement(V8Object expressionStatement) {
								visitExpressionImpl(expressionStatement);
							}

							private String getText(V8Object node) {
								V8Array arguments = new V8Array(ts.getRuntime());
								String text = node.executeStringFunction("getText", arguments);
								arguments.release();
								return text;
							}

							private boolean isAssertion(V8Object expressionStatement) {
								String expression = getText(expressionStatement);
								if (expression.startsWith("expect") || expression.startsWith("should") || expression.startsWith("assert")) {
									// _might_ be an expect. check the first identifier to see if it's an expect
									V8Object currentObject = expressionStatement;
									while (currentObject.contains("expression")) {
										currentObject = currentObject.getObject("expression");
									}

									String identifier = currentObject
											.executeStringFunction("getText", new V8Array(ts.getRuntime()));
									return isKind(currentObject, "Identifier") &&
											(identifier.equals("expect") || identifier.equals("should") || identifier.equals("assert"));
								} else {
									return false;
								}
							}

							private boolean isTest(V8Object expressionStatement) {
								String expression = getText(expressionStatement);
								if (expression.startsWith("it")) {
									// _might_ be a test. check first that we have a call expression
									if (isKind(expressionStatement.getObject("expression"), "CallExpression")) {
										V8Object identifier = expressionStatement.getObject("expression").getObject("expression");
										return isKind(identifier, "Identifier") && identifier.executeJSFunction("getText").equals("it");
									} else {
										return false;
									}
								}
								return false;
							}

							private Assertion getAssertion(V8Object expressionStatement, String filePath, V8Object sourceFile) throws IOException {
								List<PropertyAccess> propertyAccesses = new ArrayList<>();
								Map<Target, V8Object> mapToV8Node = new HashMap<>();

								int start = expressionStatement.executeIntegerFunction("getStart", new V8Array(ts.getRuntime()));
								V8Object lineAndCharacter = sourceFile
										.executeObjectFunction("getLineAndCharacterOfPosition", new V8Array(ts.getRuntime()).push(start));
								int line = lineAndCharacter.getInteger("line");

								TypeScriptVisitor expressionVisitor = new TypeScriptVisitor() {
									@Override
									protected void visitCallExpression(V8Object callExpression) {
										try {
											List<Target> arguments = new ArrayList<>();

											V8Array v8ArgumentArray = callExpression.getArray("arguments");
											int numArguments = v8ArgumentArray.length();
											for (int i = 0; i < numArguments; i++) {
												V8Object v8Node = v8ArgumentArray.getObject(i);
												Target target = getTarget(v8Node);
												mapToV8Node.put(target, v8Node);
												arguments.add(target);
											}
											// if expression is an identifier, we must be done
											V8Object parentExpression = callExpression.getObject("expression");
											if (isKind(parentExpression, "Identifier")) {
												String identifier = getText(parentExpression);
												propertyAccesses.add(0, new Call(identifier, arguments));
											} else if (isKind(parentExpression, "PropertyAccessExpression")) {
												String propertyName = getText(parentExpression.getObject("name"));
												propertyAccesses.add(0, new Call(propertyName, arguments));
												visit(parentExpression.getObject("expression"));
											}
										} catch (IOException ioe) {
											System.out.println("Fatal error! Could not reinit typescript!");
										}
									}

									@Override
									protected void visitPropertyAccessExpression(V8Object propertyAccessExpression) {
										propertyAccesses.add(0, new PropertyAccess(getText(propertyAccessExpression.getObject("name"))));
										visit(propertyAccessExpression.getObject("expression"));
									}

									@Override
									protected void visitIdentifier(V8Object identifier) {
										propertyAccesses.add(0, new PropertyAccess(getText(identifier)));
									}
								};
								expressionVisitor.visit(expressionStatement);

								Assertion assertion = new Assertion(propertyAccesses, filePath, line);
								if (EqualitySpecifier.isInEqualityCategory(assertion)) {
									assertion = EqualitySpecifier.getEqualityDetails(assertion, mapToV8Node);
								}

								// this line must be after the last use of the map
								// or else it will release all the objects inside
								expressionVisitor.close();

								return assertion;
							}

							private Test getTest(V8Object expressionStatement, String filePath, V8Object sourceFile) throws IOException {
								List<Assertion> testAssertions = new ArrayList<>();
								int start = expressionStatement.executeIntegerFunction("getStart", new V8Array(ts.getRuntime()));
								V8Object lineAndCharacter = sourceFile
										.executeObjectFunction("getLineAndCharacterOfPosition", new V8Array(ts.getRuntime()).push(start));
								int line = lineAndCharacter.getInteger("line");
								TypeScriptVisitor testVisitor = new TypeScriptVisitor() {
									@Override
									protected void visitExpressionStatement(V8Object expressionStatement) {
										try {
											if (isAssertion(expressionStatement)) {
												testAssertions.add(getAssertion(expressionStatement, filePath, sourceFile));
											}
										} catch (Exception e) {
											System.out.println(e.toString());
											// Do nothing
										}
										visitChildren(expressionStatement);
									}
								};
								testVisitor.visit(expressionStatement);
								testVisitor.close();
								return new Test(testAssertions, filePath, line);
							}

							private Target getTarget(V8Object object) throws IOException {
								String text = getText(object);
								final boolean[] includesPropertyAccess = {false};
								final boolean[] includesCallExpression = {false};
								final boolean[] includesIdentifier = {false};
								final String[] root = {null};
								final int[] depth = {0};
								TypeScriptVisitor targetVisitor = new TypeScriptVisitor() {
									@Override
									protected void visitPropertyAccessExpression(V8Object propertyAccessExpression) {
										includesPropertyAccess[0] = true;
										depth[0]++;
										visit(propertyAccessExpression.getObject("expression"));
									}

									@Override
									protected void visitElementAccessExpression(V8Object elementAccessExpression) {
										includesPropertyAccess[0] = true;
										depth[0]++;
										visit(elementAccessExpression.getObject("expression"));
									}

									@Override
									protected void visitCallExpression(V8Object callExpression) {
										includesCallExpression[0] = true;
										depth[0]++;
										visit(callExpression.getObject("expression"));
									}

									@Override
									protected void visitIdentifier(V8Object identifier) {
										includesIdentifier[0] = true;
										root[0] = getText(identifier);
									}
								};
								boolean isIdentifier = isKind(object, "Identifier") || isKind(object, "ThisKeyword"); // this ????
								boolean isExpression = isExpression(object);
								boolean isLiteral = isLiteral(object);
								boolean isCall = isCall(object);
								targetVisitor.visit(object);
								targetVisitor.close();
								if (isExpression || isLiteral || includesCallExpression[0]) {
									depth[0] = -1;
								}
								return new Target(text, includesPropertyAccess[0], includesCallExpression[0], includesIdentifier[0], isExpression, isIdentifier, isLiteral, isCall, depth[0], root[0]);
							}

							private boolean isLiteral(V8Object literal) {
								return isKind(literal, "NullKeyword") ||
										isKind(literal, "UndefinedKeyword") ||
										isKind(literal, "TrueKeyword") ||
										isKind(literal, "FalseKeyword") ||
										isKind(literal, "NumericLiteral") ||
										isKind(literal, "BigIntLiteral") ||
										isKind(literal, "StringLiteral") ||
										isKind(literal, "JsxText") ||
										isKind(literal, "JsxTextAllWhiteSpaces") ||
										isKind(literal, "RegularExpressionLiteral") ||
										isKind(literal, "NoSubstitutionTemplateLiteral") ||
										isKind(literal, "TypeLiteral") || // Should this be here?
										isKind(literal, "ArrayLiteralExpression") ||
										isKind(literal, "ObjectLiteralExpression");
							}

							private boolean isExpression(V8Object expression) {
								return // isKind(expression, "ArrayLiteralExpression") || // ??
										// isKind(expression, "ObjectLiteralExpression") || // ??
										// isKind(expression, "PropertyAccessExpression") || // ??
										// isKind(expression, "ElementAccessExpression") || // ??
										// isKind(expression, "CallExpression") || // ??
										isKind(expression, "NewExpression") ||
										isKind(expression, "TaggedTemplateExpression") ||
										isKind(expression, "TypeAssertionExpression") ||
										isKind(expression, "ParenthesizedExpression") ||
										isKind(expression, "FunctionExpression") ||
										isKind(expression, "ArrowFunction") ||
										isKind(expression, "DeleteExpression") ||
										isKind(expression, "TypeOfExpression") ||
										isKind(expression, "VoidExpression") ||
										isKind(expression, "AwaitExpression") ||
										isKind(expression, "PrefixUnaryExpression") ||
										isKind(expression, "PostfixUnaryExpression") ||
										isKind(expression, "BinaryExpression") ||
										isKind(expression, "ConditionalExpression") ||
										isKind(expression, "TemplateExpression") ||
										isKind(expression, "YieldExpression") ||
										isKind(expression, "SpreadElement") ||
										isKind(expression, "ClassExpression") ||
										isKind(expression, "OmittedExpression") ||
										isKind(expression, "ExpressionWithTypeArguments") || // ??
										isKind(expression, "AsExpression") || // ??
										isKind(expression, "NonNullExpression") ||
										isKind(expression, "SyntheticExpression") ||
										isKind(expression, "ExpressionStatement");
							}

							private boolean isCall(V8Object call) {
								return isKind(call, "CallExpression");
							}
						};
						String source = new String(Files.readAllBytes(filePath));
						source = source.replaceAll("\n[\n\t\r ]*\n", "\n");
						sourceVisitor.visit(source);
						sourceVisitor.close();

						if (!DYNAMIC_RUN) {
							return;
						}

						for (Test test : testsInThisFile) {
							for (List<Assertion> block : test.getAssertionBlocks()) {
								Assertion firstAssertion = block.get(0);
								int line = firstAssertion.getLine() - 1;
								Target assertingOn = firstAssertion.getLHS();
								String root;
								if (!assertingOn.isIncludesCallExpression() && !assertingOn.isLiteral() && !assertingOn.isExpression()) {
									root = assertingOn.getRoot();
								} else {
									root = assertingOn.getText();
								}
								Task task;
								String testFileRelativePath;
								File file = new File(filePath.toString());
								String fileName = file.getName();

								String testDirPath = "." + file.getParent().replace(repo.toString(), "");

								if (repo.toString().contains("nock")) {
									testFileRelativePath = filePath.toString().replace(repo.toString() + "/tests/", "");
									task = new Nock(testDirPath, fileName);
								} else if (repo.toString().contains("Typeset")) {
									testFileRelativePath = filePath.toString().replace(repo.toString() + "/test/", "");
									task = new Typeset(testDirPath, fileName);
								} else if (repo.toString().contains("dredd")){
									if (fileName.endsWith(".ts")) {
										continue; // fml
									}
									testFileRelativePath = filePath.toString().replace(repo.toString() + "/packages/dredd-transactions/test/unit", "");
									task = new Dredd(testDirPath, fileName);
								} else {
									return;
								}
								String newAssertions;
								boolean error = false;
								boolean differentBetweenRuns = false;
								String errorMessage;
								try {
									generateCounter++;
									AssertionGenerationResponse response = IsolatedAssertionGeneration.generateAssertions(line, root, source, task);
									newAssertions = response.getGeneratedAssertions();
									differentBetweenRuns = response.isDifferentBetweenRuns();
									error = response.isFailed();
									errorMessage = response.getReason();
								} catch (PluginException pluginException) {
									newAssertions = "";
									error = true;
									errorMessage = pluginException.getMessage();
								}
								DynamicAnalysisResult result = new DynamicAnalysisResult(
										block,
										testFileRelativePath,
										differentBetweenRuns,
										error,
										newAssertions,
										errorMessage
								);
								saveResultToFile(repo.toString(), result);
							}
						}
					} catch (IOException ioException) {
						System.out.println("Error visiting file's source.");
						ioException.printStackTrace();
					}
				}
			};
			projectVisitor.visit(repo);
			projectVisitor.close();
			return new Repo(name, orphanAssertions, tests);
		} catch (Exception exception) {
			System.out.println("Failed to mine assertions");
			throw new Exception();
		}
	}

	private static void saveAssertionData(List<Assertion> assertions, BufferedWriter bufferedWriter) {
		for (Assertion assertion : assertions) {
			for (PropertyAccess propertyAccess : assertion.getPropertyAccesses()) {
				if (!propertyCounts.containsKey(propertyAccess.getText())) {
					propertyCounts.put(propertyAccess.getText(), 0);
				}
				propertyCounts.put(propertyAccess.getText(),
						propertyCounts.get(propertyAccess.getText()) + 1);
			}
			if (assertion.getPropertyAccesses().get(0) instanceof Call) {
				Call call = (Call) assertion.getPropertyAccesses().get(0);
				if (call.getArguments().size() > 0 && call.getText().equals("expect")) {
					expectCount++;
					expectKeywordCounts.add(assertion.getPropertyAccesses().size());
					Target assertingOn = call.getArguments().get(0);
					if (assertingOn.isIdentifier()) {
						assertOnIdentifierCount++;
					}
					if (assertingOn.isExpression()) {
						assertOnExpression++;
					}
					if (assertingOn.isIncludesCallExpression()) {
						assertOnExpWithCall++;
					}
					if (assertingOn.isIncludesPropertyAccess()) {
						assertOnExpWithPropAccess++;
					}
					if (assertingOn.getDepth() >= 0) {
						propertyAccessDepths.add(assertingOn.getDepth());
					}

					for (int i = 1; i < assertion.getPropertyAccesses().size(); i = i + 1) {
						PropertyAccess propertyAccess = assertion.getPropertyAccesses().get(i);
						if (propertyAccess instanceof Call) {
							Call rhsCall = (Call) propertyAccess;
							for (Target argument : rhsCall.getArguments()) {
								expectedValueCount++;
								if (argument.isIdentifier()) {
									expectedValueIsIdentifier++;
								}
								if (argument.isLiteral()) {
									expectedValueIsLiteral++;
								}
								if (argument.isCall()) {
									expectedValueIsCall++;
								}
							}
						}
					}
				}
			}
			if (assertion instanceof EqualityAssertion) {
				appendToEqualityFile((EqualityAssertion) assertion, bufferedWriter);
			}
		}
	}

	private static void appendToEqualityFile(EqualityAssertion assertion, BufferedWriter bufferedWriter) {
		StringBuilder row = new StringBuilder();
		row.append(assertion.isEqInclusion());
		row.append(",");
		row.append(assertion.isEqNull());
		row.append(",");
		row.append(assertion.isEqUndefined());
		row.append(",");
		row.append(assertion.isEqBoolean());
		row.append(",");
		row.append(assertion.isEqTypeof());
		row.append(",");
		row.append(assertion.isEqInstanceOf());
		row.append(",");
		row.append(assertion.isEqNumeric());
		row.append(",");
		row.append(assertion.isEqTruthiness());
		row.append(",");
		row.append(assertion.isEqLength());
		try {
			bufferedWriter.write(row.toString());
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	private static void saveResultToFile(String repo, DynamicAnalysisResult result) {
		String fileName = "./save/dynamic/" + repo + "-" + result.getSourceFilePath() + "-" + generateCounter;
		try {
			File file = new File(fileName);
			Util.ensureDir(file.getParent());
			file.createNewFile();
			PrintWriter printWriter = new PrintWriter(fileName);
			printWriter.print(result.toString());
			printWriter.close();
		} catch (Exception exception) {
			System.err.println("a");
		}
	}

	private static void saveAssertions(Repo repo, String repoUrl, BufferedWriter bufferedWriter) throws Exception {
//		System.out.println("saving assertions");
//		String fileName = "./save/meta/" + repoUrl
//				.replace("https://github.com/", "")
//				.replace("/","-");
//		try {
//			File file = new File(fileName);
//			file.createNewFile();
//			PrintWriter printWriter = new PrintWriter(fileName);
//			printWriter.print(assertions.toString()); // TODO to csv?
//			for (Assertion assertion : assertions) {
//				Target target = assertion.getTarget();
//				if (target != null) {
//					printWriter.print(target.isIncludesPropertyAccess() + ",");
//					printWriter.print(target.isIncludesCallExpression() + ",");
//					printWriter.print(target.isIncludesIdentifier() + ",");
//					printWriter.print(target.isExpression() + ",\n");
//				}
//			}
//			printWriter.close();
//		} catch (Exception exception) {
//			System.out.println("Error saving assertions");
//			exception.printStackTrace();
//			throw new Exception();
//		}

		Set<String> projectProperties = new HashSet<>();

		repoTestCounts.add(repo.getTests().size());
		if (repo.getTests().size() > 0) {
			nonZeroTestRepoCount++;
		}

		numberOfProjects += 1;
		orphanAssertions += repo.getOrphanAssertions().size();
		saveAssertionData(repo.getOrphanAssertions(), bufferedWriter);
		addToSet(repo.getOrphanAssertions(), projectProperties);
		for (Test test : repo.getTests()) {
			testAssertionCounts.add(test.getAssertions().size());
			saveAssertionData(test.getAssertions(), bufferedWriter);
			addToSet(test.getAssertions(), projectProperties);
		}
		for (String property : projectProperties) {
			if (!propertyProjectCounts.containsKey(property)) {
				propertyProjectCounts.put(property, 0);
			}
			propertyProjectCounts.put(property, propertyProjectCounts.get(property) + 1);
		}
	}

	private static void addToSet(List<Assertion> assertions, Set<String> set) {
		for (Assertion assertion : assertions) {
			for (PropertyAccess propertyAccess : assertion.getPropertyAccesses()) {
				set.add(propertyAccess.getText());
			}
		}
	}

	private static void printDetails() {
		System.out.println("NUMBER OF ASSERTIONS");
		long sumOfAssertionsInTests = 0;
		for (Integer integer : testAssertionCounts) {
			sumOfAssertionsInTests += integer;
		}
		System.out.println(orphanAssertions + sumOfAssertionsInTests);
		System.out.println();

		System.out.println("NUMBER OF TESTS");
		System.out.println(testAssertionCounts.size());
		System.out.println();

		System.out.println("NUMBER OF PROJECTS");
		System.out.println(numberOfProjects);
		System.out.println();

		System.out.println("NUMBER OF PROJECTS WITH TESTS");
		System.out.println(nonZeroTestRepoCount);
		System.out.println();

		System.out.println("PROPERTY,COUNT,PROJECTCOUNT");
		for (String key : propertyCounts.keySet()) {
			System.out.println(key + "," + propertyCounts.get(key) + ", " + propertyProjectCounts.get(key));
		}
		System.out.println();

		System.out.println("MEAN ASSNS PER TEST");
		System.out.println((double) sumOfAssertionsInTests / (double) testAssertionCounts.size());
		System.out.println("MEDIAN ASSNS PER TEST");
		Collections.sort(testAssertionCounts);
		System.out.println(testAssertionCounts.get(testAssertionCounts.size() / 2));
		System.out.println();

		System.out.println("MEAN TESTS PER REPO");
		long testSum = 0;
		for (Integer integer : repoTestCounts) {
			testSum += integer;
		}
		System.out.println((double) testSum / (double) repoTestCounts.size());
		System.out.println("MEDIAN TESTS PER REPO");
		Collections.sort(repoTestCounts);
		System.out.println(repoTestCounts.get(repoTestCounts.size() / 2));
		System.out.println();

		System.out.println("WARNING NUMBERS BELOW ARE FUZZY. ONLY COUNTING EXPECTS");
		System.out.println("EXPECTS:" + expectCount);
		System.out.println("ASSERT ON IDENTIFIER:" + assertOnIdentifierCount);
		System.out.println("ASSERT ON EXPRESSION:" + assertOnExpression);
		System.out.println("TARGET CONTAINS CALL:" + assertOnExpWithCall);
		System.out.println("TARGET CONTAINS PROPERTY ACCESS:" + assertOnExpWithPropAccess);
		System.out.println("VALUES EXPECTED COUNT:" + expectedValueCount);
		System.out.println("EXPECTED VALUE WAS IDENTIFIER:" + expectedValueIsIdentifier);
		System.out.println("EXPECTED VALUE WAS LITERAL:" + expectedValueIsLiteral);
		System.out.println("EXPECTED VALUE WAS CALL:" + expectedValueIsCall);
		System.out.println();

		System.out.println("SPITTING OUT REMAINING RAW DATA");

		System.out.println("Assertions per test");
		System.out.println(testAssertionCounts.toString());
		System.out.println();

		System.out.println("Target property access depth");
		System.out.println(propertyAccessDepths.toString());
		System.out.println();

		System.out.println("Number of keywords in expect assertions");
		System.out.println(expectKeywordCounts.toString());
		System.out.println();
	}

	private static void deleteRepo(Path repo) throws Exception {
		try {
			File dir = repo.toFile();
			if (dir.exists()) {
				FileUtils.deleteDirectory(dir);
			}
		} catch (IOException ioException) {
			System.out.println("Error in directory deletion");
			throw new Exception();
		}
	}

	private static List<String> getRepos() {
		return Arrays.asList(
//				"https://github.com/npm/npm",
//				"https://github.com/palantir/blueprint",
//				"https://github.com/nock/nock",
//				"https://github.com/ConsenSys/truffle",
////				"https://github.com/DevExpress/testcafe", // Causes a crash?
//				"https://github.com/sahat/satellizer",
//				"https://github.com/jonobr1/two.js",
//				"https://github.com/vigetlabs/gulp-starter",
//				"https://github.com/ipfs/js-ipfs",
//				"https://github.com/theintern/intern",
				"https://github.com/apiaryio/dredd" //,
//				"https://github.com/bitpay/copay",
//				"https://github.com/huytd/agar.io-clone",
//				"https://github.com/davidmerfield/Typeset",
//				"https://github.com/electrode-io/electrode",
//				"https://github.com/alibaba/uirecorder",
//				"https://github.com/carteb/carte-blanche",
//				"https://github.com/adleroliveira/dreamjs",
//				"https://github.com/microsoft/pxt",
//				"https://github.com/0xProject/0x-monorepo",
//				"https://github.com/samgozman/YoptaScript",
////				"https://github.com/open-wc/open-wc", // Causes a crash?
//				"https://github.com/jaymedavis/hubble",
//				"https://github.com/smartcontractkit/chainlink",
//				"https://github.com/ruanyf/es-checker",
//				"https://github.com/dareid/chakram",
//				"https://github.com/omnisharp/generator-aspnet",
//				"https://github.com/firebase/bolt",
//				"https://github.com/xolvio/chimp",
//				"https://github.com/sourcegraph/javascript-typescript-langserver",
//				"https://github.com/dharmafly/noodle",
//				"https://github.com/dalekjs/dalek",
//				"https://github.com/racker/dreadnot",
//				"https://github.com/polymer/web-component-tester",
//				"https://github.com/Polymer/web-component-tester",
//				"https://github.com/mojotech/dill.js",
//				"https://github.com/mojotech/pioneer",
//				"https://github.com/TheBrainFamily/cypress-cucumber-preprocessor",
//				"https://github.com/taskcluster/neo",
//				"https://github.com/mozilla/neo",
//				"https://github.com/lprhodes/homebridge-broadlink-rm",
//				"https://github.com/Brainfock/Brainfock",
//				"https://github.com/bdefore/redux-universal-starter",
//				"https://github.com/bdefore/redux-universal-renderer",
//				"https://github.com/facebookincubator/exerslide",
//				"https://github.com/Poincare/apollo-query-whitelisting",
//				"https://github.com/webdriverio/cucumber-boilerplate",
//				"https://github.com/codewars/codewars-runner",
//				"https://github.com/Microsoft/botbuilder-tools",
//				"https://github.com/nomiclabs/buidler",
//				"https://github.com/instructure/instructure-ui",
//				"https://github.com/mr-doc/mr-doc",
//				"https://github.com/diogomoretti/react-snakke",
//				"https://github.com/pemrouz/popper",
//				"https://github.com/akserg/ng2-toasty",
//				"https://github.com/philcockfield/ui-harness",
//				"https://github.com/RobinCK/typeorm-fixtures",
//				"https://github.com/distillpub/distill-template",
//				"https://github.com/Modular-Network/ethereum-libraries",
//				"https://github.com/exratione/lambda-complex",
//				"https://github.com/iden3/circom",
//				"https://github.com/rosshinkley/nightmare-examples",
//				"https://github.com/Koenkk/zigbee-shepherd-converters",
//				"https://github.com/sporto/planetproto",
//				"https://github.com/ether/ueberDB",
//				"https://github.com/lonelyplanet/backpack-ui",
//				"https://github.com/cardstack/cardstack",
//				"https://github.com/opengsn/gsn",
//				"https://github.com/rodowi/Paparazzo.js",
//				"https://github.com/syzer/JS-Spark",
//				"https://github.com/PersonifyJS/personify.js",
//				"https://github.com/elsehow/signal-protocol",
//				"https://github.com/bhushankumarl/amazon-mws",
//				"https://github.com/pega-digital/bolt",
//				"https://github.com/alanhoff/node-tar.gz",
//				"https://github.com/jsillitoe/react-currency-input",
//				"https://github.com/anticoders/gagarin",
//				"https://github.com/masumsoft/express-cassandra",
//				"https://github.com/rapid-sensemaking-framework/noflo-rsf",
//				"https://github.com/thetutlage/japa",
//				"https://github.com/trufflesuite/ganache-core",
//				"https://github.com/sebpiq/rhizome",
//				"https://github.com/nordcloud/serverless-mocha-plugin",
//				"https://github.com/chishui/JSSoup",
//				"https://github.com/kobigurk/semaphore",
//				"https://github.com/mikeseven/node-opencl",
//				"https://github.com/import-io/s3-deploy",
//				"https://github.com/accordproject/cicero",
//				"https://github.com/dowjones/fiveby",
//				"https://github.com/raphaklaus/fontwr",
//				"https://github.com/electrode-io/electrode-archetype-react-app",
//				"https://github.com/pevers/images-scraper",
//				"https://github.com/jrm2k6/dynamic-json-resume",
//				"https://github.com/BigstickCarpet/postman-bdd",
//				"https://github.com/maranran/eslint-plugin-vue-a11y",
//				"https://github.com/fex-team/node-ral",
//				"https://github.com/OpenZeppelin/openzeppelin-test-helpers",
//				"https://github.com/cavarzan/generator-android-hipster",
//				"https://github.com/excellalabs/js-best-practices-workshopper",
//				"https://github.com/sidneys/electron-titlebar-windows",
//				"https://github.com/veer66/wordcut",
//				"https://github.com/callumlocke/esbox",
//				"https://github.com/RxJSInAction/rxjs-in-action",
//				"https://github.com/nordcloud/serverless-kms-secrets",
//				"https://github.com/codeforequity-at/testmybot",
//				"https://github.com/xetorthio/shmock",
//				"https://github.com/john-doherty/selenium-cucumber-js"
		);
	}
}
