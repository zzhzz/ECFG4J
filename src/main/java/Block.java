import com.google.gson.annotations.Expose;
import fj.Hash;
import org.javatuples.Pair;
import polyglot.ast.NewArray;
import soot.*;
import soot.jimple.*;
import soot.jimple.Stmt;
import soot.jimple.internal.*;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.*;
import java.util.stream.Collectors;

public class Block {
    private List<Unit> unitList = new ArrayList<>();

    @Expose
    private Set<Value> def = new HashSet<>(), use = new HashSet<>();
    @Expose
    private Set<String> callees = new HashSet<>();
    @Expose
    private List<String> nodes = new ArrayList<>();
    @Expose
    private List<Pair<Integer, Integer>> edges = new ArrayList<>();
    private ASTNode root;

    @Expose
    Integer ID;

    private String packageName;

    Block(Integer ID){
        this.ID = ID;
        root = new ASTNode("CompoundStmt");
    }

    void setPackageName(String name){
        packageName = name;
    }

    void addUnit(Unit u){
        unitList.add(u);
    }

    Integer size(){
        return unitList.size();
    }

    private void process_invoke(InvokeExpr invokeExpr){
        SootMethod method = invokeExpr.getMethod();
        SootClass claz = method.getDeclaringClass();
        if(claz.getPackageName().equals(packageName)){
            if(!ExtendCFGList.IsMethodExist(method.getName())){
                if(method.hasActiveBody()) {
                    MethodUtils.process_method(method.getActiveBody());
                } else {
                    try {
                        MethodUtils.process_method(method.retrieveActiveBody());
                    } catch (RuntimeException e) {
                        return;
                    }
                }
            }
            callees.add(method.getName());
        }
    }

    private ASTNode process_type(Type type){
        ASTNode typeNode = null;
        if(type instanceof RefType){
            typeNode = new ASTNode("RefType");
            typeNode.addAll(
                Arrays.stream(type.toString().split("[.]+"))
                        .map(s -> s.split("(?=[A-Z])+"))
                        .flatMap(Arrays::stream)
                        .map(ASTNode::new)
                        .collect(Collectors.toList())
            );
        } else if(type instanceof NullType){
            typeNode = new ASTNode("Null");
        } else if(type instanceof BooleanType){
            typeNode = new ASTNode("Boolean");
        } else if(type instanceof IntType){
            typeNode = new ASTNode("Int");
        } else if(type instanceof ArrayType){
            typeNode = new ASTNode("Array");
            Type baseType = ((ArrayType) type).getElementType();
            typeNode.addChild(process_type(baseType));
            typeNode.addChild(new ASTNode("[]"));
        } else if(type instanceof CharType){
            typeNode = new ASTNode("Char");
        } else if(type instanceof FloatType) {
            typeNode = new ASTNode("Float");
        } else if(type instanceof ShortType){
            typeNode = new ASTNode("Short");
        } else if(type instanceof DoubleType) {
            typeNode = new ASTNode("Double");
        } else if(type instanceof ByteType) {
            typeNode = new ASTNode("Byte");
        } else if(type instanceof LongType){
            typeNode = new ASTNode("Long");
        } else {
            System.err.println("process_type unhandled class " + type.getClass());
        }
        return typeNode;
    }

    private ASTNode process_value(Value value){
        ASTNode valueNode = null;
        if(value instanceof BinopExpr){
            valueNode = new ASTNode("BinaryOp");
            BinopExpr expr = (BinopExpr) value;
            String operator = expr.getSymbol();
            ASTNode node1 = process_value(expr.getOp1());
            ASTNode node2 = process_value(expr.getOp2());
            valueNode.addChild(node1);
            valueNode.addChild(new ASTNode(operator));
            valueNode.addChild(node2);
        } else if(value instanceof InvokeExpr) {
            valueNode = new ASTNode("Invoke");
            InvokeExpr expr = (InvokeExpr) value;
            process_invoke(expr);
        } else if(value instanceof Local) {
            valueNode = new ASTNode("Local");
            Local local = (Local) value;
            valueNode.addChild(process_type(local.getType()));
        } else if(value instanceof Constant) {
            valueNode = new ASTNode("Constant");
            ASTNode typeNode = process_type(value.getType());
            if(value instanceof NumericConstant){
                valueNode.addAll(
                    Arrays.stream(value.toString().split(""))
                        .map(ASTNode::new)
                        .collect(Collectors.toList())
                );
            }
            valueNode.addChild(typeNode);
        } else if(value instanceof ParameterRef) {
            valueNode = new ASTNode("Param");
            ParameterRef ref = (ParameterRef) value;
            ASTNode typeNode = process_type(ref.getType());
            valueNode.addChild(typeNode);
        } else if(value instanceof NewExpr) {
            valueNode = new ASTNode("NewExpr");
            NewExpr expr = (NewExpr) value;
            valueNode.addChild(process_type(expr.getType()));
        } else if(value instanceof NewArrayExpr) {
            valueNode = new ASTNode("NewArray");
            NewArrayExpr expr = (NewArrayExpr) value;
            valueNode.addChild(process_type(expr.getType()));
        } else if(value instanceof CaughtExceptionRef) {
            valueNode = new ASTNode("CaughtException");
            CaughtExceptionRef ref = (CaughtExceptionRef) value;
            valueNode.addChild(process_type(ref.getType()));
        } else if(value instanceof UnopExpr) {
            valueNode = new ASTNode("UnaryOp");
            UnopExpr expr = (UnopExpr) value;
            ASTNode opNode = process_value(expr.getOp());
            String operator;
            if(expr instanceof LengthExpr){
                operator = "length";
            } else if(expr instanceof NegExpr){
                operator = "-";
            } else {
                operator = "";
                System.err.println("Unary operator unhandled " + value.getClass());
            }
            valueNode.addChild(new ASTNode(operator));
            valueNode.addChild(opNode);
        } else if(value instanceof CastExpr) {
            valueNode = new ASTNode("CastExpr");
            CastExpr expr = (CastExpr) value;
            ASTNode opNode = process_value(expr.getOp());
            ASTNode type1 = process_type(expr.getType());
            ASTNode type2 = process_type(expr.getCastType());
            valueNode.addChild(type1);
            valueNode.addChild(type2);
            valueNode.addChild(opNode);
        } else if(value instanceof ArrayRef) {
            valueNode = new ASTNode("ArrayRef");
            ArrayRef ref = (ArrayRef) value;
            ASTNode baseNode = process_value(ref.getBase());
            ASTNode indexNode = process_value(ref.getIndex());
            ASTNode typeNode = process_type(ref.getType());
            valueNode.addChild(typeNode);
            valueNode.addChild(baseNode);
            valueNode.addChild(indexNode);
        } else if(value instanceof ThisRef) {
            valueNode = new ASTNode("ThisRef");
            ThisRef ref = (ThisRef) value;
            valueNode.addChild(process_type(ref.getType()));
        } else if(value instanceof InstanceFieldRef) {
            valueNode = new ASTNode("InstanceFieldRef");
            InstanceFieldRef ref = (InstanceFieldRef) value;
            valueNode.addChild(process_type(ref.getType()));
            valueNode.addChild(process_value(ref.getBase()));
            valueNode.addChild(process_type(ref.getField().getType()));
        } else if(value instanceof StaticFieldRef){
            valueNode = new ASTNode("StaticFieldRef");
            StaticFieldRef ref = (StaticFieldRef) value;
            valueNode.addChild(process_type(ref.getType()));
            valueNode.addChild(process_type(ref.getField().getType()));
        } else if(value instanceof InstanceOfExpr){
            valueNode = new ASTNode("InstanceOfExpr");
            InstanceOfExpr expr = (InstanceOfExpr) value;
            valueNode.addChild(process_type(expr.getType()));
            valueNode.addChild(process_value(expr.getOp()));
            valueNode.addChild(process_type(expr.getCheckType()));
        } else {
            System.err.println("process_value Not handle " + value.getClass());
        }
        return valueNode;
    }

    private ASTNode process_unit(Unit u){
        ASTNode unitNode = null;
        // System.out.println(u);
        if(u instanceof InvokeStmt){
            unitNode = new ASTNode("Invoke");
            InvokeStmt invokeStmt = (InvokeStmt) u;
            process_invoke(invokeStmt.getInvokeExpr());
        } else if(u instanceof IfStmt) {
            unitNode = new ASTNode("IfStmt");
            IfStmt ifStmt = (IfStmt) u;
            Value condition = ifStmt.getCondition();
            ASTNode condNode = process_value(condition);
            unitNode.addChild(condNode);
        } else if(u instanceof GotoStmt){
            unitNode = new ASTNode("GotoStmt");
        } else if(u instanceof ReturnStmt){
            unitNode = new ASTNode("ReturnStmt");
            ReturnStmt returnStmt = (ReturnStmt) u;
            Value op = returnStmt.getOp();
            ASTNode opNode = process_value(op);
            unitNode.addChild(opNode);
        } else if(u instanceof ReturnVoidStmt){
            unitNode = new ASTNode("ReturnVoidStmt");
        } else if(u instanceof DefinitionStmt) {
            unitNode = new ASTNode("DefinitionStmt");
            DefinitionStmt definitionStmt = (DefinitionStmt) u;
            Value leftop = definitionStmt.getLeftOp();
            Value rightop = definitionStmt.getRightOp();
            ASTNode leftopNode = process_value(leftop);
            ASTNode rightopNode = process_value(rightop);
            unitNode.addChild(leftopNode);
            unitNode.addChild(rightopNode);
        } else if(u instanceof ThrowStmt) {
            unitNode = new ASTNode("ThrowStmt");
            ThrowStmt stmt = (ThrowStmt) u;
            unitNode.addChild(process_value(stmt.getOp()));
        } else if(u instanceof SwitchStmt) {
            unitNode = new ASTNode("SwitchStmt");
            SwitchStmt stmt = (SwitchStmt) u;
            unitNode.addChild(process_value(stmt.getKey()));
        } else if(u instanceof EnterMonitorStmt) {
            unitNode = new ASTNode("EnterMonitorStmt");
            EnterMonitorStmt stmt = (EnterMonitorStmt) u;
            unitNode.addChild(process_value(stmt.getOp()));
        } else if(u instanceof ExitMonitorStmt){
            unitNode = new ASTNode("ExitMonitorStmt");
            ExitMonitorStmt stmt = (ExitMonitorStmt) u;
            unitNode.addChild(process_value(stmt.getOp()));
        } else {
            System.err.println(u.getClass() + " Not handle in process_unit");
        }
        List<ValueBox> defList = u.getDefBoxes();
        List<ValueBox> useList = u.getUseBoxes();
        def.addAll(defList.stream()
                .map(ValueBox::getValue)
                .filter(s -> s instanceof Local || s instanceof Ref)
                .collect(Collectors.toList()));
        use.addAll(useList.stream()
                .map(ValueBox::getValue)
                .filter(s -> s instanceof Local || s instanceof Ref)
                .collect(Collectors.toList()));
        return unitNode;
    }

    private void bfs(){
        Queue<Pair<Integer, ASTNode>> que = new LinkedList<>();
        que.offer(Pair.with(-1, root));
        while(!que.isEmpty()){
            Pair<Integer, ASTNode> state = que.poll();
            Integer parent = state.getValue0();
            ASTNode node = state.getValue1();
            Integer node_id = nodes.size();
            if(parent >= 0){
                edges.add(Pair.with(parent, node_id));
            }
            nodes.add(node.getLabel());
            List<ASTNode> childrens = node.getChildrens();
            for(ASTNode child : childrens){
                que.offer(Pair.with(node_id, child));
            }
        }
    }

    void transToAST(){
        for(Unit u: unitList){
            ASTNode node = process_unit(u);
            root.addChild(node);
        }
        bfs();
    }

    public String toString(){
        return "Block " + ID + " content \n" + nodes.toString();
    }
}
