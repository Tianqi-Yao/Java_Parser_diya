package plc.project;

import java.util.List;
import java.util.ArrayList;


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
        //only white space inside double quote
        while (chars.has(0)){
           if(match("[ \b\n\r\t]"))
               chars.skip();
           else
               list.add(lexToken());
        }
        return list;
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
        // IDENTIFIER
        if (peek("[A-Za-z_]")){
            return lexIdentifier();
        }
        //number
        else if (peek("[+\\-]","[0-9]") || peek("[0-9]")){
            return lexNumber();
        }
        //CHARACTER
        else if (peek("\'")){
            return lexCharacter();
        }
        //string
        else if (peek("\"")){
            return lexString();
        }
        //operator
        else {
            return lexOperator();
        }
    }

    public Token lexIdentifier() {
        while (match("[A-Za-z0-9_-]")) ;
        return chars.emit(Token.Type.IDENTIFIER);
    }

    public Token lexNumber() {
        match("[+-]");
        while (match("[0-9]"));
        if (match("[\\.]","[0-9]")){ // \\. -> .      \\->\       \. -> .
            while(match("[0-9]"));
            return chars.emit(Token.Type.DECIMAL);
        }
        return chars.emit(Token.Type.INTEGER);
    }

    public Token lexCharacter() {
        match("'");
//        if(match(" "))
//            throw new ParseException("empty char error", chars.index);
        if (peek("[^'\n\r]")) {
            if (peek("\\\\"))
                lexEscape();
//            else if (peek(" ")) {
//                throw new ParseException("empty char error", chars.index);
//            }
            else {
                chars.advance();
            }
        }
        else
            throw new ParseException("invalid char", chars.index);

        if (!match("'")) {
            throw new ParseException("character error, missing single quote", chars.index);
        }

        return chars.emit(Token.Type.CHARACTER);
    }

    public Token lexString() {
        match("\"");
        while(peek("[^\"\n\r]")) {
            if(peek("\\\\"))
                lexEscape();
            else
                chars.advance();
        }
        if(!match("\"")){
            throw new ParseException("character error, missing double quote", chars.index);
        }

        return chars.emit(Token.Type.STRING);
    }

    public void lexEscape() {
        //invalid escape character
        match("\\\\");
        if(!match("[bnrt\'\"\\\\]")) {
            throw new ParseException ("invalid escpae", chars.index);
        }
    }

    public Token lexOperator() {
        if (match("[<>!=]")) {
            match("=");  //look for the next equal sign
        }
        else {
            chars.advance();
        }

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
        return true;
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
        return peek;
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
    }
}