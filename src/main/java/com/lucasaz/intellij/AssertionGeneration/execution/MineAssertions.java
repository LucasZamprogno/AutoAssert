package com.lucasaz.intellij.AssertionGeneration.execution;

import com.lucasaz.intellij.AssertionGeneration.exceptions.PluginException;
import com.lucasaz.intellij.AssertionGeneration.model.AssertionGenerationResponse;
import com.lucasaz.intellij.AssertionGeneration.model.DynamicAnalysisResult;
import com.lucasaz.intellij.AssertionGeneration.model.assertion.*;
import com.lucasaz.intellij.AssertionGeneration.model.task.Task;
import com.lucasaz.intellij.AssertionGeneration.services.IsolatedAssertionGeneration;
import com.lucasaz.intellij.AssertionGeneration.util.Util;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.*;
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
				Repo repo = new Repo(repoUrl, repoPath);
				if (DYNAMIC_RUN) {
					mineRepo(repo);
				}
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
			bufferedWriter.write("INCLUSION,NULL,UNDEFINED,BOOL,TYPEOF,INSTANCEOF,NUMERIC,TRUTHINESS,ORIGINALASSN\n");
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

	private static void mineRepo(Repo repo) {
		for (TestFile testFile : repo.getFiles()) {
			for (Test test : testFile.getTests()) {
				for (List<Assertion> block : test.getAssertionBlocks()) {
					Assertion firstAssertion = block.get(0);
					int line = firstAssertion.getLine() - 1;
					Target assertingOn = firstAssertion.getLHS();
					String root = assertingOn.getText();
					File file = new File(testFile.getFilePath().toString());
					String fileName = file.getName();

					String testDirPath = "." + file.getParent().replace(repo.toString(), "");
					String testFileRelativePath = testDirPath.replace("./temp/", "");
					String repoName = repo.toString().replace("./temp/", "");
					Task task = new Task(repoName, testDirPath.replace(".", ""), fileName);
					String newAssertions;
					boolean error;
					boolean differentBetweenRuns = false;
					String errorMessage;
					try {
						generateCounter++;
						AssertionGenerationResponse response = IsolatedAssertionGeneration.generateAssertions(line, root, testFile.getSource(), task);
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
					if (!error) {
						saveResultToFile(repo.toString().replace("./temp/", ""), result);
					}
				}
			}
		}
	}

	private static void saveAssertionData(Collection<Assertion> assertions, BufferedWriter bufferedWriter) {
		for (Assertion assertion : assertions) {
			for (PropertyAccess propertyAccess : assertion.getPropertyAccesses()) {
				if (!propertyCounts.containsKey(propertyAccess.getName())) {
					propertyCounts.put(propertyAccess.getName(), 0);
				}
				propertyCounts.put(propertyAccess.getName(),
						propertyCounts.get(propertyAccess.getName()) + 1);
			}
			if (assertion.getPropertyAccesses().get(0) instanceof Call) {
				Call call = (Call) assertion.getPropertyAccesses().get(0);
				if (call.getArguments().size() > 0 && call.getName().equals("expect")) {
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
		row.append(",");
		row.append(assertion.toString());
		row.append("\n");
		try {
			bufferedWriter.write(row.toString());
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	private static void saveResultToFile(String repo, DynamicAnalysisResult result) {
		String withinRepo = result.getSourceFilePath().replace(".", "");
		String fileName = "./save/dynamic/" + repo + withinRepo + "-" + generateCounter;
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

	private static void addToSet(Collection<Assertion> assertions, Set<String> set) {
		for (Assertion assertion : assertions) {
			for (PropertyAccess propertyAccess : assertion.getPropertyAccesses()) {
				set.add(propertyAccess.getName());
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
//				"https://github.com/nock/nock"//,
//				"https://github.com/ConsenSys/truffle",
//				"https://github.com/trufflesuite/truffle", // [4]
////				"https://github.com/DevExpress/testcafe", // [5] Causes a crash?
//				"https://github.com/sahat/satellizer",
//				"https://github.com/jonobr1/two.js",
//				"https://github.com/vigetlabs/gulp-starter",
//				"https://github.com/ipfs/js-ipfs", // [3]
//				"https://github.com/theintern/intern",
//				"https://github.com/apiaryio/dredd" //,
//				"https://github.com/bitpay/copay",
//				"https://github.com/huytd/agar.io-clone",
//				"https://github.com/davidmerfield/Typeset" // [2]
//				"https://github.com/electrode-io/electrode",
//				"https://github.com/alibaba/uirecorder",
//				"https://github.com/carteb/carte-blanche",
//				"https://github.com/adleroliveira/dreamjs", // [6]
//				"https://github.com/microsoft/pxt",
//				"https://github.com/0xProject/0x-monorepo",
//				"https://github.com/samgozman/YoptaScript",
////				"https://github.com/open-wc/open-wc", // [1], Causes a crash?
//				"https://github.com/jaymedavis/hubble",
//				"https://github.com/smartcontractkit/chainlink",
//				"https://github.com/ruanyf/es-checker",
//				"https://github.com/dareid/chakram" // [7]
//				"https://github.com/omnisharp/generator-aspnet",
//				"https://github.com/firebase/bolt",
//				"https://github.com/xolvio/chimp",
//				"https://github.com/sourcegraph/javascript-typescript-langserver", // [8]
//				"https://github.com/dharmafly/noodle",
//				"https://github.com/dalekjs/dalek",
//				"https://github.com/racker/dreadnot",
//				"https://github.com/polymer/web-component-tester",
//				"https://github.com/Polymer/web-component-tester",
//				"https://github.com/Polymer/tools", // [9]
//				"https://github.com/mojotech/dill.js",
//				"https://github.com/mojotech/pioneer",
//				"https://github.com/TheBrainFamily/cypress-cucumber-preprocessor", // [10]
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
//				"https://github.com/philcockfield/ui-harness"
//				"https://github.com/RobinCK/typeorm-fixtures",
//				"https://github.com/distillpub/distill-template",
//				"https://github.com/Modular-Network/ethereum-libraries",
//				"https://github.com/exratione/lambda-complex", // [11]
//				"https://github.com/iden3/circom",
//				"https://github.com/rosshinkley/nightmare-examples",
//				"https://github.com/segmentio/nightmare.git", // [12]
//				"https://github.com/Koenkk/zigbee-shepherd-converters",
//				"https://github.com/sporto/planetproto",
//				"https://github.com/ether/ueberDB",
//				"https://github.com/lonelyplanet/backpack-ui", // [13]
//				"https://github.com/cardstack/cardstack",
//				"https://github.com/opengsn/gsn",
//				"https://github.com/rodowi/Paparazzo.js",
//				"https://github.com/syzer/JS-Spark",
//				"https://github.com/PersonifyJS/personify.js",
//				"https://github.com/elsehow/signal-protocol",
//				"https://github.com/bhushankumarl/amazon-mws",
//				"https://github.com/pega-digital/bolt",
//				"https://github.com/alanhoff/node-tar.gz",
//				"https://github.com/jsillitoe/react-currency-input", // [14]
//				"https://github.com/anticoders/gagarin",
//				"https://github.com/masumsoft/express-cassandra",
//				"https://github.com/rapid-sensemaking-framework/noflo-rsf", // [15]
//				"https://github.com/thetutlage/japa", // [16]
//				"https://github.com/trufflesuite/ganache-core", // [17]
//				"https://github.com/sebpiq/rhizome", // [18]
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
