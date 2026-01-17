package com.kukso.hy.lib.locale;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles loading and parsing of locale JSON files.
 * Flattens nested JSON structures to dot-notation keys.
 */
public final class LocaleLoader {

    private static final Gson GSON = new Gson();

    private LocaleLoader() {
        // Utility class
    }

    /**
     * Loads all locale files from the given directory.
     *
     * @param localesDir The directory containing locale JSON files
     * @return Map of locale code to flattened key-value translations
     */
    public static Map<String, Map<String, String>> loadAll(Path localesDir) {
        Map<String, Map<String, String>> allLocales = new ConcurrentHashMap<>();

        if (!Files.exists(localesDir) || !Files.isDirectory(localesDir)) {
            return allLocales;
        }

        try (var stream = Files.list(localesDir)) {
            stream.filter(p -> p.toString().endsWith(".json"))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        String localeCode = fileName.substring(0, fileName.lastIndexOf('.'));
                        Map<String, String> translations = loadFile(path);
                        if (!translations.isEmpty()) {
                            allLocales.put(localeCode, translations);
                        }
                    });
        } catch (IOException e) {
            // Silent fail, empty map returned
        }

        return allLocales;
    }

    /**
     * Loads a single locale file and flattens it to dot-notation keys.
     *
     * @param path Path to the JSON file
     * @return Flattened key-value map
     */
    public static Map<String, String> loadFile(Path path) {
        if (!Files.exists(path)) {
            return Collections.emptyMap();
        }

        try (InputStream is = Files.newInputStream(path);
             Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            return parseJson(reader);
        } catch (IOException e) {
            return Collections.emptyMap();
        }
    }

    /**
     * Loads a locale file from plugin resources (JAR).
     *
     * @param resourcePath Path inside the JAR (e.g., "locales/en_US.json")
     * @return Flattened key-value map
     */
    public static Map<String, String> loadFromResource(String resourcePath) {
        try (InputStream is = LocaleLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                return Collections.emptyMap();
            }
            try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                return parseJson(reader);
            }
        } catch (IOException e) {
            return Collections.emptyMap();
        }
    }

    /**
     * Extracts a resource file to the target path if it doesn't exist.
     *
     * @param resourcePath Path inside the JAR
     * @param targetPath   Target file path
     * @return true if extraction was successful or file already exists
     */
    public static boolean extractResource(String resourcePath, Path targetPath) {
        if (Files.exists(targetPath)) {
            return true;
        }

        try {
            // Ensure parent directory exists
            Files.createDirectories(targetPath.getParent());

            try (InputStream is = LocaleLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
                if (is == null) {
                    return false;
                }
                Files.copy(is, targetPath);
                return true;
            }
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Parses JSON from a reader and flattens to dot-notation.
     *
     * @param reader The reader for JSON content
     * @return Flattened key-value map
     */
    private static Map<String, String> parseJson(Reader reader) {
        JsonElement element = JsonParser.parseReader(reader);

        if (element == null || !element.isJsonObject()) {
            return Collections.emptyMap();
        }

        Map<String, String> flattened = new HashMap<>();
        flatten("", element.getAsJsonObject(), flattened);
        return flattened;
    }

    /**
     * Recursively flattens a JSON object to dot-notation keys.
     *
     * @param prefix Current key prefix
     * @param source Source JSON object to flatten
     * @param target Target map for flattened entries
     */
    private static void flatten(String prefix, JsonObject source, Map<String, String> target) {
        for (String key : source.keySet()) {
            JsonElement value = source.get(key);
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;

            if (value.isJsonObject()) {
                flatten(fullKey, value.getAsJsonObject(), target);
            } else if (value.isJsonPrimitive()) {
                target.put(fullKey, value.getAsString());
            }
        }
    }
}
