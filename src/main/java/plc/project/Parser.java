package plc.project;

import sun.security.krb5.internal.PAEncTSEnc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.chrono.MinguoDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The parser takes the sequence of tokens emitted by the lexer and turns that
 * into a structured representation of the program, called the Abstract Syntax
 * Tree (AST).
 * <p>
 * The parser has a similar architecture to the lexer, just with {@link Token}s
 * instead of characters. As before, {@link #peek(Object...)} and {@link
 * #match(Object...)} are helpers to make the implementation easier.
 * <p>
 * This type of parser is called <em>recursive descent</em>. Each rule in our
 * grammar will have it's own function, and reference to other rules correspond
 * to calling that functions.
 */
public final class Parser {

    private final TokenStream tokens;

    public Parser(List<Token> tokens) {
        this.tokens = new TokenStream(tokens);
    }

    /**
     * Parses the {@code source} rule.
     */
    public Ast.Source parseSource() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code field} rule. This method should only be called if the
     * next tokens start a field, aka {@code LET}.
     */
    public Ast.Field parseField() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code method} rule. This method should only be called if the
     * next tokens start a method, aka {@code DEF}.
     */
    public Ast.Method parseMethod() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code statement} rule and delegates to the necessary method.
     * If the next tokens do not start a declaration, if, while, or return
     * statement, then it is an expression/assignment statement.
     */
    public Ast.Stmt parseStatement() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        Optional<Ast.Expr> receiver = Optional.empty();
        String name = "";
        List<Ast.Expr> arguments = new ArrayList<>();
        String name2 = "";


        // Determine if it is an assignment
        int index = tokens.index;
        while (tokens.has(index)) {
            // nmae.mike();  || name.mike;
            // name.
            if (peek(Token.Type.IDENTIFIER, ".")) {
                String receiverName = tokens.get(index).getLiteral();
                receiver = Optional.of(new Ast.Expr.Access(Optional.empty(), receiverName));
                match(Token.Type.IDENTIFIER, ".");
            }
            // mike()
            if (peek(Token.Type.IDENTIFIER, "(")) {
                name = tokens.get(index).getLiteral();
                match(Token.Type.IDENTIFIER, "(");
                if (peek(Token.Type.IDENTIFIER)) {
                    arguments.add(new Ast.Expr.Access(Optional.empty(), tokens.get(index).getLiteral()));
                    tokens.advance();
                }
                while (peek(",", Token.Type.IDENTIFIER)) {
                    tokens.advance();
                    arguments.add(new Ast.Expr.Access(Optional.empty(), tokens.get(index).getLiteral()));
                    tokens.advance();
                }

                if (!match(")")) throw new ParseException("no )", index);
            }
            // mike
            else if (peek(Token.Type.IDENTIFIER)) {
                name = tokens.get(index).getLiteral();
                tokens.advance();
                // need expr!!!!!
                if (peek("=", Token.Type.IDENTIFIER)) {
                    tokens.advance();
                    name2 = tokens.get(index).getLiteral();
                    tokens.advance();
                    return new Ast.Stmt.Assignment(
                            new Ast.Expr.Access(receiver, name),
                            // !!! is empty?
                            new Ast.Expr.Access(Optional.empty(), name2));
                }
            } else {
                throw new ParseException("no name", index);
            }

            if (tokens.get(index - 1).getType() == Token.Type.IDENTIFIER)

                if (!match(";")) throw new ParseException("parseStatement() error", index);

            return new Ast.Stmt.Expression(new Ast.Expr.Function(receiver, name, arguments));
        }

        return new Ast.Stmt.Expression(new Ast.Expr.Function(receiver, name, arguments));
//        Optional<Ast.Expr> receiver = Optional.empty();
//        String name = "";
//        List< Ast.Expr > arguments = new ArrayList<>();
//        Optional<Ast.Expr> receiver2 = Optional.empty();
//        String name2 = "";
//        // Determine if it is an assignment
//        int index = 0;
//
//        while (tokens.has(index)){
//            if (tokens.get(index).getType() == Token.Type.OPERATOR && tokens.get(index).getLiteral() == "."){
//                if (name == ""){
//                    throw new ParseException("begain with \".\", should have receiver before \".\"",index);
//                }
//                receiver = Optional.of(new Ast.Expr.Access(Optional.empty(), name));
//            }
//            else if (tokens.get(index).getType() == Token.Type.OPERATOR && tokens.get(index).getLiteral() == "="){
//                break;
//            }
//            else if (tokens.get(index).getType() == Token.Type.OPERATOR && tokens.get(index).getLiteral() == "("){
//                tokens.advance();
//                while (tokens.has(index) && tokens.get(index).getLiteral() != ")"){
//                    arguments.add(new Ast.Expr.Access(Optional.empty(), tokens.get(index).getLiteral()));
//                    tokens.advance();
//                }
//                if (tokens.has(index) == false || tokens.get(index).getLiteral() != ")"){
//                    throw new ParseException("no )",index);
//                }else{
//                    tokens.advance();
//                    if (tokens.has(index) == false || tokens.get(index).getLiteral() != ";"){
//                        throw new ParseException("no ;",index);
//                    }else{
//                        tokens.advance();
//                    }
//                }
//            }
//            else{
//                name = tokens.get(index).getLiteral();
//            }
//            tokens.advance();
//        }
//        if (tokens.has(index) && tokens.get(index).getLiteral() == "="){
//            while (tokens.has(index)){
//                if (tokens.get(index).getType() == Token.Type.OPERATOR && tokens.get(index).getLiteral() == "."){
//                    if (name2 == ""){
//                        throw new ParseException("2. begain with \".\", should have receiver before \".\"",index);
//                    }
//                    receiver2 = Optional.of(new Ast.Expr.Access(Optional.empty(), name2));
//                }
//                else if (tokens.has(index+1) == false && tokens.get(index).getLiteral() == ";"){
//                    // do nothing
//                }
//                else{
//                    name2 = tokens.get(index).getLiteral();
//                }
//                tokens.advance();
//            }
//            return new Ast.Stmt.Assignment(
//                    new Ast.Expr.Access(receiver, name),
//                    new Ast.Expr.Access(receiver2, name2));
//        }
//
//        return new Ast.Stmt.Expression(new Ast.Expr.Function(receiver, name, arguments));
    }

    /**
     * Parses a declaration statement from the {@code statement} rule. This
     * method should only be called if the next tokens start a declaration
     * statement, aka {@code LET}.
     */
    public Ast.Stmt.Declaration parseDeclarationStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses an if statement from the {@code statement} rule. This method
     * should only be called if the next tokens start an if statement, aka
     * {@code IF}.
     */
    public Ast.Stmt.If parseIfStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a for statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a for statement, aka
     * {@code FOR}.
     */
    public Ast.Stmt.For parseForStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a while statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a while statement, aka
     * {@code WHILE}.
     */
    public Ast.Stmt.While parseWhileStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a return statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a return statement, aka
     * {@code RETURN}.
     */
    public Ast.Stmt.Return parseReturnStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code expression} rule.
     */
    public Ast.Expr parseExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        if (tokens.has(0) == false) {
            throw new ParseException("parseExpression() do not have token", 0);
        }
        int index = tokens.index;
        if (tokens.tokens.size() == 1) {
            if (peek(Token.Type.CHARACTER)) {
                if (tokens.get(0).getLiteral().length() == 2) {
                    return new Ast.Expr.Literal("");
                } else if (tokens.get(0).getLiteral().length() == 3) {
                    return new Ast.Expr.Literal(tokens.get(0).getLiteral().charAt(1));
                } else {
                    throw new ParseException("character number error", 0);
                }
            } else if (peek(Token.Type.DECIMAL)) {
                return new Ast.Expr.Literal(new BigDecimal(tokens.get(0).getLiteral()));
            } else if (peek(Token.Type.IDENTIFIER)) {
                if (tokens.get(0).getLiteral() == "TRUE") {
                    return new Ast.Expr.Literal(Boolean.TRUE);
                } else if (tokens.get(0).getLiteral() == "FALSE") {
                    return new Ast.Expr.Literal(Boolean.FALSE);
                } else if (tokens.get(0).getLiteral() == "TYPE") {
                    return new Ast.Expr.Literal(Boolean.TYPE);
                } else if (tokens.get(0).getLiteral() == "NIL") {
                    return new Ast.Expr.Literal(null);
                } else {
                    return new Ast.Expr.Access(Optional.empty(), tokens.get(0).getLiteral());
                }
            } else if (peek(Token.Type.INTEGER)) {
                return new Ast.Expr.Literal(new BigInteger(tokens.get(0).getLiteral()));
            } else if (peek(Token.Type.OPERATOR)) {
                return new Ast.Expr.Literal(tokens.get(0).getLiteral());
            } else if (peek(Token.Type.STRING)) {
                String str = "";
                if (tokens.get(0).getLiteral().length() == 2) {
                    return new Ast.Expr.Literal("");
                }
                if (tokens.get(0).getLiteral().length() < 2) {
                    throw new ParseException("string less than 2", 0);
                }
                for (int i = 1; i < tokens.get(0).getLiteral().length() - 1; i++) {
                    if (tokens.get(0).getLiteral().charAt(i) == '\\') {
                        char ch = tokens.get(0).getLiteral().charAt(i + 1);
                        if (ch == 'n') {
                            str += '\n';
                        } else if (ch == 'b') {
                            str += '\b';
                        } else if (ch == 'r') {
                            str += '\r';
                        } else if (ch == 't') {
                            str += '\t';
                        } else if (ch == '\'') {
                            str += '\'';
                        } else if (ch == '\"') {
                            str += '\"';
                        } else if (ch == '\\') {
                            str += '\\';
                        }
                        i++;
                        continue;
                    }
                    str += tokens.get(0).getLiteral().charAt(i);
                }
                return new Ast.Expr.Literal(str);
            } else {
                throw new ParseException("parseExpression() Literal do not have this type token", 0);
            }
        } else {
            // group
            if (peek("(", Token.Type.IDENTIFIER, ")")) {
                tokens.advance();
                return new Ast.Expr.Group(new Ast.Expr.Access(Optional.empty(), tokens.get(index).getLiteral()));
            }
            // !!! Is that just the case?
            else if (peek("(", Token.Type.IDENTIFIER, "+", Token.Type.IDENTIFIER, ")")) {
                tokens.advance();
                String name1 = tokens.get(index).getLiteral();
                tokens.advance();
                tokens.advance();
                String name2 = tokens.get(index).getLiteral();
                return new Ast.Expr.Group(new Ast.Expr.Binary("+",
                        new Ast.Expr.Access(Optional.empty(), name1),
                        new Ast.Expr.Access(Optional.empty(), name2)));
            }
            // BinaryExpression
            else if (peek(Token.Type.IDENTIFIER, "AND", Token.Type.IDENTIFIER)) {
                String name1 = tokens.get(index).getLiteral();
                tokens.advance();
                tokens.advance();
                String name2 = tokens.get(index).getLiteral();
                return new Ast.Expr.Binary("AND",
                        new Ast.Expr.Access(Optional.empty(), name1),
                        new Ast.Expr.Access(Optional.empty(), name2));
            }else if (peek(Token.Type.IDENTIFIER, "OR", Token.Type.IDENTIFIER)) {
                String name1 = tokens.get(index).getLiteral();
                tokens.advance();
                tokens.advance();
                String name2 = tokens.get(index).getLiteral();
                return new Ast.Expr.Binary("OR",
                        new Ast.Expr.Access(Optional.empty(), name1),
                        new Ast.Expr.Access(Optional.empty(), name2));
            } else if (peek(Token.Type.IDENTIFIER, "==", Token.Type.IDENTIFIER)) {
                String name1 = tokens.get(index).getLiteral();
                tokens.advance();
                tokens.advance();
                String name2 = tokens.get(index).getLiteral();
                return new Ast.Expr.Binary("==",
                        new Ast.Expr.Access(Optional.empty(), name1),
                        new Ast.Expr.Access(Optional.empty(), name2));
            } else if (peek(Token.Type.IDENTIFIER, "+", Token.Type.IDENTIFIER)) {
                String name1 = tokens.get(index).getLiteral();
                tokens.advance();
                tokens.advance();
                String name2 = tokens.get(index).getLiteral();
                return new Ast.Expr.Binary("+",
                        new Ast.Expr.Access(Optional.empty(), name1),
                        new Ast.Expr.Access(Optional.empty(), name2));
            } else if (peek(Token.Type.IDENTIFIER, "*", Token.Type.IDENTIFIER)) {
                String name1 = tokens.get(index).getLiteral();
                tokens.advance();
                tokens.advance();
                String name2 = tokens.get(index).getLiteral();
                return new Ast.Expr.Binary("*",
                        new Ast.Expr.Access(Optional.empty(), name1),
                        new Ast.Expr.Access(Optional.empty(), name2));
            }else if (peek(Token.Type.IDENTIFIER, "-", Token.Type.IDENTIFIER)) {
                String name1 = tokens.get(index).getLiteral();
                tokens.advance();
                tokens.advance();
                String name2 = tokens.get(index).getLiteral();
                return new Ast.Expr.Binary("-",
                        new Ast.Expr.Access(Optional.empty(), name1),
                        new Ast.Expr.Access(Optional.empty(), name2));
            }else if (peek(Token.Type.IDENTIFIER, "/", Token.Type.IDENTIFIER)) {
                String name1 = tokens.get(index).getLiteral();
                tokens.advance();
                tokens.advance();
                String name2 = tokens.get(index).getLiteral();
                return new Ast.Expr.Binary("/",
                        new Ast.Expr.Access(Optional.empty(), name1),
                        new Ast.Expr.Access(Optional.empty(), name2));
            }
            //AccessExpression
            //!!! only have one time '.'
            else if (peek(Token.Type.IDENTIFIER,".",Token.Type.IDENTIFIER)) {
                String name1 = tokens.get(index).getLiteral();
                tokens.advance();
                tokens.advance();
                String name2 = tokens.get(index).getLiteral();
                if (match(Token.Type.IDENTIFIER,"(")) {
                    List expr = new ArrayList();
                    if (peek(Token.Type.IDENTIFIER)){
                        expr.add(new Ast.Expr.Access(Optional.empty(), tokens.get(index).getLiteral()));
                        tokens.advance();
                    }
                    while (peek(",",Token.Type.IDENTIFIER)){
                        tokens.advance();
                        expr.add(new Ast.Expr.Access(Optional.empty(), tokens.get(index).getLiteral()));
                        tokens.advance();
                    }
                    if (!peek(")")) throw new ParseException("FunctionExpression() no )",index);
                    return new Ast.Expr.Function(Optional.of(new Ast.Expr.Access(Optional.empty(), name1)), name2, expr);
                }
                return new Ast.Expr.Access(Optional.of(new Ast.Expr.Access(Optional.empty(), name1)), name2);
            }
            //FunctionExpression
            else if (peek(Token.Type.IDENTIFIER,"(")) {
                String name = tokens.get(index).getLiteral();
                tokens.advance();
                tokens.advance();
                List expr = new ArrayList();
                if (peek(Token.Type.IDENTIFIER)){
                    expr.add(new Ast.Expr.Access(Optional.empty(), tokens.get(index).getLiteral()));
                    tokens.advance();
                }
                while (peek(",",Token.Type.IDENTIFIER)){
                    tokens.advance();
                    expr.add(new Ast.Expr.Access(Optional.empty(), tokens.get(index).getLiteral()));
                    tokens.advance();
                }
                if (!peek(")")) throw new ParseException("FunctionExpression() no )",index);
                return new Ast.Expr.Function(Optional.empty(), name, expr);
            }


        }
        return null;


        /*if (tokens.get(0).getType() == Token.Type.CHARACTER){
            if (tokens.get(0).getLiteral().length() == 2){
                return new Ast.Expr.Literal("");
            }
            if (tokens.get(0).getLiteral().length() < 2){
                throw new ParseException("character less than 2",0);
            }
            return new Ast.Expr.Literal(tokens.get(0).getLiteral().charAt(1));
        }
        else if (tokens.get(0).getType() == Token.Type.DECIMAL){
            return new Ast.Expr.Literal(new BigDecimal(tokens.get(0).getLiteral()));
        }
        else if (tokens.get(0).getType() == Token.Type.IDENTIFIER){
            if (tokens.get(0).getLiteral() == "TRUE"){
                return new Ast.Expr.Literal(Boolean.TRUE);
            }
            else if (tokens.get(0).getLiteral() == "FALSE"){
                return new Ast.Expr.Literal(Boolean.FALSE);
            }
            else if (tokens.get(0).getLiteral() == "TYPE"){
                return new Ast.Expr.Literal(Boolean.TYPE);
            }
            else{
                return new Ast.Expr.Access(Optional.empty(), tokens.get(0).getLiteral());
            }
        }
        else if (tokens.get(0).getType() == Token.Type.INTEGER){
            return new Ast.Expr.Literal(new BigInteger(tokens.get(0).getLiteral()));
        }
        else if (tokens.get(0).getType() == Token.Type.OPERATOR){
            return new Ast.Expr.Literal(tokens.get(0).getLiteral());
        }
        else if (tokens.get(0).getType() == Token.Type.STRING){
            String str = "";
            if (tokens.get(0).getLiteral().length() == 2){
                return new Ast.Expr.Literal("");
            }
            if (tokens.get(0).getLiteral().length() < 2){
                throw new ParseException("string less than 2",0);
            }
            for (int i = 1; i < tokens.get(0).getLiteral().length()-1; i++) {
                if (tokens.get(0).getLiteral().charAt(i) == '\\'){
                    char ch = tokens.get(0).getLiteral().charAt(i+1);
                    if (ch == 'n'){
                        str += '\n';
                    }else if (ch == 'b'){
                        str += '\b';
                    }
                    else if (ch == 'r'){
                        str += '\r';
                    }
                    else if (ch == 't'){
                        str += '\t';
                    }else if (ch == '\''){
                        str += '\'';
                    }
                    else if (ch == '\"'){
                        str += '\"';
                    }else if (ch == '\\'){
                        str += '\\';
                    }
                    i++;
                    continue;
                }
                str += tokens.get(0).getLiteral().charAt(i);
            }
            return new Ast.Expr.Literal(str);
        }
        else{
            throw new ParseException("parseExpression() do not have this type token",0);
        }*/
    }

    /**
     * Parses the {@code logical-expression} rule.
     */
    public Ast.Expr parseLogicalExpression() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code equality-expression} rule.
     */
    public Ast.Expr parseEqualityExpression() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code additive-expression} rule.
     */
    public Ast.Expr parseAdditiveExpression() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code multiplicative-expression} rule.
     */
    public Ast.Expr parseMultiplicativeExpression() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code secondary-expression} rule.
     */
    public Ast.Expr parseSecondaryExpression() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code primary-expression} rule. This is the top-level rule
     * for expressions and includes literal values, grouping, variables, and
     * functions. It may be helpful to break these up into other methods but is
     * not strictly necessary.
     */
    public Ast.Expr parsePrimaryExpression() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * As in the lexer, returns {@code true} if the current sequence of tokens
     * matches the given patterns. Unlike the lexer, the pattern is not a regex;
     * instead it is either a {@link Token.Type}, which matches if the token's
     * type is the same, or a {@link String}, which matches if the token's
     * literal is the same.
     * <p>
     * In other words, {@code Token(IDENTIFIER, "literal")} is matched by both
     * {@code peek(Token.Type.IDENTIFIER)} and {@code peek("literal")}.
     */
    private boolean peek(Object... patterns) {
        //throw new UnsupportedOperationException(); //TODO (in lecture)
        for (int i = 0; i < patterns.length; i++) {
            if (!tokens.has(i)) {
                return false;
            } else if (patterns[i] instanceof Token.Type) {
                if (patterns[i] != tokens.get(i).getType()) {
                    return false;
                }
            } else if (patterns[i] instanceof String) {
                if (!patterns[i].equals(tokens.get(i).getLiteral())) {
                    return false;
                }
            } else {
                throw new AssertionError("Invalid pattern object: " + patterns[i].getClass());
            }
        }
        return true;
    }

    /**
     * As in the lexer, returns {@code true} if {@link #peek(Object...)} is true
     * and advances the token stream.
     */
    private boolean match(Object... patterns) {
        //throw new UnsupportedOperationException(); //TODO (in lecture)
        boolean peek = peek(patterns);
        if (peek) {
            for (int i = 0; i < patterns.length; i++) {
                tokens.advance();
            }
        }
        return peek;
    }

    private static final class TokenStream {

        private final List<Token> tokens;
        private int index = 0;

        private TokenStream(List<Token> tokens) {
            this.tokens = tokens;
        }

        /**
         * Returns true if there is a token at index + offset.
         */
        public boolean has(int offset) {
            return index + offset < tokens.size();
        }

        /**
         * Gets the token at index + offset.
         */
        public Token get(int offset) {
            return tokens.get(index + offset);
        }

        /**
         * Advances to the next token, incrementing the index.
         */
        public void advance() {
            index++;
        }

    }

}
