import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import soot.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException {
        System.err.println("Transform Java class file into extend-cfg");
        PackManager.v().getPack("wjtp")
                .add(new Transform("wjtp.trans", new ProgramTransformer()));


        String work_dir = System.getProperty("user.dir");
        String project_dir = work_dir + File.separator + "projects";
        String csv_dir_path = project_dir + File.separator + "csvdatas";
        String output_dir_path = work_dir + File.separator + "datas";

        File csv_dir = new File(csv_dir_path);
        File[] csv_files = csv_dir.listFiles();

        for(File proj: csv_files){
            String project_name = proj.getName().replaceFirst(".csv", "");
            DataManager manager = DataManager.createManager(output_dir_path, project_name);

            StringBuilder classpath = new StringBuilder(
                    System.getenv("JAVA_HOME") + File.separator + "lib" + File.separator + "tools.jar"
                    + File.pathSeparator + System.getenv("JAVA_HOME") + File.separator + "jre/lib" + File.separator + "rt.jar"
                    + File.pathSeparator + System.getenv("JAVA_HOME") + File.separator + "jre/lib" + File.separator + "jce.jar"
            );
            manager.processCSV(proj);

            File third_dir = new File(project_dir
                    + File.separator +  project_name + File.separator + "lib");
            System.out.println(third_dir);
            File[] files = third_dir.listFiles();
            for(File jarFile: files){
                if(jarFile.isFile() && jarFile.getName().endsWith(".jar")){
                    classpath.append(File.pathSeparator)
                            .append(third_dir.getPath())
                            .append(File.separator)
                            .append(jarFile.getName());
                } else if(jarFile.isDirectory()){
                    File[] subfiles = jarFile.listFiles();
                    for(File subJar: subfiles){
                        if(subJar.isFile() && subJar.getName().endsWith(".jar")){
                            classpath.append(File.pathSeparator)
                                    .append(subJar.getPath());
                        }
                    }
                }
            }


            List<String> Args = new ArrayList<>();

            System.out.println(manager.size());
            List<String> classes = manager.getClassList();
            Args.add("-w");
            Args.add("-p");
            Args.add("wjtp.trans");
            Args.add("enabled:true");
            Args.add("-allow-phantom-refs");
            Args.add("--soot-class-path");
            Args.add(classpath.toString());
            Args.addAll(classes);

            soot.Main.main(Args.toArray(new String[Args.size()]));
            System.exit(0);
        }

    }
}
