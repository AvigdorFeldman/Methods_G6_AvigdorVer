package clientControllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import logic.SendObject;

/**
 * Controller class for the admin screen.
 * Allows administrators to navigate to different reports, including reservation, subscriber, and active session reports.
 */
public class AdminScreenController extends Controller{
	
	@FXML Button openReservationReportButton;
	@FXML Button openSubscribersReportButton;
	@FXML Button openActiveSessionsReportButton;
	@FXML Button backButton;
    
	
	/**
     * Sets a handler to execute when the back button is pressed.
     * @param backHandler a {@code Runnable} specifying the action to perform when navigating back
     */
	public void setBackHandler(Runnable backHandler) {
		this.backHandler = backHandler;
	}
	
	
	/**
     * Handles the action of opening the reservation report screen.
     * Loads the corresponding UI and sends a request to the server to retrieve all reservations.
     */
	@FXML
	public void openReservationReport() {
		try {
			setscreen("Reservations Report", "ReportReservationScreenUI.fxml", "AdminScreen.fxml", "Admin Menu",
					openReservationReportButton);
			// Send request to the server
			SendObject<String> request = new SendObject<>("Get", "all reservations");
			client.sendToServerSafely(request);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
     * Handles the action of opening the subscribers report screen.
     * Loads the corresponding UI and sends a request to the server to retrieve all subscribers.
     */
	@FXML
	public void openSubscribersReport() {
		try {
			setscreen("Subscribers Report", "ReportSubscriberUI.fxml", "AdminScreen.fxml", "Admin Menu",
					openSubscribersReportButton);
			// Send request to the server
			SendObject<String> request = new SendObject<>("Get", "all subscribers");
			client.sendToServerSafely(request);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
     * Handles the action of opening the active sessions report screen.
     * Loads the corresponding UI and sends a request to the server to retrieve all active parking sessions.
     */
	@FXML
	public void openActiveSessionsReport() {
		try {
			setscreen("Active Sessions Report", "ReportActiveSessionsUI.fxml", "AdminScreen.fxml", "Admin Menu",
					openActiveSessionsReportButton);
			// Send request to the server
			SendObject<String> request = new SendObject<>("Get", "active parking sessions");
			client.sendToServerSafely(request);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
     * Handles the back button action to navigate back to the login screen.
     */
    @FXML protected void handleBackButton() {
		// swap the TableView scene back to the connect screen
    	handleButtonToLogin(backButton);
	}
    
}