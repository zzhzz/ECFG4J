import soot.Transform;
import soot.PackManager;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        System.err.println("Transform Java class file into extend-cfg");
        PackManager.v().getPack("jtp")
                .add(new Transform("jtp.trans", new ProgramTransformer()));
        String work_dir = System.getProperty("user.dir");
        StringBuilder classpath = new StringBuilder(work_dir + File.separator + "jce.jar"
                + File.pathSeparator + work_dir + File.separator + "rt.jar"
                + File.pathSeparator + work_dir + File.separator + "junit-4.3.jar");
        File third_dir = new File(args[0] + File.separator + "lib");
        File[] files = third_dir.listFiles();
        for(File jarFile: files){
            if(jarFile.isFile() && jarFile.getName().endsWith(".jar")){
                classpath.append(File.pathSeparator)
                        .append(third_dir.getPath())
                        .append(File.separator)
                        .append(jarFile.getName());
            }
        }
        soot.Main.main(new String[] {
            "-w",
            "-p", "jtp.trans", "enabled:true",
            "--soot-class-path", classpath.toString(), args[1]
        });

    }
}
