package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
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
        //throw new UnsupportedOperationException(); //TODO
        if (peek(Token.Type.OPERATOR)) {
            if (tokens.get(0).getIndex() == 0) {
                if (!peek("(")) {
                    try {
                        tokens.get(0);
                    } catch (Exception e) {
                        throw new ParseException("do not start with operator!", 0);
                    }
                    throw new ParseException("do not start with operator!", tokens.get(0).getIndex());
                }
            }
        }

        List fields = new ArrayList();
        List methods = new ArrayList();
        Ast.Field field = null;
        do {
            field = parseField();
            if (field != null) {
                fields.add(field);
            }
        } while (field != null);

        // ???
        if (match("\b") ||
                match("\n") ||
                match("\r") ||
                match("\t") ||
                match("\f") ||
                match("\000B") ||
                match(" ")) {
            // do nothing
        }

        Ast.Method method = null;
        do {
            method = parseMethod();
            if (method != null) {
                methods.add(method);
            }
        } while (method != null);
        return new Ast.Source(fields, methods);
    }

    /**
     * Parses the {@code field} rule. This method should only be called if the
     * next tokens start a field, aka {@code LET}.
     */
    public Ast.Field parseField() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        if (peek("LET", Token.Type.IDENTIFIER)) {
            match("LET");
            String first = tokens.get(0).getLiteral();
            Ast.Expr second = null;
            String type = null;
            match(Token.Type.IDENTIFIER);

            if (match(":")) {
                type = tokens.get(0).getLiteral();
                tokens.advance();
                if (type == null) {
                    try {
                        tokens.get(0);
                    } catch (Exception e) {
                        throw new ParseException("type should not be null", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                    }
                    throw new ParseException("type should not be null", tokens.get(0).getIndex());
                }
            }

            if (match("=")) {
                second = parseExpression();
                if (second == null) {
                    try {
                        tokens.get(0);
                    } catch (Exception e) {
                        throw new ParseException("parseExpression() should not be null", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                    }
                    throw new ParseException("parseExpression() should not be null", tokens.get(0).getIndex());
                }
            }
            if (!match(";")) {
                try {
                    tokens.get(0);
                } catch (Exception e) {
                    throw new ParseException("do not have ;", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                }
                throw new ParseException("do not have ;", tokens.get(0).getIndex());
            }


            return new Ast.Field(first,type, Optional.of(second));
        }
        return null;
    }

    /**
     * Parses the {@code method} rule. This method should only be called if the
     * next tokens start a method, aka {@code DEF}.
     */
    public Ast.Method parseMethod() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        String first = null;
        List<String> second = new ArrayList<>();
        List<String> secondType = new ArrayList<>();
        List<Ast.Stmt> third = new ArrayList<>();

        String type = null;
        if (!match("DEF")) {
            return null;
        }
        if (!peek(Token.Type.IDENTIFIER)) {
            try {
                tokens.get(0);
            } catch (Exception e) {
                throw new ParseException("do not have IDENTIFIER", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
            }
            throw new ParseException("do not have IDENTIFIER", tokens.get(0).getIndex());
        }else{
            first = tokens.get(0).getLiteral();
            match(Token.Type.IDENTIFIER);
        }

        if (!match("(")) {
            try {
                tokens.get(0);
            } catch (Exception e) {
                throw new ParseException("do not have (", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
            }
            throw new ParseException("do not have (", tokens.get(0).getIndex());
        }


            if (peek(Token.Type.IDENTIFIER)) {
                second.add(tokens.get(0).getLiteral());
                match(Token.Type.IDENTIFIER);
                if (match(":")) {
                    String temp = tokens.get(0).getLiteral();
                    secondType.add(temp);
                    tokens.advance();
                    if (temp == null) {
                        try {
                            tokens.get(0);
                        } catch (Exception e) {
                            throw new ParseException("type should not be null", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                        }
                        throw new ParseException("type should not be null", tokens.get(0).getIndex());
                    }
                }
                while (peek(",", Token.Type.IDENTIFIER)) {
                    match(",");
                    second.add(tokens.get(0).getLiteral());
                    match(Token.Type.IDENTIFIER);
                    if (match(":")) {
                        String temp = tokens.get(0).getLiteral();
                        secondType.add(temp);
                        tokens.advance();
                        if (temp == null) {
                            try {
                                tokens.get(0);
                            } catch (Exception e) {
                                throw new ParseException("type should not be null", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                            }
                            throw new ParseException("type should not be null", tokens.get(0).getIndex());
                        }
                    }
                }
            }
            // ???
            if (!match(")")) {
                try {
                    tokens.get(0);
                } catch (Exception e) {
                    throw new ParseException("do not have )", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                }
                throw new ParseException("do not have )", tokens.get(0).getIndex());
            }

            if (match(":")) {
                type = tokens.get(0).getLiteral();
                tokens.advance();
                if (type == null) {
                    try {
                        tokens.get(0);
                    } catch (Exception e) {
                        throw new ParseException("return type should not be null", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                    }
                    throw new ParseException("return type should not be null", tokens.get(0).getIndex());
                }
            }

            if (!match("DO")) {
                try {
                    tokens.get(0);
                } catch (Exception e) {
                    throw new ParseException("do not have DO", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                }
                throw new ParseException("do not have DO", tokens.get(0).getIndex());
            }

            if (!peek("END")){
                Ast.Stmt stmt = parseStatement();
                if (stmt != null) {
                    third.add(stmt);
                }
            }

            if (!match("END")) {
                try {
                    tokens.get(0);
                } catch (Exception e) {
                    throw new ParseException("do not have END", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                }
                throw new ParseException("do not have END", tokens.get(0).getIndex());
            }
            if (type == null){
                return new Ast.Method(first, second, secondType, Optional.empty(), third);
            }else{
                return new Ast.Method(first, second, secondType, Optional.of(type), third);
            }
    }

    /**
     * Parses the {@code statement} rule and delegates to the necessary method.
     * If the next tokens do not start a declaration, if, while, or return
     * statement, then it is an expression/assignment statement.
     */
    public Ast.Stmt parseStatement() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        if (peek(Token.Type.OPERATOR)) {
            if (tokens.get(0).getIndex() == 0) {
                if (!peek("(")) {
                    try {
                        tokens.get(0);
                    } catch (Exception e) {
                        throw new ParseException("do not start with operator!", 0);
                    }
                    throw new ParseException("do not start with operator!", tokens.get(0).getIndex());
                }
            }
        }
        Ast.Stmt.Declaration declaration = parseDeclarationStatement();
        Ast.Stmt.If iff = parseIfStatement();
        Ast.Stmt.For forr = parseForStatement();
        Ast.Stmt.While whilee = parseWhileStatement();
        Ast.Stmt.Return returnn = parseReturnStatement();
        if (declaration != null) return declaration;
        if (iff != null) return iff;
        if (forr != null) return forr;
        if (whilee != null) return whilee;
        if (returnn != null) return returnn;

        Ast.Stmt result;
        Ast.Expr first;
        first = parseExpression();
        result = new Ast.Stmt.Expression(first);
        if (match("=")) {
            Ast.Expr second = parseExpression();
            if (second == null) {
                try {
                    tokens.get(0);
                } catch (Exception e) {
                    throw new ParseException("after = need an expression", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                }
                throw new ParseException("after = need an expression", tokens.get(0).getIndex());
            }
            result = new Ast.Stmt.Assignment(first, second);
        }
        if (!match(";")) {
            try {
                tokens.get(0);
            } catch (Exception e) {
                throw new ParseException("no ;", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
            }
            throw new ParseException("no ;", tokens.get(0).getIndex());
        }
        return result;
    }

    /**
     * Parses a declaration statement from the {@code statement} rule. This
     * method should only be called if the next tokens start a declaration
     * statement, aka {@code LET}.
     */
    public Ast.Stmt.Declaration parseDeclarationStatement() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        if (peek("LET", Token.Type.IDENTIFIER)) {
            match("LET");
            String name = tokens.get(0).getLiteral();
            match(Token.Type.IDENTIFIER);

            String type = null;
            if (match(":")) {
                type = tokens.get(0).getLiteral();
                tokens.advance();
                if (type == null) {
                    try {
                        tokens.get(0);
                    } catch (Exception e) {
                        throw new ParseException("type should not be null", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                    }
                    throw new ParseException("type should not be null", tokens.get(0).getIndex());
                }
            }

            Ast.Expr expr = null;
            if (match("=")) {
                expr = parseExpression();
                if (expr == null) {
                    try {
                        tokens.get(0);
                    } catch (Exception e) {
                        throw new ParseException("parseExpression() should not be null", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                    }
                    throw new ParseException("parseExpression() should not be null", tokens.get(0).getIndex());
                }
            }

            if (!match(";")) {
                try {
                    tokens.get(0);
                } catch (Exception e) {
                    throw new ParseException("no ;", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                }
                throw new ParseException("no ;", tokens.get(0).getIndex());
            }
            if (expr == null) {
                if (type == null){
                    return new Ast.Stmt.Declaration(name, Optional.empty(), Optional.empty());
                }else{
                    return new Ast.Stmt.Declaration(name, Optional.of(type), Optional.empty());
                }
            } else {
                if (type == null){
                    return new Ast.Stmt.Declaration(name, Optional.empty(), Optional.of(expr));
                }
                else{
                    return new Ast.Stmt.Declaration(name, Optional.of(type), Optional.of(expr));
                }
            }
        }
        return null;
    }

    /**
     * Parses an if statement from the {@code statement} rule. This method
     * should only be called if the next tokens start an if statement, aka
     * {@code IF}.
     */
    public Ast.Stmt.If parseIfStatement() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        if (match("IF")) {
            Ast.Expr expr = parseExpression();
            if (expr == null) {
                try {
                    tokens.get(0);
                } catch (Exception e) {
                    throw new ParseException("parseExpression() should not be null", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                }
                throw new ParseException("parseExpression() should not be null", tokens.get(0).getIndex());
            }
            if (!match("DO")) {
                try {
                    tokens.get(0);
                } catch (Exception e) {
                    throw new ParseException("need DO", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                }
                throw new ParseException("need DO", tokens.get(0).getIndex());
            }

            List<Ast.Stmt> stmts1 = new ArrayList<>();
            Ast.Stmt stmt1 = null;
            List<Ast.Stmt> stmts2 = new ArrayList<>();
            Ast.Stmt stmt2 = null;

            do {
                if (peek("END") || peek("ELSE")) {
                    break;
                }
                stmt1 = parseStatement();
                if (stmt1 != null) {
                    stmts1.add(stmt1);
                }
            } while (stmt1 != null && !peek("ELSE") && !peek("END"));


            if (match("ELSE")) {
                do {
                    if (peek("END")) {
                        break;
                    }
                    stmt2 = parseStatement();
                    if (stmt2 != null) {
                        stmts2.add(stmt2);
                    }
                } while (stmt2 != null && !peek("END"));
            }
            if (!match("END")) {
                try {
                    tokens.get(0);
                } catch (Exception e) {
                    throw new ParseException("need END", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                }
                throw new ParseException("need END", tokens.get(0).getIndex());

            }
            return new Ast.Stmt.If(expr, stmts1, stmts2);
        }
        return null;
    }

    /**
     * Parses a for statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a for statement, aka
     * {@code FOR}.
     */
    public Ast.Stmt.For parseForStatement() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        String name = null;
        if (!match("FOR")){
            return null;
        }
        if (peek(Token.Type.IDENTIFIER)) {
            name = tokens.get(0).getLiteral();
            match(Token.Type.IDENTIFIER);
        }else{
            try {
                tokens.get(0);
            } catch (Exception e) {
                throw new ParseException("do not have IDENTIFIER", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
            }
            throw new ParseException("do not have IDENTIFIER", tokens.get(0).getIndex());
        }
        if (!match("IN")){
            try {
                tokens.get(0);
            } catch (Exception e) {
                throw new ParseException("do not have IN", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
            }
            throw new ParseException("do not have IN", tokens.get(0).getIndex());
        }

            Ast.Expr expr = parseExpression();
            if (expr == null) {
                try {
                    tokens.get(0);
                } catch (Exception e) {
                    throw new ParseException("parseExpression() should not be null", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                }
                throw new ParseException("parseExpression() should not be null", tokens.get(0).getIndex());

            }

            if (!match("DO")) {
                try {
                    tokens.get(0);
                } catch (Exception e) {
                    throw new ParseException("need DO", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                }
                throw new ParseException("need DO", tokens.get(0).getIndex());

            }

            List<Ast.Stmt> stmts1 = new ArrayList<>();
            Ast.Stmt stmt1 = null;

            do {
                if (peek("END")) {
                    break;
                }
                stmt1 = parseStatement();
                if (stmt1 != null) {
                    stmts1.add(stmt1);
                }
            } while (stmt1 != null && !peek("END"));

            if (!match("END")) {
                try {
                    tokens.get(0);
                } catch (Exception e) {
                    throw new ParseException("need END", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                }
                throw new ParseException("need END", tokens.get(0).getIndex());

            }
            return new Ast.Stmt.For(name, expr, stmts1);

    }

    /**
     * Parses a while statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a while statement, aka
     * {@code WHILE}.
     */
    public Ast.Stmt.While parseWhileStatement() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        if (match("WHILE")) {

            Ast.Expr expr = parseExpression();
            if (expr == null) {
                try {
                    tokens.get(0);
                } catch (Exception e) {
                    throw new ParseException("parseExpression() should not be null", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                }
                throw new ParseException("parseExpression() should not be null", tokens.get(0).getIndex());

            }

            if (!match("DO")) {
                try {
                    tokens.get(0);
                } catch (Exception e) {
                    throw new ParseException("need DO", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                }
                throw new ParseException("need DO", tokens.get(0).getIndex());

            }

            List<Ast.Stmt> stmts1 = new ArrayList<>();
            Ast.Stmt stmt1 = null;

            do {
                if (peek("END")) {
                    break;
                }
                stmt1 = parseStatement();
                if (stmt1 != null) {
                    stmts1.add(stmt1);
                }
            } while (stmt1 != null && !peek("END"));

            if (!match("END")) {
                try {
                    tokens.get(0);
                } catch (Exception e) {
                    throw new ParseException("need END", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                }
                throw new ParseException("need END", tokens.get(0).getIndex());

            }
            return new Ast.Stmt.While(expr, stmts1);
        }
        return null;
    }

    /**
     * Parses a return statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a return statement, aka
     * {@code RETURN}.
     */
    public Ast.Stmt.Return parseReturnStatement() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        if (match("RETURN")) {
            Ast.Expr expr = parseExpression();
            if (expr == null) {
                try {
                    tokens.get(0);
                } catch (Exception e) {
                    throw new ParseException("parseExpression() should not be null", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                }
                throw new ParseException("parseExpression() should not be null", tokens.get(0).getIndex());

            }

            if (!match(";")) {
                try {
                    tokens.get(0);
                } catch (Exception e) {
                    throw new ParseException("need ;", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                }
                throw new ParseException("need ;", tokens.get(0).getIndex());

            }
            return new Ast.Stmt.Return(expr);
        }
        return null;
    }

    /**
     * Parses the {@code expression} rule.
     */
    public Ast.Expr parseExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        if (peek(Token.Type.OPERATOR)) {
            if (tokens.get(0).getIndex() == 0) {
                if (!peek("(")) {
                    try {
                        tokens.get(0);
                    } catch (Exception e) {
                        throw new ParseException("do not start with operator!", 0);
                    }
                    throw new ParseException("do not start with operator!", tokens.get(0).getIndex());

                }
            }
        }
        return parseLogicalExpression();
    }

    /**
     * Parses the {@code logical-expression} rule.
     */
    public Ast.Expr parseLogicalExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        Ast.Expr first;
        first = parseEqualityExpression();
        while (peek("AND") || peek("OR")) {
            if (match("AND")) {
                Ast.Expr second = parseEqualityExpression();
                if (second == null) {
                    try {
                        tokens.get(0);
                    } catch (Exception e) {
                        throw new ParseException("after AND need an expression", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                    }
                    throw new ParseException("after AND need an expression", tokens.get(0).getIndex());

                }
                first = new Ast.Expr.Binary("AND", first, second);
            } else if (match("OR")) {
                Ast.Expr second = parseEqualityExpression();
                if (second == null) {
                    try {
                        tokens.get(0);
                    } catch (Exception e) {
                        throw new ParseException("after OR need an expression", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                    }
                    throw new ParseException("after OR need an expression", tokens.get(0).getIndex());

                }
                first = new Ast.Expr.Binary("OR", first, second);
            }
        }
        return first;
    }

    /**
     * Parses the {@code equality-expression} rule.
     */
    public Ast.Expr parseEqualityExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        Ast.Expr first;
        first = parseAdditiveExpression();
        while (peek("<")
                || peek("<=")
                || peek(">")
                || peek(">=")
                || peek("==")
                || peek("!=")) {
            if (match("<")) {
                Ast.Expr second = parseAdditiveExpression();
                if (second == null) {
                    try {
                        tokens.get(0);
                    } catch (Exception e) {
                        throw new ParseException("after < need an expression", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                    }
                    throw new ParseException("after < need an expression", tokens.get(0).getIndex());

                }
                first = new Ast.Expr.Binary("<", first, second);
            } else if (match("<=")) {
                Ast.Expr second = parseAdditiveExpression();
                if (second == null) {
                    try {
                        tokens.get(0);
                    } catch (Exception e) {
                        throw new ParseException("after <= need an expression", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                    }
                    throw new ParseException("after <= need an expression", tokens.get(0).getIndex());

                }
                first = new Ast.Expr.Binary("<=", first, second);
            } else if (match(">")) {
                Ast.Expr second = parseAdditiveExpression();
                if (second == null) {
                    try {
                        tokens.get(0);
                    } catch (Exception e) {
                        throw new ParseException("after > need an expression", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                    }
                    throw new ParseException("after > need an expression", tokens.get(0).getIndex());

                }
                first = new Ast.Expr.Binary(">", first, second);
            } else if (match(">=")) {
                Ast.Expr second = parseAdditiveExpression();
                if (second == null) {
                    try {
                        tokens.get(0);
                    } catch (Exception e) {
                        throw new ParseException("after >= need an expression", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                    }
                    throw new ParseException("after >= need an expression", tokens.get(0).getIndex());

                }
                first = new Ast.Expr.Binary(">=", first, second);
            } else if (match("==")) {
                Ast.Expr second = parseAdditiveExpression();
                if (second == null) {
                    try {
                        tokens.get(0);
                    } catch (Exception e) {
                        throw new ParseException("after == need an expression", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                    }
                    throw new ParseException("after == need an expression", tokens.get(0).getIndex());

                }
                first = new Ast.Expr.Binary("==", first, second);
            } else if (match("!=")) {
                Ast.Expr second = parseAdditiveExpression();
                if (second == null) {
                    try {
                        tokens.get(0);
                    } catch (Exception e) {
                        throw new ParseException("after != need an expression", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                    }
                    throw new ParseException("after != need an expression", tokens.get(0).getIndex());

                }
                first = new Ast.Expr.Binary("!=", first, second);
            }
        }
        return first;
    }

    /**
     * Parses the {@code additive-expression} rule.
     */
    public Ast.Expr parseAdditiveExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        Ast.Expr first;
        first = parseMultiplicativeExpression();
        while (peek("+") || peek("-")) {
            if (match("+")) {
                Ast.Expr second = parseMultiplicativeExpression();
                if (second == null) {
                    try {
                        tokens.get(0);
                    } catch (Exception e) {
                        throw new ParseException("after + need an expression", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                    }
                    throw new ParseException("after + need an expression", tokens.get(0).getIndex());

                }
                first = new Ast.Expr.Binary("+", first, second);
            } else if (match("-")) {
                Ast.Expr second = parseMultiplicativeExpression();
                if (second == null) {
                    try {
                        tokens.get(0);
                    } catch (Exception e) {
                        throw new ParseException("after - need an expression", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                    }
                    throw new ParseException("after - need an expression", tokens.get(0).getIndex());

                }
                first = new Ast.Expr.Binary("-", first, second);
            }
        }
        return first;
    }

    /**
     * Parses the {@code multiplicative-expression} rule.
     */
    public Ast.Expr parseMultiplicativeExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        Ast.Expr first;
        first = parseSecondaryExpression();
        while (peek("*") || peek("/")) {
            if (match("*")) {
                Ast.Expr second = parseSecondaryExpression();
                if (second == null) {
                    try {
                        tokens.get(0);
                    } catch (Exception e) {
                        throw new ParseException("after * need an expression", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                    }
                    throw new ParseException("after * need an expression", tokens.get(0).getIndex());

                }
                first = new Ast.Expr.Binary("*", first, second);
            } else if (match("/")) {
                Ast.Expr second = parseSecondaryExpression();
                if (second == null) {
                    try {
                        tokens.get(0);
                    } catch (Exception e) {
                        throw new ParseException("after / need an expression", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                    }
                    throw new ParseException("after / need an expression", tokens.get(0).getIndex());

                }
                first = new Ast.Expr.Binary("/", first, second);
            }
        }
        return first;
    }

    /**
     * Parses the {@code secondary-expression} rule.
     */
    public Ast.Expr parseSecondaryExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        Ast.Expr first;
        first = parsePrimaryExpression();
        while (match(".")) {
            if (peek(Token.Type.IDENTIFIER)) {
                String name = tokens.get(0).getLiteral();
                match(Token.Type.IDENTIFIER);
                if (match("(")) {
                    ArrayList<Ast.Expr> arrays = new ArrayList<Ast.Expr>();
                    Ast.Expr temp = parseExpression();
                    if (temp != null) {
                        arrays.add(temp);
                    }
                    while (match(",")) {
                        temp = parseExpression();
                        if (temp != null) {
                            arrays.add(temp);
                        } else {

                            try {
                                tokens.get(0);
                            } catch (Exception e) {
                                throw new ParseException("after , need an expression", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                            }
                            throw new ParseException("after , need an expression", tokens.get(0).getIndex());


                        }
                    }
                    if (!match(")")) {

                        try {
                            tokens.get(0);
                        } catch (Exception e) {
                            throw new ParseException("parsePrimaryExpression(): do not have )", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                        }
                        throw new ParseException("parsePrimaryExpression(): do not have )", tokens.get(0).getIndex());


                    }
                    first = new Ast.Expr.Function(Optional.of(first), name, arrays);
                } else {
                    first = new Ast.Expr.Access(Optional.of(first), name);
                }
            } else {

                try {
                    tokens.get(0);
                } catch (Exception e) {
                    throw new ParseException("after . need an identifier", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                }
                throw new ParseException("after . need an identifier", tokens.get(0).getIndex());


            }
        }
        return first;
    }

    /**
     * Parses the {@code primary-expression} rule. This is the top-level rule
     * for expressions and includes literal values, grouping, variables, and
     * functions. It may be helpful to break these up into other methods but is
     * not strictly necessary.
     */
    public Ast.Expr parsePrimaryExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        if (match("NIL")) {
            return new Ast.Expr.Literal(null);
        } else if (match("TRUE")) {
            return new Ast.Expr.Literal(Boolean.TRUE);
        } else if (match("FALSE")) {
            return new Ast.Expr.Literal(Boolean.FALSE);
        } else if (peek(Token.Type.INTEGER)) {
            Ast.Expr result = new Ast.Expr.Literal(new BigInteger(tokens.get(0).getLiteral()));
            match(Token.Type.INTEGER);
            return result;
        } else if (peek(Token.Type.DECIMAL)) {
            Ast.Expr result = new Ast.Expr.Literal(new BigDecimal(tokens.get(0).getLiteral()));
            match(Token.Type.DECIMAL);
            return result;
        } else if (peek(Token.Type.CHARACTER)) {
            String name = tokens.get(0).getLiteral();
            char resultName = 0;
            if (name.length() == 2) {
                match(name);
                return new Ast.Expr.Literal("");
            } else if (tokens.get(0).getLiteral().length() == 3) {
                resultName = name.charAt(1);
            } else if (name.charAt(1) == '\\') {
                char ch = name.charAt(2);
                if (ch == 'n') {
                    resultName += '\n';
                } else if (ch == 'b') {
                    resultName += '\b';
                } else if (ch == 'r') {
                    resultName += '\r';
                } else if (ch == 't') {
                    resultName += '\t';
                } else if (ch == '\'') {
                    resultName += '\'';
                } else if (ch == '\"') {
                    resultName += '\"';
                } else if (ch == '\\') {
                    resultName += '\\';
                } else {

                    try {
                        tokens.get(0);
                    } catch (Exception e) {
                        throw new ParseException("just one \\", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                    }
                    throw new ParseException("just one \\", tokens.get(0).getIndex());


                }
            } else {
                throw new ParseException("character number error", 0);
            }
            Ast.Expr result = new Ast.Expr.Literal(resultName);
            match(name);
            return result;
        } else if (peek(Token.Type.STRING)) {
            String name = tokens.get(0).getLiteral();
            String str = "";
            if (name.length() == 2) {
                match(name);
                return new Ast.Expr.Literal("");
            }
            if (tokens.get(0).getLiteral().length() < 2) {
                throw new ParseException("string less than 2", 0);
            }
            for (int i = 1; i < name.length() - 1; i++) {
                if (name.charAt(i) == '\\') {
                    char ch = name.charAt(i + 1);
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
                    } else {

                        try {
                            tokens.get(0);
                        } catch (Exception e) {
                            throw new ParseException("just one \\", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                        }
                        throw new ParseException("just one \\", tokens.get(0).getIndex());


                    }
                    i++;
                    continue;
                }
                str += name.charAt(i);
            }
            match(name);
            return new Ast.Expr.Literal(str);
        } else if (match("(")) {
            if (peek(")")) {
                try {
                    tokens.get(0);
                } catch (Exception e) {
                    throw new ParseException("parsePrimaryExpression(): do not have inside things", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                }
                throw new ParseException("parsePrimaryExpression(): do not have inside things", tokens.get(0).getIndex());

            }
            Ast.Expr result = new Ast.Expr.Group(parseExpression());
            if (!match(")")) {
                try {
                    tokens.get(0);
                } catch (Exception e) {
                    throw new ParseException("parsePrimaryExpression(): do not have )", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                }
                throw new ParseException("parsePrimaryExpression(): do not have )", tokens.get(0).getIndex());

            }
            return result;
        } else if (peek(Token.Type.IDENTIFIER)) {
            String name = tokens.get(0).getLiteral();
            match(Token.Type.IDENTIFIER);
            if (match("(")) {
                ArrayList<Ast.Expr> arrays = new ArrayList<Ast.Expr>();
                Ast.Expr temp = parseExpression();
                if (temp != null) {
                    arrays.add(temp);
                }
                while (match(",")) {
                    temp = parseExpression();
                    if (temp != null) {
                        arrays.add(temp);
                    } else {

                        try {
                            tokens.get(0);
                        } catch (Exception e) {
                            throw new ParseException("after , need an expression", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                        }
                        throw new ParseException("after , need an expression", tokens.get(0).getIndex());


                    }
                }
                if (!match(")")) {

                    try {
                        tokens.get(0);
                    } catch (Exception e) {
                        throw new ParseException("parsePrimaryExpression(): do not have )", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                    }
                    throw new ParseException("parsePrimaryExpression(): do not have )", tokens.get(0).getIndex());


                }
                return new Ast.Expr.Function(Optional.empty(), name, arrays);
            } else {
                return new Ast.Expr.Access(Optional.empty(), name);
            }
        } else if (peek(")")) {
            return null;
        } else if (peek(Token.Type.OPERATOR)) {

            try {
                tokens.get(0);
            } catch (Exception e) {
                throw new ParseException("parsePrimaryExpression(): Should not have this OPERATOR", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
            }
            throw new ParseException("parsePrimaryExpression(): Should not have this OPERATOR", tokens.get(0).getIndex());


        }
        return null;
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
