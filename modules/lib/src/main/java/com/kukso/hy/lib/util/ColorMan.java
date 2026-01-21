package com.kukso.hy.lib.util;

import com.hypixel.hytale.server.core.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for translating legacy color codes to Hytale's Message format.
 * Supports Minecraft-style color codes, hex colors,
 * and multiple color changes within a single string.
 */
public class ColorMan {

    private static final char COLOR_CHAR = '&';
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    // Minecraft legacy color code to hex mapping
    private static final Map<Character, String> COLOR_MAP = new HashMap<>();

    static {
        // Standard colors (0-9, a-f)
        COLOR_MAP.put('0', "#000000"); // Black
        COLOR_MAP.put('1', "#0000AA"); // Dark Blue
        COLOR_MAP.put('2', "#00AA00"); // Dark Green
        COLOR_MAP.put('3', "#00AAAA"); // Dark Aqua
        COLOR_MAP.put('4', "#AA0000"); // Dark Red
        COLOR_MAP.put('5', "#AA00AA"); // Dark Purple
        COLOR_MAP.put('6', "#FFAA00"); // Gold
        COLOR_MAP.put('7', "#AAAAAA"); // Gray
        COLOR_MAP.put('8', "#555555"); // Dark Gray
        COLOR_MAP.put('9', "#5555FF"); // Blue
        COLOR_MAP.put('a', "#55FF55"); // Green
        COLOR_MAP.put('b', "#55FFFF"); // Aqua
        COLOR_MAP.put('c', "#FF5555"); // Red
        COLOR_MAP.put('d', "#FF55FF"); // Light Purple
        COLOR_MAP.put('e', "#FFFF55"); // Yellow
        COLOR_MAP.put('f', "#FFFFFF"); // White
    }

    /**
     * Translates a string with color codes to a Hytale Message.
     * Supports legacy codes, format codes (bold, italic), and hex codes.
     * Note: Only bold and italic are supported by Hytale's Message API.
     *
     * @param text The text with color codes
     * @return A formatted Hytale Message
     */
    public static Message translate(String text) {
        if (text == null || text.isEmpty()) {
            return Message.raw("");
        }

        // First, process hex colors
        text = processHexColors(text);

        // Parse the text into segments with formatting
        List<MessageSegment> segments = parseSegments(text);

        // If only one segment with no formatting, return simple message
        if (segments.size() == 1 && !segments.get(0).hasFormatting()) {
            return Message.raw(segments.get(0).text);
        }

        // Build the message by chaining segments
        return buildMessage(segments);
    }

    /**
     * Replaces hex color codes (&#RRGGBB) with internal format for processing.
     */
    private static String processHexColors(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            // Replace with a special marker that won't conflict with regular codes
            matcher.appendReplacement(sb, "§x" + hex);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Parses the text into segments with formatting information.
     */
    private static List<MessageSegment> parseSegments(String text) {
        List<MessageSegment> segments = new ArrayList<>();
        StringBuilder currentText = new StringBuilder();
        MessageSegment currentSegment = new MessageSegment();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // Check for color/format codes
            if (c == COLOR_CHAR && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(i + 1));

                // Save current segment if it has text
                if (currentText.length() > 0) {
                    currentSegment.text = currentText.toString();
                    segments.add(currentSegment);
                    currentText = new StringBuilder();
                    currentSegment = new MessageSegment(currentSegment);
                }

                // Process the code
                if (COLOR_MAP.containsKey(code)) {
                    // Color code - reset formatting but keep the color
                    currentSegment = new MessageSegment();
                    currentSegment.color = COLOR_MAP.get(code);
                } else if (code == 'r') {
                    // Reset all formatting
                    currentSegment = new MessageSegment();
                } else {
                    // Format code
                    applyFormatCode(currentSegment, code);
                }

                i++; // Skip the code character
            } else if (c == '§' && i + 1 < text.length() && text.charAt(i + 1) == 'x' && i + 7 < text.length()) {
                // Hex color marker from processHexColors
                if (currentText.length() > 0) {
                    currentSegment.text = currentText.toString();
                    segments.add(currentSegment);
                    currentText = new StringBuilder();
                    currentSegment = new MessageSegment(currentSegment);
                }

                String hex = text.substring(i + 2, i + 8);
                currentSegment = new MessageSegment();
                currentSegment.color = "#" + hex;
                i += 7; // Skip the hex code
            } else {
                currentText.append(c);
            }
        }

        // Add final segment
        if (currentText.length() > 0) {
            currentSegment.text = currentText.toString();
            segments.add(currentSegment);
        }

        return segments;
    }

    /**
     * Applies a format code to the segment.
     * Note: Only bold (&l) and italic (&o) are supported by Hytale's Message API.
     * Other format codes (&n, &m, &k) are ignored.
     */
    private static void applyFormatCode(MessageSegment segment, char code) {
        switch (code) {
            case 'l':
                segment.bold = true;
                break;
            case 'o':
                segment.italic = true;
                break;
            // Hytale Message doesn't support these formatting options
            // case 'n': underlined
            // case 'm': strikethrough
            // case 'k': obfuscated
        }
    }

    /**
     * Builds a Message from segments by chaining them together.
     * Each segment maintains its own color and formatting.
     */
    private static Message buildMessage(List<MessageSegment> segments) {
        if (segments.isEmpty()) {
            return Message.raw("");
        }

        // Build the first segment
        Message result = buildSegment(segments.get(0));

        // Insert remaining segments
        for (int i = 1; i < segments.size(); i++) {
            result = result.insert(buildSegment(segments.get(i)));
        }

        return result;
    }

    /**
     * Builds a single Message segment with its formatting.
     */
    private static Message buildSegment(MessageSegment segment) {
        if (segment.text.isEmpty()) {
            return Message.raw("");
        }

        Message message = Message.raw(segment.text);

        if (segment.color != null) {
            message = message.color(segment.color);
        }
        if (segment.bold) {
            message = message.bold(true);
        }
        if (segment.italic) {
            message = message.italic(true);
        }

        return message;
    }

    /**
     * Internal class to represent a message segment with formatting.
     */
    private static class MessageSegment {
        String text = "";
        String color = null;
        boolean bold = false;
        boolean italic = false;

        MessageSegment() {}

        // Copy constructor to preserve formatting
        MessageSegment(MessageSegment other) {
            this.color = other.color;
            this.bold = other.bold;
            this.italic = other.italic;
        }

        boolean hasFormatting() {
            return color != null || bold || italic;
        }
    }
}
