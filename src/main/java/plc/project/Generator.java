package plc.project;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

public final class Generator implements Ast.Visitor<Void> {

    private final PrintWriter writer;
    private int indent = 0;

    public Generator(PrintWriter writer) {
        this.writer = writer;
    }

    private void print(Object... objects) {
        for (Object object : objects) {
            if (object instanceof Ast) {
                visit((Ast) object);
            } else {
                writer.write(object.toString());
            }
        }
    }

    private void newline(int indent) {
        writer.println();
        for (int i = 0; i < indent; i++) {
            writer.write("    ");
        }
    }

    @Override
    public Void visit(Ast.Source ast) {
        // the class header
        print("public class Main {");
        indent++;
        newline(0);

        // the source's fields
        if (!ast.getFields().isEmpty()) {
            for (int i = 0; i < ast.getFields().size(); i++) {
                newline(indent);
                print(ast.getFields().get(i));
            }
            newline(0);
        }

        // Java's main method
        newline(indent);
        print("public static void main(String[] args) {");
        indent++;
        newline(indent);
        print("System.exit(new Main().main());");
        indent--;
        newline(indent);
        print("}");
        newline(0);

        // the source's methods
        for (int i = 0; i < ast.getMethods().size(); i++) {
            newline(indent);
            print(ast.getMethods().get(i));
            newline(0);
        }

        // finally the closing brace for the class.
        indent--;
        newline(indent);
        print("}");

        return null;
    }

    @Override
    public Void visit(Ast.Field ast) {

        // Generates a field expression
        if (ast.getTypeName().equals("Integer")) {
            print("int");
        } else if (ast.getTypeName().equals("Decimal")) {
            print("double");
        } else if (ast.getTypeName().equals("Boolean")) {
            print("boolean");
        } else if (ast.getTypeName().equals("Character")) {
            print("char");
        } else if (ast.getTypeName().equals("String")) {
            print("String");
        } else if (ast.getTypeName().equals("Comparable")) {
            print("Comparable");
        } else if (ast.getTypeName().equals("IntegerIterable")) {
            print("Iterable<Integer>");
        }
        else if (ast.getTypeName().equals("Any")) {
            print("Object");
        }
        else if (ast.getTypeName().equals("Nil")) {
            print("Void");
        }

        print(" ");         // blank
        print(ast.getName());
        if (ast.getValue().isPresent()) {
            print(" = ");
            print(ast.getValue().get());
        }
        print(";");

        return null;
    }

    @Override
    public Void visit(Ast.Method ast) {
        print(ast.getFunction().getReturnType().getJvmName());
        print(" ");
        print(ast.getName());
        print("(");
        for (int i = 0; i < ast.getParameters().size(); i++) {
            //System.out.println(ast.getParameterTypeNames().get(i));
            //print(ast.getParameterTypeNames().get(i));
            if (ast.getParameterTypeNames().get(i).equals("Integer")) {
                print("int");
            } else if (ast.getParameterTypeNames().get(i).equals("Decimal")) {
                print("double");
            } else if (ast.getParameterTypeNames().get(i).equals("Boolean")) {
                print("boolean");
            } else if (ast.getParameterTypeNames().get(i).equals("Character")) {
                print("char");
            } else if (ast.getParameterTypeNames().get(i).equals("String")) {
                print("String");
            } else if (ast.getParameterTypeNames().get(i).equals("Comparable")) {
                print("Comparable");
            } else if (ast.getParameterTypeNames().get(i).equals("IntegerIterable")) {
                print("Iterable<Integer>");
            }
            else if (ast.getParameterTypeNames().get(i).equals("Any")) {
                print("Object");
            }
            else if (ast.getParameterTypeNames().get(i).equals("Nil")) {
                print("Void");
            }
            print(" ");
            print(ast.getParameters().get(i));
            if (i != ast.getParameters().size() - 1) {
                print(", ");
            }
        }
        print(") {");
        if (!ast.getStatements().isEmpty()) {
            indent++;
            for (int i = 0; i < ast.getStatements().size(); i++) {
                newline(indent);
                print(ast.getStatements().get(i));
            }
            indent--;
            newline(indent);
        }
        print("}");

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Expression ast) {
        print(ast.getExpression());
        print(";");
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Declaration ast) {
        print(ast.getVariable().getType().getJvmName());
        print(" ");
        print(ast.getVariable().getJvmName());
        if (ast.getValue().isPresent()) {
            print(" = ");
            print(ast.getValue().get());
        }
        print(";");

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Assignment ast) {
        print(ast.getReceiver());
        print(" = ");
        print(ast.getValue());
        print(";");

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.If ast) {
        print("if (");
        print(ast.getCondition());
        print(") {");
        indent++;
        for (int i = 0; i < ast.getThenStatements().size(); i++) {
            newline(indent);
            print(ast.getThenStatements().get(i));
        }
        indent--;
        newline(indent);
        print("}");

        if (!ast.getElseStatements().isEmpty()) {
            print(" else {");
            indent++;
            for (int i = 0; i < ast.getElseStatements().size(); i++) {
                newline(indent);
                print(ast.getElseStatements().get(i));
            }
            indent--;
            newline(indent);
            print("}");
        }

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.For ast) {
        print("for (");
        print("int ");
        print(ast.getName());
        print(" : ");
        print(ast.getValue());
        print(") {");
        indent++;
        for (int i = 0; i < ast.getStatements().size(); i++) {
            newline(indent);
            print(ast.getStatements().get(i));
        }
        indent--;
        newline(indent);
        print("}");

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.While ast) {
        print("while (");
        print(ast.getCondition());
        print(") {");
        if (ast.getStatements().isEmpty()) {
            print("}");
        } else {
            indent++;
            for (int i = 0; i < ast.getStatements().size(); i++) {
                newline(indent);
                print(ast.getStatements().get(i));
            }
            indent--;
            newline(indent);
            print("}");
        }

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Return ast) {
        print("return ");
        print(ast.getValue());
        print(";");
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Literal ast) {
        if (ast.getType() == Environment.Type.CHARACTER) {
            print("'");
            print(ast.getLiteral());
            print("'");
        } else if (ast.getType() == Environment.Type.STRING) {
            print("\"");
            print(ast.getLiteral());
            print("\"");
        } else if (ast.getType() == Environment.Type.DECIMAL) {
            BigDecimal temp = BigDecimal.class.cast(ast.getLiteral());
            print(temp.doubleValue());
        } else if (ast.getType() == Environment.Type.INTEGER) {
            BigInteger temp = BigInteger.class.cast(ast.getLiteral());
            print(temp.intValue());
        } else {
            print(ast.getLiteral());
        }
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Group ast) {
        print("(");
        print(ast.getExpression());
        print(")");
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Binary ast) {
        print(ast.getLeft());
        print(" ");
        if (ast.getOperator().equals("AND")) {
            print("&&");
        } else if (ast.getOperator().equals("OR")) {
            print("||");
        } else {
            print(ast.getOperator());
        }
        print(" ");
        print(ast.getRight());
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Access ast) {
        if (ast.getReceiver().isPresent()) {
            print(ast.getReceiver().get());
            print(".");
        }
        print(ast.getVariable().getJvmName());
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Function ast) {
        if (ast.getReceiver().isPresent()) {
            print(ast.getReceiver().get());
            print(".");
        }
        print(ast.getFunction().getJvmName());
        print("(");
        if (!ast.getArguments().isEmpty()) {
            for (int i = 0; i < ast.getArguments().size(); i++) {
                print(ast.getArguments().get(i));
                if (i != ast.getArguments().size() - 1) {
                    print(", ");
                }
            }
        }
        print(")");
        return null;
    }

}
