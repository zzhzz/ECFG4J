import soot.Body;
import soot.Unit;
import soot.Value;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.DefinitionStmt;
import soot.jimple.IfStmt;
import soot.jimple.SwitchStmt;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.List;

public class MethodUtils {
    static void process_method(Body body){
        UnitGraph graph = new ExceptionalUnitGraph(body);
        ECFG ecfg = new ECFG(body.getMethod().getName());
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
            int x, y, t = 0;
            List<Unit> succs = graph.getSuccsOf(u);
            x = ecfg.FindUnit(u);
            if(u instanceof IfStmt){
                t = 1;
            } else if(u instanceof SwitchStmt) {
                t = 6;
            }
            for(Unit succU : succs){
                int type = t;
                y = ecfg.FindUnit(succU);
                if(succU instanceof DefinitionStmt){
                    Value rightop = ((DefinitionStmt) succU).getRightOp();
                    if(rightop instanceof CaughtExceptionRef){
                        type = 5; // exception flow
                    }
                }
                ecfg.appendEdge(x, y, type);
                if(type == t) {
                    t++;
                }
            }
        }
        ecfg.simplify();
        ExtendCFGList.appendECFG(ecfg);
    }
}
