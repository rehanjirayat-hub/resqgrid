package api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal JSON encoding and decoding for the documented API endpoints.
 *
 * This exists so the REST layer needs no serialization dependency beyond the
 * documented technology set. It supports the value types the endpoints
 * require and nothing more.
 */
public final class Json {

    private Json() {
    }

    public static String write(Object value) {

        StringBuilder builder = new StringBuilder();

        writeValue(builder, value);

        return builder.toString();
    }

    private static void writeValue(StringBuilder builder, Object value) {

        if (value == null) {
            builder.append("null");

        } else if (value instanceof String string) {
            writeString(builder, string);

        } else if (value instanceof Number number) {
            builder.append(number);

        } else if (value instanceof Boolean bool) {
            builder.append(bool);

        } else if (value instanceof Map<?, ?> map) {
            writeObject(builder, map);

        } else if (value instanceof Iterable<?> items) {
            writeArray(builder, items);

        } else {
            writeString(builder, value.toString());
        }
    }

    private static void writeObject(StringBuilder builder, Map<?, ?> map) {

        builder.append('{');

        boolean first = true;

        for (Map.Entry<?, ?> entry : map.entrySet()) {

            if (!first) {
                builder.append(',');
            }

            first = false;

            writeString(builder, entry.getKey().toString());
            builder.append(':');
            writeValue(builder, entry.getValue());
        }

        builder.append('}');
    }

    private static void writeArray(StringBuilder builder, Iterable<?> items) {

        builder.append('[');

        boolean first = true;

        for (Object item : items) {

            if (!first) {
                builder.append(',');
            }

            first = false;

            writeValue(builder, item);
        }

        builder.append(']');
    }

    private static void writeString(StringBuilder builder, String value) {

        builder.append('"');

        for (int index = 0; index < value.length(); index++) {

            char character = value.charAt(index);

            switch (character) {

                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> {

                    if (character < 0x20) {
                        builder.append(String.format("\\u%04x", (int) character));
                    } else {
                        builder.append(character);
                    }
                }
            }
        }

        builder.append('"');
    }

    public static Map<String, Object> readObject(String json) {

        Object parsed = new Parser(json).parseValue();

        if (!(parsed instanceof Map)) {
            throw new IllegalArgumentException("Request body must be a JSON object.");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> object = (Map<String, Object>) parsed;

        return object;
    }

    private static final class Parser {

        private final String source;
        private int position;

        Parser(String source) {
            this.source = source;
        }

        Object parseValue() {

            skipWhitespace();

            if (position >= source.length()) {
                throw new IllegalArgumentException("Unexpected end of JSON input.");
            }

            char character = source.charAt(position);

            return switch (character) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't', 'f' -> parseBoolean();
                case 'n' -> parseNull();
                default -> parseNumber();
            };
        }

        private Map<String, Object> parseObject() {

            Map<String, Object> values = new LinkedHashMap<>();

            position++;

            skipWhitespace();

            if (peek() == '}') {
                position++;
                return values;
            }

            while (true) {

                skipWhitespace();

                String key = parseString();

                skipWhitespace();

                if (peek() != ':') {
                    throw new IllegalArgumentException(
                            "Expected ':' in JSON object.");
                }

                position++;

                values.put(key, parseValue());

                skipWhitespace();

                char next = peek();

                if (next == ',') {
                    position++;
                    continue;
                }

                if (next == '}') {
                    position++;
                    return values;
                }

                throw new IllegalArgumentException("Malformed JSON object.");
            }
        }

        private List<Object> parseArray() {

            List<Object> items = new ArrayList<>();

            position++;

            skipWhitespace();

            if (peek() == ']') {
                position++;
                return items;
            }

            while (true) {

                items.add(parseValue());

                skipWhitespace();

                char next = peek();

                if (next == ',') {
                    position++;
                    continue;
                }

                if (next == ']') {
                    position++;
                    return items;
                }

                throw new IllegalArgumentException("Malformed JSON array.");
            }
        }

        private String parseString() {

            if (peek() != '"') {
                throw new IllegalArgumentException("Expected a JSON string.");
            }

            position++;

            StringBuilder builder = new StringBuilder();

            while (true) {

                if (position >= source.length()) {
                    throw new IllegalArgumentException("Unterminated JSON string.");
                }

                char character = source.charAt(position++);

                if (character == '"') {
                    return builder.toString();
                }

                if (character != '\\') {
                    builder.append(character);
                    continue;
                }

                char escaped = source.charAt(position++);

                switch (escaped) {
                    case 'n' -> builder.append('\n');
                    case 'r' -> builder.append('\r');
                    case 't' -> builder.append('\t');
                    case 'b' -> builder.append('\b');
                    case 'f' -> builder.append('\f');
                    case 'u' -> {
                        builder.append((char) Integer.parseInt(
                                source.substring(position, position + 4), 16));
                        position += 4;
                    }
                    default -> builder.append(escaped);
                }
            }
        }

        private Boolean parseBoolean() {

            if (source.startsWith("true", position)) {
                position += 4;
                return Boolean.TRUE;
            }

            if (source.startsWith("false", position)) {
                position += 5;
                return Boolean.FALSE;
            }

            throw new IllegalArgumentException("Malformed JSON value.");
        }

        private Object parseNull() {

            if (source.startsWith("null", position)) {
                position += 4;
                return null;
            }

            throw new IllegalArgumentException("Malformed JSON value.");
        }

        private Object parseNumber() {

            int start = position;

            while (position < source.length()) {

                char character = source.charAt(position);

                if (Character.isDigit(character)
                        || character == '-'
                        || character == '+'
                        || character == '.'
                        || character == 'e'
                        || character == 'E') {

                    position++;

                } else {
                    break;
                }
            }

            String number = source.substring(start, position);

            if (number.isEmpty()) {
                throw new IllegalArgumentException("Malformed JSON number.");
            }

            if (number.contains(".") || number.contains("e") || number.contains("E")) {
                return Double.parseDouble(number);
            }

            return Long.parseLong(number);
        }

        private char peek() {

            if (position >= source.length()) {
                throw new IllegalArgumentException("Unexpected end of JSON input.");
            }

            return source.charAt(position);
        }

        private void skipWhitespace() {

            while (position < source.length()
                    && Character.isWhitespace(source.charAt(position))) {

                position++;
            }
        }
    }
}