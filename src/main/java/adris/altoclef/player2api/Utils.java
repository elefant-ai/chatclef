package adris.altoclef.player2api;

import com.google.gson.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Utils {
    /**
     * Replaces placeholders in a string with corresponding values from a map.
     * Placeholders are of the form {{key}}.
     *
     * @param input The input string containing placeholders.
     * @param replacements A map containing keys and their corresponding replacement values.
     * @return The input string with placeholders replaced.
     */
    public static String replacePlaceholders(String input, Map<String, String> replacements) {
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            String placeholder = "\\{\\{" + entry.getKey() + "}}";  // Escape {{hole}}
            input = input.replaceAll(placeholder, entry.getValue());
        }
        return input;
    }

    /**
     * Safely retrieves a string value from a JsonObject.
     * Returns null if the field does not exist or is null.
     *
     * @param input The JsonObject to extract the field from.
     * @param fieldName The name of the field to retrieve.
     * @return The string value of the field, or null if it does not exist or is null.
     */
    public static String getStringJsonSafely(JsonObject input, String fieldName) {
        return (input.has(fieldName) && !input.get(fieldName).isJsonNull())
                ? input.get(fieldName).getAsString()
                : null;
    }
    /**
     * Converts a JSON array of strings into a Java String[] array.
     *
     * @param jsonArray The input JSON array.
     * @return A Java String[] array containing the values. Skips non-string elements.
     */
    public static String[] jsonArrayToStringArray(JsonArray jsonArray) {
        if (jsonArray == null) {
            return new String[0];
        }

        List<String> stringList = new ArrayList<>();
        for (JsonElement element : jsonArray) {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                stringList.add(element.getAsString());
            } else {
                System.err.println("Warning: Skipping non-string element in JSON array: " + element);
            }
        }
        return stringList.toArray(new String[0]);
    }

    /**
     * Safely retrieves a string array from a JsonObject.
     * Returns null if the field does not exist, is null, or is not a valid JSON array of strings.
     *
     * @param input The JsonObject to extract the field from.
     * @param fieldName The name of the field to retrieve.
     * @return A String[] array containing the values, or null if the field is missing or invalid.
     */
    public static String[] getStringArrayJsonSafely(JsonObject input, String fieldName) {
        if (!input.has(fieldName) || input.get(fieldName).isJsonNull()) {
            return null;
        }

        JsonElement element = input.get(fieldName);
        if (!element.isJsonArray()) {
            System.err.println("Warning: Expected a JSON array for field '" + fieldName + "', but found a different type.");
            return null;
        }

        JsonArray jsonArray = element.getAsJsonArray();
        return jsonArrayToStringArray(jsonArray);
    }



    /**
     * Removes Markdown-style code block formatting (```json ... ```) and parses the JSON.
     *
     * @param content The raw string content from the LLM response.
     * @return The cleaned JSON object.
     * @throws JsonSyntaxException If the content is not valid JSON.
     */
    public static JsonObject parseCleanedJson(String content) throws JsonSyntaxException {
        content = content.replaceAll("^```json\\s*", "").replaceAll("\\s*```$", "").trim();
        JsonParser parser = new JsonParser();
        return parser.parse(content).getAsJsonObject();
    }

    /**
     * Splits a multiline string into an array of strings, where each line is an element.
     * Handles different newline formats (\n, \r, \r\n).
     *
     * @param input The input string containing multiple lines.
     * @return A string array containing each line as an element. Returns an empty array if input is null or empty.
     */
    public static String[] splitLinesToArray(String input) {
        if (input == null || input.isEmpty()) {
            return new String[0];
        }
        return input.split("\\R+"); // \\R is any of: (\n, \r, \r\n)
    }
    public static JsonObject deepCopy(JsonObject original) {
        JsonParser parser = new JsonParser();
        return parser.parse(original.toString()).getAsJsonObject();
    }
}