import soot.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        System.err.println("Transform Java class file into extend-cfg");
        if (args.length < 3){
            System.out.println("Usage: java -jar ECFG4J.jar <project_path> <class_name> <method_name>");
            System.exit(1);
        }

        String project_dir = args[0], className = args[1], methodName = args[2];
        
        String work_dir = System.getProperty("user.dir");
        String output_dir_path = work_dir + File.separator + "json_datas";

        DataManager manager = DataManager.createManager(output_dir_path);



        StringBuilder classpath = new StringBuilder(
                System.getenv("JAVA_HOME") + File.separator + "lib" + File.separator + "tools.jar"
                + File.pathSeparator + System.getenv("JAVA_HOME") + File.separator + "jre/lib" + File.separator + "rt.jar"
                + File.pathSeparator + System.getenv("JAVA_HOME") + File.separator + "jre/lib" + File.separator + "jce.jar"
                + File.pathSeparator + project_dir + File.separator + "classes"
                + File.pathSeparator + project_dir + File.separator + "tests"
                + File.pathSeparator + work_dir + File.separator + "junit-4.10.jar"
        );



        manager.addOne(className, methodName);

        List<String> Args = new ArrayList<>();
        System.out.println(manager.size());
        List<String> classes = manager.getClassList();
        Args.add("-w");
        Args.add("-p");
        Args.add("wjtp.trans");
        Args.add("enabled:true");
        Args.add("-p");
        Args.add("cg.cha");
        Args.add("apponly:true");
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
