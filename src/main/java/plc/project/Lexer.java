package plc.project;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The lexer works through three main functions:
 * <p>
 * - {@link #lex()}, which repeatedly calls lexToken() and skips whitespace
 * - {@link #lexToken()}, which lexes the next token
 * - {@link CharStream}, which manages the state of the lexer and literals
 * <p>
 * If the lexer fails to parse something (such as an unterminated string) you
 * should throw a {@link ParseException} with an index at the character which is
 * invalid or missing.
 * <p>
 * The {@link #peek(String...)} and {@link #match(String...)} functions are
 * helpers you need to use, they will make the implementation a lot easier.
 */
public final class Lexer {

    private final CharStream chars;
    public List<Token> list = new ArrayList<>();

    public Lexer(String input) {
        chars = new CharStream(input);
    }

    /**
     * Repeatedly lexes the input using {@link #lexToken()}, also skipping over
     * whitespace where appropriate.
     */
    public List<Token> lex() {
        System.out.println("input: " + chars.input);
        while (chars.has(0)) {
            Token token = lexToken();
            if (token != null) {
                list.add(token);
            }
            if (chars.index == chars.input.length() && !token.getLiteral().equals(";")) {
                System.out.println("hhhhh");
                throw new ParseException("parse error", chars.index);
            }
            chars.skip();
        }
        System.out.println("end");
        return list;

//        lexIdentifier();
//        int j = 0;
//        while (match(" ")){
//            chars.skip();
//        };
//        System.out.println(chars.index);
//
//        System.out.println("input: " + chars.input);
//        match(chars.input, "123", "hello");
//
//        chars.toShow();
//        System.out.println(chars.length);
//        //repeatedly calls lexToken() and skips whitespace
//
//        int index = chars.index;
//        String str = "";
//        while (chars.has(index)){
//            char ch = chars.get(index);
//            if (ch == ' '){
//                continue;
//            }
//            str += ch;
//            System.out.println("ch: "+ch);
//            chars.advance();
//        }
//
//        List<Token> tokens = new ArrayList<Token>();
//        for(int i = 0; i < chars.length; i++) {
//            if (!peek(" ", "\b", "\n", "\r", "\t"))
//                tokens.add(lexToken());
//        }
//
//        System.out.println(tokens.size());
//
//        for (Token e: tokens) {
//            String r = e.getLiteral();
//            System.out.println("each word: "+r);
//        }
//
//        return tokens;
    }

    /**
     * This method determines the type of the next token, delegating to the
     * appropriate lex method. As such, it is best for this method to not change
     * the state of the char stream (thus, use peek not match).
     * <p>
     * The next character should start a valid token since whitespace is handled
     * by {@link #lex()}
     */
    public Token lexToken() {
        if (peek("[A-Za-z_][A-Za-z0-9_-]*")) {
            return lexIdentifier();
        }
        if (peek("[+\\-]?[0-9]+('.'[0-9]+)?")) {
            return lexNumber();
        }
        if (peek("'([A-Za-z]{1}|\\[bnrt'\"\\]{1})'")) {
            return lexCharacter();
        }
        if (peek("^\\\"([^\\\\]*(\\\\[bnrt'\"\\\\])*)*\\\"$")) {
            return lexString();
        }
        if (peek("[<>!=]=?|[^A-Za-z0-9 ]")) {
            return lexOperator();
        }
        if (peek(" ")){
            lexEscape();
            return null;
        }
        return null;
    }

    public Token lexIdentifier() {
        //throw new UnsupportedOperationException(); //TODO
        while (match("[A-Za-z_][A-Za-z0-9_-]*")) ;
        return chars.emit(Token.Type.IDENTIFIER);
    }

    public Token lexNumber() {
        //throw new UnsupportedOperationException(); //TODO
        while (match("[+\\-]?[0-9]+('.'[0-9]+)?")) ;
        return chars.emit(Token.Type.DECIMAL);
    }

    public Token lexCharacter() {
        //throw new UnsupportedOperationException(); //TODO
        while (match("['] ([^'\\n\\r\\\\] | escape) [']")) ;
        return chars.emit(Token.Type.CHARACTER);

    }

    public Token lexString() {
        //throw new UnsupportedOperationException(); //TODO
        while (match("'\"'([^\"\\n\\r\\\\]|escape)*'\"'")) ;
        return chars.emit(Token.Type.STRING);

    }

    public void lexEscape() {
        //throw new UnsupportedOperationException(); //TODO
        while (match(" "))
        return;
    }

    public Token lexOperator() {
        //throw new UnsupportedOperationException(); //TODO
        while (match("[<>!=]=?|[^A-Za-z0-9 ]")) ;
        return chars.emit(Token.Type.OPERATOR);
    }

    /**
     * Returns true if the next sequence of characters match the given patterns,
     * which should be a regex. For example, {@code peek("a", "b", "c")} would
     * return true if the next characters are {@code 'a', 'b', 'c'}.
     */
    public boolean peek(String... patterns) {
        for (int i = 0; i < patterns.length; i++) {
            if (!chars.has(i) || !String.valueOf(chars.get(i)).matches(patterns[i])) {
                return false;
            }
        }
        return true;//TODO (in lecture)
    }

    /**
     * Returns true in the same way as {@link #peek(String...)}, but also
     * advances the character stream past all matched characters if peek returns
     * true. Hint - it's easiest to have this method simply call peek.
     */
    public boolean match(String... patterns) {
        boolean peek = peek(patterns);
        if (peek) {
            for (int i = 0; i < patterns.length; i++) {
                chars.advance();
            }
        }
        return peek;//TODO (in lecture)
    }

    /**
     * A helper class maintaining the input string, current index of the char
     * stream, and the current length of the token being matched.
     * <p>
     * You should rely on peek/match for state management in nearly all cases.
     * The only field you need to access is {@link #index} for any {@link
     * ParseException} which is thrown.
     */
    public static final class CharStream {

        private final String input;
        private int index = 0;
        private int length = 0;

        public CharStream(String input) {
            this.input = input;
        }

        public boolean has(int offset) {
            return index + offset < input.length();
        }

        public char get(int offset) {
            return input.charAt(index + offset);
        }

        public void advance() {
            index++;
            length++;
        }

        public void skip() {
            length = 0;
        }

        public Token emit(Token.Type type) {
            int start = index - length;
            skip();
            return new Token(type, input.substring(start, index), start);
        }

        public void toShow() {
            System.out.println(input);
        }

    }

    public static void main(String[] args) {
        Lexer l = new Lexer("hello world you and me 1dd2 3 asdf;");
        l.lex();
        for (Token e :
                l.list) {
            System.out.println(e.getLiteral());
        }
    }

}
