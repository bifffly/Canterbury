package com.bifffly.canterbury.interpreter;

import com.bifffly.canterbury.Canterbury;
import com.bifffly.canterbury.modules.StandardLibrary;
import com.bifffly.canterbury.object.Callable;
import com.bifffly.canterbury.object.Function;
import com.bifffly.canterbury.object.Instance;
import com.bifffly.canterbury.modules.Module;
import com.bifffly.canterbury.object.Struct;
import com.bifffly.canterbury.parser.expr.AssignmentExpr;
import com.bifffly.canterbury.parser.expr.BinaryExpr;
import com.bifffly.canterbury.parser.expr.CallExpr;
import com.bifffly.canterbury.parser.expr.Expr;
import com.bifffly.canterbury.parser.expr.FuncExpr;
import com.bifffly.canterbury.parser.expr.GetExpr;
import com.bifffly.canterbury.parser.expr.GroupingExpr;
import com.bifffly.canterbury.parser.expr.LiteralExpr;
import com.bifffly.canterbury.parser.expr.LogicalExpr;
import com.bifffly.canterbury.parser.expr.SelfExpr;
import com.bifffly.canterbury.parser.expr.StructExpr;
import com.bifffly.canterbury.parser.expr.UnaryExpr;
import com.bifffly.canterbury.parser.expr.ExprVisitor;
import com.bifffly.canterbury.parser.expr.VariableExpr;
import com.bifffly.canterbury.parser.stmt.BlockStmt;
import com.bifffly.canterbury.parser.stmt.ExpressionStmt;
import com.bifffly.canterbury.parser.stmt.IfStmt;
import com.bifffly.canterbury.parser.stmt.ImportStmt;
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
        StandardLibrary stdlib = new StandardLibrary();
        stdlib.getModules().forEach((module) -> globals.define(module.getName(), module));
    }

    public Environment getEnv() {
        return env;
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
        if (o instanceof Module module) {
            return "<module " + module.getName() + ">";
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

    private boolean isIdentical(Object a, Object b) {
        if (a == null) {
            return false;
        }
        if ((a instanceof Double && b instanceof Double)
            || (a instanceof String && b instanceof String)) {
            return a.equals(b);
        }
        return a == b;
    }

    private void checkIntOperand(Token operator, Object operand) {
        if (operand instanceof Double d) {
            if (d == Math.rint(d)) {
                return;
            }
        }
        throw new RuntimeError(operator, "Operand must be an integer.");
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) {
            return;
        }
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkIntOperands(Token operator, Object left, Object right) {
        if (left instanceof Double doubleLeft && right instanceof Double doubleRight) {
            if (doubleLeft == Math.rint(doubleLeft) && doubleRight == Math.rint(doubleRight)) {
                return;
            }
        }
        throw new RuntimeError(operator, "Operands must be integers.");
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
        if (expr.getTarget() instanceof VariableExpr varExpr) {
            env.define(varExpr.getIdentifier().getLexeme(), value);
        } else if (expr.getTarget() instanceof GetExpr get && get.getExpr() instanceof SelfExpr self) {
            ((Instance) env.get(self.getSelf())).define(get.getIdentifier(), value);
        }
        return value;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr expr) {
        Object left = eval(expr.getLeft());
        Object right = eval(expr.getRight());

        switch (expr.getOp().getType()) {
            case BIT_OR:
            case BIT_AND: checkIntOperands(expr.getOp(), left, right); break;
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
            case BIT_OR: return ((Double) left).intValue() | ((Double) right).intValue();
            case BIT_AND: return ((Double) left).intValue() & ((Double) right).intValue();
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
            case IS: return isIdentical(left, right);
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
        if (o instanceof Module module) {
            return module.get(expr.getIdentifier());
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
    public Object visitLogicalExpr(LogicalExpr expr) {
        Object left = eval(expr.getLeft());
        Object right = eval(expr.getRight());

        switch (expr.getOp().getType()) {
            case OR: return bool(left) || bool(right);
            case AND: return bool(left) && bool(right);
            default: return null;
        }
    }

    @Override
    public Object visitSelfExpr(SelfExpr expr) {
        return env.get(expr.getSelf());
    }

    @Override
    public Object visitStructExpr(StructExpr expr) {
        Map<String, Object> attributes = new HashMap<>();
        for (AssignmentExpr assignmentExpr : expr.getBody()) {
            if (assignmentExpr.getTarget() instanceof VariableExpr varExpr) {
                String name = varExpr.getIdentifier().getLexeme();
                Object value = eval(assignmentExpr.getValue());
                attributes.put(name, value);
            }
        }
        return new Struct(expr, attributes);
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr expr) {
        Object o = eval(expr.getExpr());

        switch (expr.getOp().getType()) {
            case BANG: return !bool(o);
            case MINUS: {
                checkNumberOperand(expr.getOp(), o);
                return -(double) o;
            }
            case BIT_NEG:
                checkIntOperand(expr.getOp(), o);
                return ~((Double) o).intValue();
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
    public Object visitImportStmt(ImportStmt stmt) {
        Object o = eval(new VariableExpr(stmt.getModule()));
        if (o instanceof Module module) {
            stmt.getImports().forEach((token) -> {
                env.define(token.getLexeme(), module.get(token));
            });
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
