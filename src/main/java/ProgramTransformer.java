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

    static String packageName;

    protected void internalTransform(Body body, String s, Map<String, String> map) {
        String method_name = body.getMethod().getName();
        packageName = body.getMethod().getDeclaringClass().getPackageName();
        System.out.println("Package is: " + packageName);
        MethodUtils.process_method(body);
    }
}
