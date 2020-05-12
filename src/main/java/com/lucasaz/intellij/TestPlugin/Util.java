package com.lucasaz.intellij.TestPlugin;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Util {
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
        String lsp = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line).append(lsp);
        }
        return sb.toString();
    }

    public static String pathToFileContent(Path filepath) throws IOException {
        return new String(Files.readAllBytes(filepath), StandardCharsets.UTF_8);
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
        return new ArrayList<String>(Arrays.asList(in.split(System.getProperty("line.separator"))));
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

    // Current unused and untested, for menu dropdown
    public static List<String> findAllParentTsconfigs(String testFile, Project project) {
        List<String> startList = new ArrayList<>();
        return Util.findAllParentTsconfigsRec(Paths.get(testFile).getParent(), Paths.get(project.getBasePath()), startList);
    }

    @Nullable
    private static List<String> findAllParentTsconfigsRec(Path currentDir, Path projectRoot, List<String> acc) {
        Path target = Paths.get(currentDir.toString(), "tsconfig.json");
        if (Files.exists(target)) {
            acc.add(target.toString());
        }
        if (currentDir.equals(projectRoot)) {
            return acc;
        }
        return findAllParentTsconfigsRec(currentDir.getParent(), projectRoot, acc);
    }
}
