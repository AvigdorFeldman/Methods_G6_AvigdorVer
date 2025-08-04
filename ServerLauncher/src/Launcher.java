
/**
 * Launcher for Server.jar, makes sure scaling doesnt bother
 */
public class Launcher {
    public static void main(String[] args) {
        try {
        	String pathJavaFX = "C:\\Program Files\\javafx-sdk-17.0.16\\lib";
            ProcessBuilder pb = new ProcessBuilder(           	
                "java",
                "-Dprism.allowhidpi=false",
                "--module-path", pathJavaFX,
                "--add-modules", "javafx.controls,javafx.fxml",
                "-jar", "Server.jar"
            );
            pb.inheritIO();
            pb.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}