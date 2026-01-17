package com.kukso.hy.lib.locale;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for resolving placeholders in localized strings.
 * Placeholders use the format {placeholder_name}.
 */
public final class PlaceholderResolver {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)}");

    private PlaceholderResolver() {
        // Utility class
    }

    /**
     * Resolves all placeholders in the given text using the provided values map.
     *
     * @param text   The text containing placeholders
     * @param values Map of placeholder names to their replacement values
     * @return The text with all placeholders resolved
     */
    public static String resolve(String text, Map<String, String> values) {
        if (text == null || text.isEmpty() || values == null || values.isEmpty()) {
            return text;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String replacement = values.getOrDefault(placeholder, matcher.group(0));
            // Escape special regex characters in replacement
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Checks if the given text contains any placeholders.
     *
     * @param text The text to check
     * @return true if the text contains placeholders, false otherwise
     */
    public static boolean hasPlaceholders(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return PLACEHOLDER_PATTERN.matcher(text).find();
    }
}
