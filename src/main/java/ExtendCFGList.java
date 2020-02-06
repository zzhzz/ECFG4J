import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExtendCFGList {

    private static ExtendCFGList singleton = null;
    private List<ECFG> ecfgList = new ArrayList<>();
    private Set<String> names = new HashSet<>();

    private ExtendCFGList(){
    }

    public static ExtendCFGList getInstance() {
        if(singleton == null) singleton = new ExtendCFGList();
        return singleton;
    }

    static void appendECFG(ECFG ecfg){
        ExtendCFGList.getInstance().ecfgList.add(ecfg);
        ExtendCFGList.getInstance().names.add(ecfg.method_name);
    }

    static boolean IsMethodExist(String method_name){
        return ExtendCFGList.getInstance().names.contains(method_name);
    }

}
