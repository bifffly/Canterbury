package com.bifffly.canterbury.interpreter;

import com.bifffly.canterbury.Canterbury;
import com.bifffly.canterbury.function.Callable;
import com.bifffly.canterbury.function.Function;
import com.bifffly.canterbury.function.Instance;
import com.bifffly.canterbury.function.Struct;
import com.bifffly.canterbury.parser.expr.AssignmentExpr;
import com.bifffly.canterbury.parser.expr.BinaryExpr;
import com.bifffly.canterbury.parser.expr.CallExpr;
import com.bifffly.canterbury.parser.expr.Expr;
import com.bifffly.canterbury.parser.expr.FuncExpr;
import com.bifffly.canterbury.parser.expr.GetExpr;
import com.bifffly.canterbury.parser.expr.GroupingExpr;
import com.bifffly.canterbury.parser.expr.LiteralExpr;
import com.bifffly.canterbury.parser.expr.StructExpr;
import com.bifffly.canterbury.parser.expr.UnaryExpr;
import com.bifffly.canterbury.parser.expr.ExprVisitor;
import com.bifffly.canterbury.parser.expr.VariableExpr;
import com.bifffly.canterbury.parser.stmt.BlockStmt;
import com.bifffly.canterbury.parser.stmt.ExpressionStmt;
import com.bifffly.canterbury.parser.stmt.IfStmt;
import com.bifffly.canterbury.parser.stmt.Stmt;
import com.bifffly.canterbury.parser.stmt.StmtVisitor;
import com.bifffly.canterbury.parser.stmt.WhileStmt;
import com.bifffly.canterbury.tokens.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements ExprVisitor<Object>, StmtVisitor<Object> {
    private final Environment globals = new Environment();
    private Environment env = globals;
    private boolean blockScope = true;

    public Interpreter() {
        globals.define("clock", new Callable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                return (double) System.currentTimeMillis() / 1000;
            }

            @Override
            public String toString() {
                return "<native func clock>";
            }
        });

        globals.define("print", new Callable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                System.out.println(stringify(args.get(0)));
                return null;
            }

            @Override
            public String toString() {
                return "<native func print>";
            }
        });
    }

    public void interpret(List<Stmt> stmts) {
        try {
            for (Stmt stmt : stmts) {
                exec(stmt);
            }
        } catch (RuntimeError e) {
            Canterbury.runtimeError(e);
        }
    }

    private Object eval(Expr expr) {
        return expr.accept(this);
    }

    private Object exec(Stmt stmt) {
        return stmt.accept(this);
    }

    public Object execBlock(List<Stmt> statements, Environment env) {
        Environment parent = this.env;
        Object returnValue = null;
        try {
            this.env = env;

            for (Stmt statement : statements) {
                returnValue = exec(statement);
            }
        } finally {
            this.env = parent;
        }
        return returnValue;
    }

    private String stringify(Object o) {
        if (o == null) {
            return "null";
        }
        if (o instanceof Double) {
            String str = o.toString();
            if (str.endsWith(".0")) {
                str = str.substring(0, str.length() - 2);
            }
            return str;
        }
        return o.toString();
    }

    private boolean bool(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Boolean) {
            return (boolean) o;
        }
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null) {
            return false;
        }
        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) {
            return;
        }
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) {
            return;
        }
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    @Override
    public Object visitAssignmentExpr(AssignmentExpr expr) {
        Object value = eval(expr.getValue());
        env.define(expr.getIdentifier().getLexeme(), value);
        return value;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr expr) {
        Object left = eval(expr.getLeft());
        Object right = eval(expr.getRight());

        switch (expr.getOp().getType()) {
            case PLUS:
            case MINUS:
            case SLASH:
            case STAR:
            case LESSER:
            case LESSER_EQUAL:
            case GREATER:
            case GREATER_EQUAL: checkNumberOperands(expr.getOp(), left, right); break;
        }

        switch (expr.getOp().getType()) {
            case PLUS: return (double) left + (double) right;
            case MINUS: return (double) left - (double) right;
            case SLASH: return (double) left / (double) right;
            case STAR: return (double) left * (double) right;
            case LESSER: return (double) left < (double) right;
            case LESSER_EQUAL: return (double) left <= (double) right;
            case GREATER: return (double) left > (double) right;
            case GREATER_EQUAL: return (double) left >= (double) right;
            case EQUAL: return isEqual(left, right);
            case UNEQUAL: return !isEqual(left, right);
            default: return null;
        }
    }

    @Override
    public Object visitCallExpr(CallExpr expr) {
        Object callee = eval(expr.getCallee());

        List<Object> args = expr.getArgs().stream().map(this::eval).toList();

        if (!(callee instanceof Callable)) {
            throw  new RuntimeError(expr.getBracket(), "Expected callable object.");
        }
        Callable callable = (Callable) callee;
        if (args.size() != callable.arity()) {
            throw new RuntimeError(expr.getBracket(), "Expected " + callable.arity() + "args, received " + args.size() + ".");
        }
        return callable.call(this, args);
    }

    @Override
    public Object visitFuncExpr(FuncExpr expr) {
        return new Function(expr, env.clone());
    }

    @Override
    public Object visitGetExpr(GetExpr expr) {
        Object o = eval(expr.getExpr());
        if (o instanceof Instance instance) {
            return instance.get(expr.getIdentifier());
        }
        throw new RuntimeError(expr.getIdentifier(), "Cannot retrieve property.");
    }

    @Override
    public Object visitGroupingExpr(GroupingExpr expr) {
        return eval(expr.getExpr());
    }

    @Override
    public Object visitLiteralExpr(LiteralExpr expr) {
        return expr.getValue();
    }

    @Override
    public Object visitStructExpr(StructExpr expr) {
        Map<String, Object> attributes = new HashMap<>();
        for (AssignmentExpr assignmentExpr : expr.getBody()) {
            String name = assignmentExpr.getIdentifier().getLexeme();
            Object value = eval(assignmentExpr.getValue());
            attributes.put(name, value);
        }
        return new Struct(expr, attributes);
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr expr) {
        Object o = eval(expr.getExpr());

        switch (expr.getOp().getType()) {
            case BANG: return !bool(o);
            case MINUS: {
                checkNumberOperand(expr.getOp(), expr.getExpr());
                return -(double) o;
            }
            default: return null;
        }
    }

    @Override
    public Object visitVariableExpr(VariableExpr expr) {
        return env.get(expr.getIdentifier());
    }

    @Override
    public Object visitBlockStmt(BlockStmt stmt) {
        Environment blockEnv = env;
        if (blockScope) {
            blockEnv = new Environment(env);
        }
        Object returnValue = execBlock(stmt.getStatements(), blockEnv);
        return returnValue;
    }

    @Override
    public Object visitExpressionStmt(ExpressionStmt stmt) {
        return eval(stmt.getExpr());
    }

    @Override
    public Object visitIfStmt(IfStmt stmt) {
        if (bool(eval(stmt.getCondition()))) {
            return exec(stmt.getThenBranch());
        } else if (stmt.getElseBranch() != null) {
            return exec(stmt.getElseBranch());
        }
        return null;
    }

    @Override
    public Object visitWhileStmt(WhileStmt stmt) {
        Object returnValue = null;
        while (bool(eval(stmt.getCondition()))) {
            blockScope = false;
            returnValue = exec(stmt.getBody());
            blockScope = true;
        }
        return returnValue;
    }
}
