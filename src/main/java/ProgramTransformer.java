import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.tagkit.Tag;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;
import soot.util.queue.QueueReader;

import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class ProgramTransformer extends BodyTransformer {

    private String packageName;
    private void process_invoke(InvokeExpr invokeExpr){
        SootMethod method = invokeExpr.getMethod();
        SootClass claz = method.getDeclaringClass();
        if(claz.getPackageName().equals(packageName)){
            Body body = method.retrieveActiveBody();
            UnitGraph graph = new ExceptionalUnitGraph(body);
            for(Unit u : graph){

            }
        }
    }

    private void process_blockUnit(Unit u){
        if(u instanceof InvokeStmt){
            InvokeStmt invokeStmt = (InvokeStmt) u;
            process_invoke(invokeStmt.getInvokeExpr());
        }
        if(u instanceof AssignStmt){
            AssignStmt assignStmt = (AssignStmt) u;
            Value value = assignStmt.getRightOp();
            if(value instanceof InvokeExpr){
                process_invoke((InvokeExpr) value);
            }
        }
        if(u instanceof IdentityStmt) {

        }
        if(u instanceof IfStmt) {

        }
        if(u instanceof GotoStmt){

        }
        if(u instanceof ReturnStmt){

        }
        if(u instanceof ReturnVoidStmt){

        }

    }

    private void process_method(Body body){
        UnitGraph graph = new ExceptionalUnitGraph(body);
        ECFG ecfg = new ECFG();
        for (Unit u : graph) {
            ecfg.appendUnit(u);
        }
        List<Unit> heads = graph.getHeads();
        for(Unit u : heads){
            ecfg.linkEntry(ecfg.FindUnit(u));
        }
        List<Unit> tails = graph.getTails();
        for(Unit u : tails){
            ecfg.linkExit(ecfg.FindUnit(u));
        }
        for (Unit u : graph) {
            Integer x, y, t = 0;
            List<Unit> succs = graph.getSuccsOf(u);
            if (succs.size() == 1) t = 0;
            else if (succs.size() == 2) t = 1;
            else if (succs.size() != 0){
                System.out.println(succs.size());
                System.err.println("Unexpected situation");;
                t = 5;
            }
            x = ecfg.FindUnit(u);
            for(Unit succU : succs){
                y = ecfg.FindUnit(succU);
                ecfg.appendEdge(x, y, t);
                t++;
            }
        }
        ecfg.simplify();
    }


    protected void internalTransform(Body body, String s, Map<String, String> map) {
        String method_name = body.getMethod().getName();
        if(method_name.equals("TestLang747")){
            process_method(body);
        }
    }
}
