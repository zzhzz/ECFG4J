import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import soot.*;

import java.io.*;
import java.util.*;


public class Main {
    public static void main(String[] args) throws IOException {
        System.err.println("Transform Java class file into extend-cfg");
        if (args.length < 2){
            System.out.println("Usage: java -jar ECFG4J.jar <output_path> <project_path>");
            System.exit(1);
        }
        InputStream stream = new FileInputStream(new File("method_list.json"));
        JsonReader reader = new JsonReader(new InputStreamReader(stream));
        Gson gson = new GsonBuilder().create();
        reader.beginArray();
        List<String> methodList = new ArrayList<>();
        while(reader.hasNext()){
            methodList.add(gson.fromJson(reader, String.class));
        }
        reader.close();


        String work_dir = System.getProperty("user.dir");
        String output_dir_path = args[0];

        DataManager manager = DataManager.createManager(output_dir_path);
        String class_path = args[1];
        for(String item: methodList){
            String[] items = item.split("::");
            String clazName = items[0], methodName = items[1];
            manager.addOne(clazName, methodName); 
        }
        

        StringBuilder classpath = new StringBuilder(
                System.getenv("JAVA_HOME") + File.separator + "lib" + File.separator + "tools.jar"
                + File.pathSeparator + System.getenv("JAVA_HOME") + File.separator + "jre/lib" + File.separator + "rt.jar"
                + File.pathSeparator + System.getenv("JAVA_HOME") + File.separator + "jre/lib" + File.separator + "jce.jar"
                + File.pathSeparator + class_path
        );

        List<String> Args = new ArrayList<>();
        System.out.println(manager.size());
        List<String> classes = manager.getClassList();
        Args.add("--keep-line-number");
        Args.add("-w");
        Args.add("-p");
        Args.add("wjtp.trans");
        Args.add("enabled:true");
        Args.add("-p");
        Args.add("cg.spark");
        Args.add("enabled:true");
        Args.add("-allow-phantom-refs");
        Args.add("--soot-class-path");
        Args.add(classpath.toString());
        Args.addAll(classes);
        System.out.println(classes);
        PackManager.v().getPack("wjtp")
                .add(new Transform("wjtp.trans", new ProgramTransformer()));
        soot.Main.main(Args.toArray(new String[Args.size()]));
    }
}
