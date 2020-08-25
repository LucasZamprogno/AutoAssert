package com.lucasaz.intellij.AssertionGeneration.util;

import com.intellij.openapi.project.Project;
import com.lucasaz.intellij.AssertionGeneration.exceptions.PluginException;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Util {
    public static String PREFIX = "zzz-";
    public static String OUTFILE = ".testOutput";

    public static final String volumeDir = "volumes";
    public static final String logDir = "logs";
    public static final String projectDir = "C:/Users/Braxton/Desktop/AssertionGeneration-Plugin/";
    public static final String dev = Util.projectDir;
    public static final String prod = "/var/opt/classy/assertiongeneration/";
    public static final String hostFSDir = Util.dev;
    public static final String hostFSVolumeDir = Util.hostFSDir + Util.volumeDir;
    public static final String hostFSLogDir = Util.hostFSDir + Util.logDir;

    public static void writeFile(String path, String content) throws PluginException {
        try {
            FileWriter fw = new FileWriter(path);
            fw.write(content);
            fw.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            throw new PluginException("Write failed", e);
        }
    }

    public static String spliceInto(String base, String toInsert, int index) throws PluginException {
        List<String> lines = Util.toLines(base);
        if (lines.size() < index + 1) {
            throw new PluginException("Insertion line index somehow greater than file length");
        }
        List<String> start = lines.subList(0, index + 1);
        List<String> end = lines.subList(index + 1, lines.size());

        String endFile = Util.createString(start);
        endFile += toInsert;
        endFile += Util.createString(end);

        return endFile;
    }

    // Modified from https://stackoverflow.com/questions/2509170/is-there-an-easy-way-to-concatenate-several-lines-of-text-into-a-string-without
    public static String createString(List<String> lines) {
        String lsp = "\n";
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line).append(lsp);
        }
        return sb.toString();
    }

    public static void ensureDir(String path) {
        new File(path).mkdirs();
    }

    public static void ensureDeleted(File file){
        if (!file.exists()) {
            return;
        }
        for (File subFile : file.listFiles()) {
            if(subFile.isDirectory()) {
                ensureDeleted(subFile);
            } else {
                subFile.delete();
            }
        }
        file.delete();
    }

    public static String pathToFileContent(Path filepath) throws IOException {
        return new String(Files.readAllBytes(filepath), StandardCharsets.UTF_8);
    }

    public static String joinStringPaths(String p1, String p2) {
        return Paths.get(p1, p2).toString();
    }

    public static String getWhitespace(String line) {
        StringBuilder out = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (Character.isWhitespace(c)) {
                out.append(c);
            } else {
                break;
            }
        }
        return out.toString();
    }

    public static List<String> toLines(String in) {
        return new ArrayList<String>(Arrays.asList(in.split("[\n]")));
    }

    public static String findNearestTsconfig(String testFile, Project project) {
        return Util.findNearestTsconfigRec(Paths.get(testFile).getParent(), Paths.get(project.getBasePath()));
    }

    @Nullable
    private static String findNearestTsconfigRec(Path currentDir, Path projectRoot) {
        Path target = Paths.get(currentDir.toString(), "tsconfig.json");
        if (Files.exists(target)) {
            return target.toString();
        }
        if (currentDir.equals(projectRoot)) {
            return null;
        }
        return findNearestTsconfigRec(currentDir.getParent(), projectRoot);
    }

    public static List<String> findAllTsconfigInProject(Project project) {
        // https://stackoverflow.com/questions/40540915/how-to-find-a-file-recursively-in-java/40541501
        try {
            List<String> list = new ArrayList<>();
            Files.walk(Paths.get(project.getBasePath()))
                .filter(Files::isRegularFile)
                .forEach((f) -> {
                    String file = f.toString();
                    if (file.endsWith("tsconfig.json") && !file.contains("node_modules")) {
                        // TODO better way to check for node_modules
                        list.add(file);
                    }
                });
            return list;
        } catch (IOException | NullPointerException err) {
            return new ArrayList<>();
        }
    }

    public static String makeBackgroundFilename(String original) {
        Path originalAsPath = Paths.get(original);
        String newFilename = Util.PREFIX + originalAsPath.getFileName();
        return Paths.get(originalAsPath.getParent().toString(), newFilename).toString();
    }

    public static void cleanup(String[] filepaths) {
        for (String path : filepaths) {
            new File(path).delete();
            if (path.endsWith(".ts")) {
                String noExtension = path.substring(0, path.length() - 3);
                new File(noExtension + ".js").delete();
                new File(noExtension + ".js.map").delete();
            }
        }
    }
}
