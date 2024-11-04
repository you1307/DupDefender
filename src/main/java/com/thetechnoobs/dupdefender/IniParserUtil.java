package com.thetechnoobs.dupdefender;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class IniParserUtil {

    public static Map<String, Map<String, String>> parseIniFileToMap(String iniFilePath, Charset encoding) throws IOException {
        Map<String, Map<String, String>> result = new HashMap<>();
        Map<String, String> currentSection = new HashMap<>();
        String currentSectionName = "";

        result.put(currentSectionName, currentSection);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(iniFilePath), encoding))) {
            String line;
            int lineNumber = 0;

            reader.mark(1);
            int ch = reader.read();
            if (ch != '\ufeff') {
                reader.reset();
            }

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty() || line.startsWith(";") || line.startsWith("#")) {
                    continue;
                }

                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSectionName = line.substring(1, line.length() - 1).trim();
                    currentSection = result.computeIfAbsent(currentSectionName, k -> new HashMap<>());
                    continue;
                }

                String[] keyValue = parseKeyValue(line, lineNumber);
                if (keyValue != null) {
                    String key = keyValue[0];
                    String value = keyValue[1];
                    currentSection.put(key, value);
                } else {
                    System.err.println("Warning: Skipping malformed line " + lineNumber + ": " + line);
                }
            }
        } catch (IOException e) {
            throw new IOException("Error reading INI file: " + iniFilePath, e);
        }

        return result;
    }

    private static String[] parseKeyValue(String line, int lineNumber) {
        line = removeInlineComment(line);

        // Look for the first '=' or ':'
        int index = line.indexOf('=');
        if (index == -1) {
            index = line.indexOf(':');
        }

        if (index == -1) {
            return null; // Line doesn't contain '=' or ':', cannot parse
        }

        String key = line.substring(0, index).trim();
        String value = line.substring(index + 1).trim();

        value = stripQuotes(value);

        return new String[]{key, value};
    }

    private static String removeInlineComment(String line) {
        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '\'' && !inDoubleQuotes) {
                inSingleQuotes = !inSingleQuotes;
            } else if (c == '\"' && !inSingleQuotes) {
                inDoubleQuotes = !inDoubleQuotes;
            } else if (!inSingleQuotes && !inDoubleQuotes) {
                if (c == ';' || c == '#') {
                    return line.substring(0, i).trim();
                }
            }
        }

        return line;
    }

    private static String stripQuotes(String value) {
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }
}

