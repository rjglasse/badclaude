package badclaude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A tiny JSON reader/writer -- just enough to talk to the LLM API, with no
 * external libraries. You should not need to change this file, but it is
 * short enough to read, and reading it is a nice exercise.
 *
 * Parsing produces plain Java objects:
 *   JSON object  becomes  Map&lt;String, Object&gt;
 *   JSON array   becomes  List&lt;Object&gt;
 *   JSON string  becomes  String
 *   JSON number  becomes  Double
 *   true/false   becomes  Boolean
 *   null         becomes  null
 */
public class Json {

    /** Escapes a string and wraps it in double quotes, ready to embed in JSON. */
    public static String quote(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.append('"').toString();
    }

    /** Parses a JSON document. Throws IllegalArgumentException on bad input. */
    public static Object parse(String text) {
        Json parser = new Json(text);
        Object value = parser.readValue();
        parser.skipWhitespace();
        if (parser.pos < parser.text.length()) {
            throw parser.error("Unexpected trailing characters");
        }
        return value;
    }

    /**
     * Walks into a parsed JSON structure: Strings index into Maps, Integers
     * index into Lists. Returns null if any step along the path is missing.
     *
     * Example: Json.get(parsed, "choices", 0, "message", "content")
     */
    public static Object get(Object json, Object... path) {
        Object current = json;
        for (Object step : path) {
            if (current instanceof Map && step instanceof String) {
                current = ((Map<?, ?>) current).get(step);
            } else if (current instanceof List && step instanceof Integer) {
                List<?> list = (List<?>) current;
                int index = (Integer) step;
                current = (index >= 0 && index < list.size()) ? list.get(index) : null;
            } else {
                return null;
            }
        }
        return current;
    }

    // ------------------------------------------------------------------
    // The parser below reads the text one character at a time, dispatching
    // on the first character of each value. This style is called a
    // recursive descent parser.
    // ------------------------------------------------------------------

    private final String text;
    private int pos = 0;

    private Json(String text) {
        this.text = text;
    }

    private Object readValue() {
        skipWhitespace();
        char c = peek();
        if (c == '{') return readObject();
        if (c == '[') return readArray();
        if (c == '"') return readString();
        if (c == 't' || c == 'f') return readBoolean();
        if (c == 'n') { expectWord("null"); return null; }
        return readNumber();
    }

    private Map<String, Object> readObject() {
        Map<String, Object> map = new HashMap<>();
        expect('{');
        skipWhitespace();
        if (peek() == '}') { pos++; return map; }
        while (true) {
            skipWhitespace();
            String key = readString();
            skipWhitespace();
            expect(':');
            map.put(key, readValue());
            skipWhitespace();
            char c = next();
            if (c == '}') return map;
            if (c != ',') throw error("Expected ',' or '}'");
        }
    }

    private List<Object> readArray() {
        List<Object> list = new ArrayList<>();
        expect('[');
        skipWhitespace();
        if (peek() == ']') { pos++; return list; }
        while (true) {
            list.add(readValue());
            skipWhitespace();
            char c = next();
            if (c == ']') return list;
            if (c != ',') throw error("Expected ',' or ']'");
        }
    }

    private String readString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = next();
            if (c == '"') return sb.toString();
            if (c == '\\') {
                char esc = next();
                switch (esc) {
                    case '"':  sb.append('"');  break;
                    case '\\': sb.append('\\'); break;
                    case '/':  sb.append('/');  break;
                    case 'n':  sb.append('\n'); break;
                    case 'r':  sb.append('\r'); break;
                    case 't':  sb.append('\t'); break;
                    case 'b':  sb.append('\b'); break;
                    case 'f':  sb.append('\f'); break;
                    case 'u':
                        if (pos + 4 > text.length()) throw error("Bad \\u escape");
                        sb.append((char) Integer.parseInt(text.substring(pos, pos + 4), 16));
                        pos += 4;
                        break;
                    default:
                        throw error("Bad escape: \\" + esc);
                }
            } else {
                sb.append(c);
            }
        }
    }

    private Boolean readBoolean() {
        if (peek() == 't') { expectWord("true"); return Boolean.TRUE; }
        expectWord("false");
        return Boolean.FALSE;
    }

    private Double readNumber() {
        int start = pos;
        while (pos < text.length() && "+-0123456789.eE".indexOf(text.charAt(pos)) >= 0) {
            pos++;
        }
        if (start == pos) throw error("Expected a value");
        try {
            return Double.parseDouble(text.substring(start, pos));
        } catch (NumberFormatException e) {
            throw error("Bad number: " + text.substring(start, pos));
        }
    }

    private void skipWhitespace() {
        while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
            pos++;
        }
    }

    private char peek() {
        if (pos >= text.length()) throw error("Unexpected end of input");
        return text.charAt(pos);
    }

    private char next() {
        char c = peek();
        pos++;
        return c;
    }

    private void expect(char c) {
        if (next() != c) throw error("Expected '" + c + "'");
    }

    private void expectWord(String word) {
        if (!text.startsWith(word, pos)) throw error("Expected '" + word + "'");
        pos += word.length();
    }

    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException(message + " (at position " + pos + ")");
    }
}
