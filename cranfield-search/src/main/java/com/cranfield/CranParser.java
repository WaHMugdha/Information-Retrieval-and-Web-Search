package com.cranfield;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CranParser {

    public static List<Map<String, String>> parse(Path file) throws IOException {
        List<Map<String, String>> records = new ArrayList<>();
        Map<String, String> cur = null;
        String field = null;
        StringBuilder buf = new StringBuilder();

        // ISO-8859-1 
        for (String line : Files.readAllLines(file, StandardCharsets.ISO_8859_1)) {
            String t = line.trim();
            if (t.startsWith(".I")) {
                flush(cur, field, buf);
                cur = new HashMap<>();
                records.add(cur);
                cur.put("I", t.substring(2).trim());
                field = null;
            } else if (t.length() == 2 && t.charAt(0) == '.' && "TABW".indexOf(t.charAt(1)) >= 0) {
                flush(cur, field, buf);
                field = t.substring(1);
            } else if (field != null) {
                buf.append(line).append(' ');
            }
        }
        flush(cur, field, buf);
        return records;
    }

    private static void flush(Map<String, String> cur, String field, StringBuilder buf) {
        if (cur != null && field != null) {
            cur.put(field, buf.toString().trim());
        }
        buf.setLength(0);
    }
}
