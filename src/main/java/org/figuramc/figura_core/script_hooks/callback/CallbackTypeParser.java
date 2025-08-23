package org.figuramc.figura_core.script_hooks.callback;

import org.figuramc.figura_core.util.data_structures.PeekableIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Parser for CallbackType from String
 */
public class CallbackTypeParser {

    // Parsing functions to convert String -> CallbackType
    public static CallbackType<?> parse(String string) {
        // Make a token iterator over the string:
        PeekableIterator<Token> toks = new PeekableIterator<>(Token.PATTERN.matcher(string).results().map(result -> {
            // If 1-char, be slightly more efficient with no substring
            if (result.end() == result.start() + 1) {
                char c = string.charAt(result.start());
                return switch (c) {
                    case '(' -> Token.LPAREN;
                    case ')' -> Token.RPAREN;
                    case '{' -> Token.LCURLY;
                    case '}' -> Token.RCURLY;
                    case '[' -> Token.LSQUARE;
                    case ']' -> Token.RSQUARE;
                    case '?' -> Token.QUESTION_MARK;
                    case ',' -> Token.COMMA;
                    default -> {
                        if (Character.isWhitespace(c)) yield null; // Will be filtered
                        throw new RuntimeException("Parse failed: Unexpected character \"" + c + "\" at index " + result.start());
                    }
                };
            } else {
                String str = string.substring(result.start(), result.end());
                return switch (str) {
                    case "->" -> Token.ARROW;
                    case "any" -> Token.ANY;
                    case "bool" -> Token.BOOL;
                    case "f32" -> Token.F32;
                    case "f64" -> Token.F64;
                    case "string" -> Token.STRING;
                    default -> throw new RuntimeException("Parse failed: Unexpected token \"" + str + "\" at index " + result.start());
                };
            }
        }).filter(Objects::nonNull).iterator());
        // Parse iterator into a type
        return parseType(toks);
    }

    private static CallbackType<?> parseType(PeekableIterator<Token> toks) {
        // If there's a special character, do special parse
        Token next = toks.peek();
        if (next == null) throw new RuntimeException("Unexpected EOF");
        CallbackType<?> ty = switch (toks.next()) {
            case LPAREN -> {
                List<CallbackType<?>> types = new ArrayList<>();
                while (!toks.take(Token.RPAREN)) {
                    types.add(parseType(toks));
                    if (!toks.take(Token.COMMA)) {
                        if (!toks.take(Token.RPAREN)) throw new RuntimeException("Expected ) to close (");
                        break;
                    }
                }
                if (types.size() > CallbackType.MAX_TUPLE_SIZE) {
                    throw new RuntimeException("Tuple too big! Tuple may be at most " + CallbackType.MAX_TUPLE_SIZE + " elements, but found " + types.size());
                }
                yield switch (types.size()) {
                    case 0 -> CallbackType.Unit.INSTANCE;
                    case 1 -> types.getFirst();
                    case 2 -> new CallbackType.Tuple2<>(types.get(0), types.get(1));
                    case 3 -> new CallbackType.Tuple3<>(types.get(0), types.get(1), types.get(2));
                    case 4 -> new CallbackType.Tuple4<>(types.get(0), types.get(1), types.get(2), types.get(3));
                    case 5 -> new CallbackType.Tuple5<>(types.get(0), types.get(1), types.get(2), types.get(3), types.get(4));
                    case 6 -> new CallbackType.Tuple6<>(types.get(0), types.get(1), types.get(2), types.get(3), types.get(4), types.get(5));
                    case 7 -> new CallbackType.Tuple7<>(types.get(0), types.get(1), types.get(2), types.get(3), types.get(4), types.get(5), types.get(6));
                    case 8 -> new CallbackType.Tuple8<>(types.get(0), types.get(1), types.get(2), types.get(3), types.get(4), types.get(5), types.get(6), types.get(7));
                    default -> throw new IllegalStateException();
                };
            }
            case LSQUARE -> {
                CallbackType<?> listItem = parseType(toks);
                if (!toks.take(Token.RSQUARE))
                    throw new RuntimeException("Expected ] to close [");
                yield new CallbackType.List<>(listItem);
            }
            case LCURLY -> {
                CallbackType<?> key = parseType(toks);
                if (!toks.take(Token.ARROW)) throw new RuntimeException("Expected -> inside map definition");
                CallbackType<?> value = parseType(toks);
                if (!toks.take(Token.RCURLY)) throw new RuntimeException("Expected } to close {");
                yield new CallbackType.Map<>(key, value);
            }
            case ANY -> CallbackType.Any.INSTANCE;
            case BOOL -> CallbackType.Bool.INSTANCE;
            case F32 -> CallbackType.F32.INSTANCE;
            case F64 -> CallbackType.F64.INSTANCE;
            case STRING -> CallbackType.Str.INSTANCE;
            default -> throw new RuntimeException("Unexpected token \"" + next + "\"");
        };
        // Parse ?s to turn it Optional
        while (toks.take(Token.QUESTION_MARK)) {
            ty = new CallbackType.Optional<>(ty);
            toks.next();
        }
        // Parse -> to find functions (right-associative)
        if (toks.take(Token.ARROW)) {
            ty = new CallbackType.Func<>(ty, parseType(toks));
        }
        return ty;
    }

    // Token
    private enum Token {
        // Punctuation
        LPAREN, RPAREN, ARROW, LCURLY, RCURLY, LSQUARE, RSQUARE, QUESTION_MARK, COMMA,
        // Keywords
        ANY, BOOL, F32, F64, STRING;

        // Pattern
        private static final Pattern PATTERN = Pattern.compile("->|any|bool|f32|f64|string|.");
    }

}
