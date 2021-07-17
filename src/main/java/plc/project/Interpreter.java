package plc.project;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Interpreter implements Ast.Visitor<Environment.PlcObject> {

    private Scope scope = new Scope(null);

    public Interpreter(Scope parent) {
        scope = new Scope(parent);
        scope.defineFunction("print", 1, args -> {
            System.out.println(args.get(0).getValue());
            return Environment.NIL;
        });
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public Environment.PlcObject visit(Ast.Source ast) {
        // Access to all fields
        for (int i = 0; i < ast.getFields().size(); i++) {
            visit(ast.getFields().get(i));
        }
        // Access to all methods
        for (int i = 0; i < ast.getMethods().size(); i++) {
            visit(ast.getMethods().get(i));
        }

        List<Environment.PlcObject> args = new ArrayList<Environment.PlcObject>();
        return scope.lookupFunction("main", 0).invoke(args);
    }

    @Override
    public Environment.PlcObject visit(Ast.Field ast) {
        if (ast.getValue().isPresent()) {
            scope.defineVariable(ast.getName(), visit(ast.getValue().get()));
            // System.out.println(scope.lookupVariable(ast.getName()));
        } else {
            scope.defineVariable(ast.getName(), Environment.NIL);
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Method ast) {
        // Defines a function in the current scope. The callback function (lambda) should implement some behaviors
        scope.defineFunction(ast.getName(), ast.getParameters().size(), args -> {
            try {
                //Set the scope to be a new child
                scope = new Scope(scope);
                // defineVariable ( parameters ) in scope
                for (String parameter : ast.getParameters()) {
                    for (Environment.PlcObject arg : args) {
                        scope.defineVariable(parameter, arg);
                    }
                }
                // visit statement
                for (Ast.Stmt statement :
                        ast.getStatements()) {
                    visit(statement);
                }
            } catch (Return r) {
                return r.value;
            }
            // restore the scope
            finally {
                scope = scope.getParent();
            }
            return Environment.NIL;
        });
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Expression ast) {
        visit(ast.getExpression());
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Declaration ast) {
        if (ast.getValue().isPresent()) {
            scope.defineVariable(ast.getName(), visit(ast.getValue().get()));
        } else {
            scope.defineVariable(ast.getName(), Environment.NIL);
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Assignment ast) {
        // ensure that the receiver is an Ast.Expr.Access
        if (ast.getReceiver().getClass().equals(Ast.Expr.Access.class)) {
            try {
                scope = new Scope(scope);
                Ast.Expr.Access receiver = ((Ast.Expr.Access) ast.getReceiver());
                if (receiver.getReceiver().isPresent()) {
                    Environment.PlcObject rec = visit(receiver.getReceiver().get());
                    rec.setField(receiver.getName(), visit(ast.getValue()));
                } else {
                    // setField
                    scope.lookupVariable(receiver.getName()).setValue(visit(ast.getValue()));
                }
            } finally {
                scope = scope.getParent();
            }
        } else {
            throw new RuntimeException("Not Access Type");
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.If ast) {
        // Ensure the condition evaluates to a Boolean
        if (requireType(Boolean.class, visit(ast.getCondition()))) {
            try {
                scope = new Scope(scope);
                for (Ast.Stmt stmt : ast.getThenStatements()) {
                    visit(stmt);
                }
            } finally {
                scope = scope.getParent();
            }
        } else if (!requireType(Boolean.class, visit(ast.getCondition()))) {
            try {
                scope = new Scope(scope);
                for (Ast.Stmt stmt : ast.getElseStatements()) {
                    visit(stmt);
                }
            } finally {
                scope = scope.getParent();
            }
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.For ast) {
        // Ensure the value evaluates to an Iterable
        Iterable iter = requireType(Iterable.class, visit(ast.getValue()));

        for (Object elem :
                iter) {
            try {
                scope = new Scope(scope);
                scope.defineVariable(ast.getName(), ((Environment.PlcObject) elem));
                for (Ast.Stmt statement :
                        ast.getStatements()) {
                    visit(statement);
                }
            } finally {
                scope = scope.getParent();
            }
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.While ast) {
        while (requireType(Boolean.class, visit(ast.getCondition()))) {
            try {
                scope = new Scope(scope);
                for (Ast.Stmt stmt : ast.getStatements()) {
                    visit(stmt);
                }
                ast.getStatements().forEach(this::visit);
            } finally {
                scope = scope.getParent();
            }
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Return ast) {
        throw new Return(visit(ast.getValue()));
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Literal ast) {
        if (ast.getLiteral() == null) {
            return Environment.NIL;
        }
        return Environment.create(ast.getLiteral());
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Group ast) {
        return visit(ast.getExpression());
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Binary ast) {
        // get operator
        String operator = ast.getOperator();

        if (operator.equals("AND")) {
            if (requireType(Boolean.class, visit(ast.getLeft())) == requireType(Boolean.class, visit(ast.getRight()))) {
                return visit(ast.getLeft());
            } else {
                return Environment.create(Boolean.FALSE);
            }
        } else if (operator.equals("OR")) {
            if (requireType(Boolean.class, visit(ast.getLeft())) == Boolean.TRUE) {
                return visit(ast.getLeft());
            } else if (requireType(Boolean.class, visit(ast.getRight())) == Boolean.TRUE) {
                return visit(ast.getRight());
            } else {
                return Environment.create(Boolean.FALSE);
            }
        } else if (operator.equals("<") || operator.equals("<=") || operator.equals(">") || operator.equals(">=")) {
            if (visit(ast.getLeft()).getValue() instanceof Comparable && visit(ast.getLeft()).getValue().getClass() == visit(ast.getRight()).getValue().getClass()) {
                int compare;
                Comparable<Object> left = (Comparable<Object>) visit(ast.getLeft()).getValue();
                Comparable<Object> right = (Comparable<Object>) visit(ast.getRight()).getValue();
                compare = left.compareTo(right);

                switch (operator) {
                    case "<":
                        if (compare < 0) {
                            return Environment.create(Boolean.TRUE);
                        } else {
                            return Environment.create(Boolean.FALSE);
                        }
                    case "<=":
                        if (compare <= 0) {
                            return Environment.create(Boolean.TRUE);
                        } else {
                            return Environment.create(Boolean.FALSE);
                        }
                    case ">":
                        if (compare > 0) {
                            return Environment.create(Boolean.TRUE);
                        } else {
                            return Environment.create(Boolean.FALSE);
                        }
                    case ">=":
                        if (compare >= 0) {
                            return Environment.create(Boolean.TRUE);
                        } else {
                            return Environment.create(Boolean.FALSE);
                        }
                }
            }
        } else if (operator.equals("==") || operator.equals("!=")) {
            switch (operator) {
                case "==":
                    if (visit(ast.getLeft()).getValue().equals(visit(ast.getRight()).getValue())) {
                        return Environment.create(Boolean.TRUE);
                    } else {
                        return Environment.create(Boolean.FALSE);
                    }
                case "!=":
                    if (visit(ast.getLeft()).getValue().equals(visit(ast.getRight()).getValue())) {
                        return Environment.create(Boolean.FALSE);
                    } else {
                        return Environment.create(Boolean.TRUE);
                    }
            }
        } else if (operator.equals("+")) {
            // String
            if (visit(ast.getLeft()).getValue().getClass() == String.class || visit(ast.getRight()).getValue().getClass() == String.class) {
                return Environment.create(visit(ast.getLeft()).getValue().toString() + visit(ast.getRight()).getValue().toString());
            }
            // BigInteger/BigDecimal
            else if (visit(ast.getLeft()).getValue().getClass() == BigInteger.class && visit(ast.getLeft()).getValue().getClass() == visit(ast.getRight()).getValue().getClass()) {
                return Environment.create(BigInteger.class.cast(visit(ast.getLeft()).getValue()).add(BigInteger.class.cast(visit(ast.getRight()).getValue())));
            } else if (visit(ast.getLeft()).getValue().getClass() == BigDecimal.class && visit(ast.getLeft()).getValue().getClass() == visit(ast.getRight()).getValue().getClass()) {
                return Environment.create(BigDecimal.class.cast(visit(ast.getLeft()).getValue()).add(BigDecimal.class.cast(visit(ast.getRight()).getValue())));
            } else {
                throw new RuntimeException("Wrong Concat types");
            }
        } else if (operator.equals("-") || operator.equals("*")) {
            if ((visit(ast.getLeft()).getValue().getClass() == BigDecimal.class || visit(ast.getLeft()).getValue().getClass() == BigInteger.class) && visit(ast.getLeft()).getValue().getClass() == visit(ast.getRight()).getValue().getClass()) {
                if (visit(ast.getLeft()).getValue().getClass() == BigInteger.class) {
                    if (operator.equals("*")) {
                        return Environment.create((((BigInteger) visit(ast.getLeft()).getValue())).multiply((((BigInteger) visit(ast.getRight()).getValue()))));
                    } else {
                        return Environment.create((((BigInteger) visit(ast.getLeft()).getValue())).subtract((((BigInteger) visit(ast.getRight()).getValue()))));
                    }
                } else if (visit(ast.getLeft()).getValue().getClass() == BigDecimal.class) {
                    if (operator.equals("*")) {
                        return Environment.create((((BigDecimal) visit(ast.getLeft()).getValue())).multiply(((BigDecimal) visit(ast.getRight()).getValue())));
                    } else {
                        return Environment.create(((BigDecimal) visit(ast.getLeft()).getValue()).subtract(((BigDecimal) visit(ast.getRight()).getValue())));
                    }
                }
            } else {
                throw new RuntimeException("Tried to - or * with wrong type");
            }
        } else if (operator.equals("/")) {
            if ((visit(ast.getLeft()).getValue().getClass() == BigDecimal.class || visit(ast.getLeft()).getValue().getClass() == BigInteger.class) && visit(ast.getLeft()).getValue().getClass() == visit(ast.getRight()).getValue().getClass()) {
                // If the denominator is zero, throw an error.
                if (visit(ast.getRight()).getValue().equals(BigDecimal.ZERO)) {
                    throw new RuntimeException("denominator is zero");
                }
                // BigDecimal, use RoundingMode.HALF_EVEN
                if (visit(ast.getLeft()).getValue().getClass() == BigDecimal.class) {
                    return Environment.create((((BigDecimal) visit(ast.getLeft()).getValue())).divide(((BigDecimal) visit(ast.getRight()).getValue()), RoundingMode.HALF_EVEN));
                } else {
                    return Environment.create(((BigInteger) visit(ast.getLeft()).getValue()).divide(((BigInteger) visit(ast.getRight()).getValue())));
                }
            } else {
                throw new RuntimeException("Tried to / with wrong type");
            }
        }

        throw new RuntimeException("Wrong types");
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Access ast) {
        if (ast.getReceiver().isPresent()) {
            Environment.PlcObject receiver = visit(ast.getReceiver().get());
            return receiver.getField(ast.getName()).getValue();
        }
        return scope.lookupVariable(ast.getName()).getValue();
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Function ast) {
        //  System.out.println("hello args: " + ast.getArguments()); // [Ast.Expr.Literal{literal=Hello, World!}]
        // System.out.println(ast.getReceiver().isPresent());
//        List<Ast.Expr> args = null;
//        Environment.PlcObject res =  Environment.NIL;
//        List<Environment.PlcObject> plc = null;

        if (ast.getReceiver().isPresent()) {
            return visit(ast.getReceiver().get()).callMethod(ast.getName(), ast.getArguments().stream().map(this::visit).collect(Collectors.toList())); //goes to each arg one by one
            //evaluate it and return the result of calling the method
//            args = ast.getArguments();
//            for (int i = 0; i < args.size(); i++) {
//                plc.add(Environment.create(args.get(i)));
//               // plc.add(new Environment.PlcObject(null, args.get(i)));
//            }
//            return res.callMethod(ast.getName(), plc);
        } else {
            return scope.lookupFunction(ast.getName(), ast.getArguments().size()).invoke(ast.getArguments().stream().map(this::visit).collect(Collectors.toList())); //function name.invoke
        }
    }

    /**
     * Helper function to ensure an object is of the appropriate type.
     */
    private static <T> T requireType(Class<T> type, Environment.PlcObject object) {
        if (type.isInstance(object.getValue())) {
            return type.cast(object.getValue());
        } else {
            throw new RuntimeException("Expected type " + type.getName() + ", received " + object.getValue().getClass().getName() + ".");
        }
    }

    /**
     * Exception class for returning values.
     */
    private static class Return extends RuntimeException {

        private final Environment.PlcObject value;

        private Return(Environment.PlcObject value) {
            this.value = value;
        }

    }

}
