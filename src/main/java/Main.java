import soot.Transform;
import soot.PackManager;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        System.err.println("Transform Java class file into extend-cfg");
        PackManager.v().getPack("jtp")
                .add(new Transform("jtp.trans", new ProgramTransformer()));
        String work_dir = System.getProperty("user.dir");
        String classpath = work_dir + File.separator + "jce.jar"
                + File.pathSeparator + work_dir + File.separator + "rt.jar"
                + File.pathSeparator + work_dir + File.separator + "junit-4.3.jar"
                + File.pathSeparator + (args[0] + File.separator + "classes")
                + File.pathSeparator + (args[0] + File.separator + "test-classes")
                + File.pathSeparator + (args[0] + File.separator + "tests");
        soot.Main.main(new String[] {
            "-w",
            "-p", "jtp.trans", "enabled:true",
            "--soot-class-path", classpath, args[1]
        });

    }
}
