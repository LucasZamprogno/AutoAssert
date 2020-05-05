package com.lucasaz.intellij.TestPlugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Util {
    public static String spliceInto(String base, String toInsert, int index) {
        List<String> lines = new ArrayList<String>(Arrays.asList(base.split(System.getProperty("line.separator"))));
        List<String> start = lines.subList(0, index + 1); // Could have some index oob issues
        List<String> end = lines.subList(index + 1, lines.size());

        String endFile = Util.createString(start);
        endFile += toInsert;
        endFile += Util.createString(end);

        return endFile;
    }

    // Modified from https://stackoverflow.com/questions/2509170/is-there-an-easy-way-to-concatenate-several-lines-of-text-into-a-string-without
    public static String createString(List<String> lines) {
        String lsp = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder ();
        for (String line : lines) {
            sb.append(line).append(lsp);
        }
        return sb.toString();
    }

    public static String pathToFileContent(Path filepath) {
        try {
            return new String(Files.readAllBytes(filepath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.printf("FILE READ FAILED for " + filepath.toString());
            e.printStackTrace();
            return "";
        }
    }
}
