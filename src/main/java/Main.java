import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import soot.Transform;
import soot.PackManager;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        System.err.println("Transform Java class file into extend-cfg");
        PackManager.v().getPack("jtp")
                .add(new Transform("jtp.trans", new ProgramTransformer()));
        String work_dir = System.getProperty("user.dir");
        String project_dir = work_dir + File.separator + "projects";
        String csv_dir_path = project_dir + File.separator + "csvdatas";
        String output_dir_path = work_dir + File.separator + "datas";

        File csv_dir = new File(csv_dir_path);
        File[] csv_files = csv_dir.listFiles();

        FileReader reader = null;
        List<Pair<String,String>> dataList = new ArrayList<>();
        for(File proj: csv_files){
            StringBuilder classpath = new StringBuilder(
                    System.getenv("JAVA_HOME") + File.separator + "lib" + File.separator + "tools.jar"
                    + File.pathSeparator + System.getenv("JAVA_HOME") + File.separator + "jre/lib" + File.separator + "jsse.jar"
                    + File.pathSeparator + System.getenv("JAVA_HOME") + File.separator + "jre/lib" + File.separator + "rt.jar"
                    + File.pathSeparator + System.getenv("JAVA_HOME") + File.separator + "jre/lib" + File.separator + "jce.jar"
                    + File.pathSeparator + System.getenv("JAVA_HOME") + File.separator + "jre/lib" + File.separator + "jfr.jar"
                    + File.pathSeparator + System.getenv("JAVA_HOME") + File.separator + "jre/lib/security" + File.separator + "local_policy.jar"

            );
            reader = new FileReader(proj);
            CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
            List<CSVRecord> records = parser.getRecords();
            Map<String, Integer> headerMap = parser.getHeaderMap();
            for(CSVRecord record: records){
                dataList.add(Pair.with(record.get(2), record.get(headerMap.get("bug"))));
            }
            String project_name = proj.getName().replaceFirst(".csv", "");
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
            File project_output = new File(output_dir_path + File.separator + project_name);
            project_output.mkdirs();

            for(Pair<String, String> item: dataList) {
                System.out.println("Process " + item.getValue0());
                soot.Main.main(new String[]{
                        "-w",
                        "-p", "jtp.trans", "enabled:true",
                        "--soot-class-path", classpath.toString(), item.getValue0()
                });
                String datas = ExtendCFGList.getInstance().extract(item.getValue1());
                FileWriter writer = new FileWriter(project_output.getPath() + File.separator + item.getValue0() + ".json");
                writer.write(datas);
                writer.close();
            }
            System.exit(0);
        }

    }
}
