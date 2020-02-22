import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.javatuples.Pair;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataManager {
    private static DataManager singleton = null;
    private List<Pair<String, String>> classes = new ArrayList<>();
    private File project_output;

    private DataManager(String output_dir_path, String project_name){
        File project_output = new File(output_dir_path + File.separator + project_name);
        project_output.mkdirs();
        this.project_output = project_output;
    }

    static DataManager createManager(String output_dir_path, String project_name) {
        if(singleton == null) singleton = new DataManager(output_dir_path, project_name);
        return singleton;
    }

    public static DataManager getInstance() {
        return singleton;
    }

    void processCSV(File proj) throws IOException {
        FileReader reader = null;
        CSVParser parser = null;
        try {
            reader = new FileReader(proj);
            parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
            List<CSVRecord> records = parser.getRecords();
            Map<String, Integer> headerMap = parser.getHeaderMap();
            for (CSVRecord record : records) {
                classes.add(Pair.with(record.get(2), record.get(headerMap.get("bug"))));
            }
        } finally {
            parser.close();
            reader.close();
        }
        System.out.println(classes);
    }

    List<String> getClassList(){
        return classes.stream().map(Pair::getValue0).collect(Collectors.toList());
    }

    List<Pair<String, String>> getClassLabelList(){
        return classes;
    }

    void saveToJsonFile(String class_name, String jsonString) {
        try {
            FileWriter writer = new FileWriter(project_output.getPath() + File.separator + class_name + ".json");
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
