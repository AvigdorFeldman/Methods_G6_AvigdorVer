package serverUI;

import java.io.IOException;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ocsf.server.BparkServer;
import serverControllers.ServerController;

/**
 * The class implements the start of the server application, holds the Main
 * method
 */
public class ServerUI extends Application {

	/**
	 * @param args
	 * Starts the application
	 */
	public static void main(String[] args) {
		// 
		launch(args);
	}

	/**
	 * The method gets a Stage, and Loads the FXML, ServerController, sets up the
	 * Initial scene and starts The BParkServer
	 */
	@Override
	public void start(Stage primaryStage) {
		try {
			// Load the FXML layout
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ServerGUI.fxml"));
			AnchorPane root = loader.load();

			// Load the ServerController
			ServerController serverController = loader.getController();

			// Set up the scene
			Scene scene = new Scene(root, 470, 462);
			primaryStage.setScene(scene);
			primaryStage.setTitle("Server Client Connections");
			// Makes sure when X is pressed it closes the server(and port)
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					System.out.println("Closing application...");
					System.gc();
					System.exit(0);
				}
			});
			primaryStage.show();

			// Start the BparkServer
			BparkServer server = new BparkServer(serverController);
			server.listen();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
