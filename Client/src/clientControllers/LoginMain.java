package clientControllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

/**
 * Entry point for the client-side JavaFX application.
 * This class initializes and displays the login screen used to connect
 * to the parking management system. It loads the corresponding FXML layout
 * and sets up the primary stage.
 * 
 * This class must be launched as a JavaFX application.
 */
public class LoginMain extends Application {

    /**
     * Starts the JavaFX application by loading the login interface.
     *
     * @param primaryStage the primary window for this application
     * @throws Exception if the FXML file cannot be loaded
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/clientUI/login.fxml"));
        primaryStage.setTitle("Login");
        primaryStage.setScene(new Scene(root, 400, 300));
        primaryStage.show();
    }

    /**
     * Launches the JavaFX application.
     *
     * @param args the command-line arguments passed to the application
     */
    public static void main(String[] args) {
    	System.setProperty("prism.allowhidpi", "false");
        launch(args);
    }
}
