import com.google.gson.*;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

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

    void registerMethod(String method_name){
        names.add(method_name);
    }

    static void appendECFG(ECFG ecfg){
        ExtendCFGList.getInstance().ecfgList.add(ecfg);
        ExtendCFGList.getInstance().names.add(ecfg.method_name);
    }

    int size(){
        return ecfgList.size();
    }

    void clear(){
        ecfgList.clear();
        names.clear();
    }

    static boolean IsMethodExist(String method_name){
        return ExtendCFGList.getInstance().names.contains(method_name);
    }

    String extract(){
        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        builder.registerTypeAdapter(Pair.class, (JsonSerializer<Pair>) (pair, type, arg) -> {
            JsonArray array = new JsonArray();
            array.add(arg.serialize(pair.getValue0()));
            array.add(arg.serialize(pair.getValue1()));
            return array;
        });

        builder.registerTypeAdapter(Triplet.class, (JsonSerializer<Triplet>) (triplet, type, arg) -> {
            JsonArray array = new JsonArray();
            array.add(arg.serialize(triplet.getValue0()));
            array.add(arg.serialize(triplet.getValue1()));
            array.add(arg.serialize(triplet.getValue2()));
            return array;
        });

        Map<String, Object> item = new HashMap<>();

        item.put("data", ecfgList);

        Gson gson = builder.create();
        return gson.toJson(item);
    }

}
