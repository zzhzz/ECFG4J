import soot.*;

import java.io.*;
import java.util.*;


public class Main {
    public static void main(String[] args) throws IOException {
        System.err.println("Transform Java class file into extend-cfg");
        if (args.length < 3){
            System.out.println("Usage: java -jar ECFG4J.jar <output_path> <class_path> <class_name>");
            System.exit(1);
        }

        String class_path = args[1];
        String clazName = args[2];
        String output_dir_path = args[0];
        DataManager manager = DataManager.createManager(output_dir_path);
        manager.addOne(clazName, "*");

        StringBuilder classpath = new StringBuilder(
                System.getenv("JAVA_HOME") + File.separator + "lib" + File.separator + "tools.jar"
                + File.pathSeparator + System.getenv("JAVA_HOME") + File.separator + "jre/lib" + File.separator + "rt.jar"
                + File.pathSeparator + System.getenv("JAVA_HOME") + File.separator + "jre/lib" + File.separator + "jce.jar"
                + File.pathSeparator + class_path
        );

        List<String> Args = new ArrayList<>();
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
