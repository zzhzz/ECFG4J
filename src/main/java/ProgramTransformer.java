import org.javatuples.Pair;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.util.Chain;
import soot.util.queue.QueueReader;

import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class ProgramTransformer extends SceneTransformer {

    protected void internalTransform(String s, Map<String, String> map) {
        List<Pair<String, String>> dataList = DataManager.getInstance().getClassLabelList();

        for(Pair<String, String> dataItem: dataList){
            ExtendCFGList.getInstance().clear();
            String class_name = dataItem.getValue0(), label = dataItem.getValue1();
            SootClass claz = Scene.v().getSootClass(class_name);
            Iterator<SootMethod>  sootMethodIterator = claz.methodIterator();
            while(sootMethodIterator.hasNext()){
                SootMethod method = sootMethodIterator.next();
                if(method.isConcrete()) {
                    MethodUtils.process_method(method.retrieveActiveBody());
                }
            }
            DataManager.getInstance().saveToJsonFile(class_name, ExtendCFGList.getInstance().extract(label));
        }
    }


}
