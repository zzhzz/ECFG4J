import org.javatuples.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DataManager {
    private static DataManager singleton = null;

    private List<Pair<String, String>> classes = new ArrayList<>();
    private File project_output;

    private DataManager(String output_dir_path){
        File project_output = new File(output_dir_path);
        project_output.mkdirs();
        this.project_output = project_output;
    }

    static DataManager createManager(String output_dir_path) {
        if(singleton == null) singleton = new DataManager(output_dir_path);
        return singleton;
    }

    public static DataManager getInstance() {
        return singleton;
    }

    void addOne(String className, String methodName) {
        classes.add(Pair.with(className, methodName));
    }
    List<String> getClassList(){
        return classes.stream().map(Pair::getValue0).collect(Collectors.toList());
    }
    List<Pair<String, String>> getClassMethods(){
        return classes;
    }
    void saveToJsonFile(String class_name, String method_name, String jsonString) {
        try {
            FileWriter writer = new FileWriter(project_output.getPath() + File.separator +
                    class_name + "_" + method_name + ".json");
            writer.write(jsonString);
            writer.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    int size(){
        return classes.size();
    }

}
