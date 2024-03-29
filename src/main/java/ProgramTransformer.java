import org.javatuples.Pair;
import soot.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ProgramTransformer extends SceneTransformer {

    protected void internalTransform(String s, Map<String, String> map) {
        List<Pair<String, String>> dataList = DataManager.getInstance().getClassMethods();

        int c = 0;
        for(Pair<String, String> dataItem: dataList) {
            ExtendCFGList.getInstance().clear();
            String class_name = dataItem.getValue0();
            String method_name = dataItem.getValue1();
            Boolean flag = false;
            if(method_name.equals("*")){
                flag = true;
            }
            System.out.println("Process of " + class_name);
            SootClass claz = Scene.v().getSootClass(class_name);
            Iterator<SootMethod> sootMethodIterator = claz.methodIterator();
            while (sootMethodIterator.hasNext()) {
                SootMethod method = sootMethodIterator.next();
                if (!flag && !method.getName().equals(method_name)){
                    continue;
                }
                if (method.hasActiveBody()) {
                    MethodUtils.process_method(method.getActiveBody());
                } else if (method.isConcrete()) {
                    MethodUtils.process_method(method.retrieveActiveBody());
                }
            }
            System.out.println("function numbers: " + ExtendCFGList.getInstance().size());
            if (ExtendCFGList.getInstance().size() > 0) {
                DataManager.getInstance().saveToJsonFile(class_name, method_name,
                        ExtendCFGList.getInstance().extract());
            } else {
                c++;
            }
        }
        System.out.println(c + " class failed!");
    }


}
