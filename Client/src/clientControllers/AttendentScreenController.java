package clientControllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import logic.SendObject;

/**
 * Controller class for the attendant screen in the parking.
 * Provides navigation to reports and subscriber registration screens.
 */
public class AttendentScreenController extends Controller {

	@FXML
	Button openReservationReportButton;
	@FXML
	Button openSubscribersReportButton;
	@FXML
	Button openActiveSessionsReportButton;
	@FXML
	Button registerNewSubscriberButton;
	@FXML
	Button backButton;

	/**
     * Sets a handler to be executed when the back button is pressed
     * @param backHandler a {@code Runnable} defining the action to perform when navigating back
     */
	public void setBackHandler(Runnable backHandler) {
		this.backHandler = backHandler;
	}

	/**
     * Handles the action of opening the reservation report screen.
     * Loads the corresponding UI and sends a request to the server to fetch all reservations.
     */
	@FXML
	public void openReservationReport() {
		try {
			setscreen("Reservations Report", "ViewReservationScreenUI.fxml", "AttendantScreen.fxml", "Attendant Menu",
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
     * Loads the corresponding UI and sends a request to the server to fetch all subscribers.
     */
	@FXML
	public void openSubscribersReport() {
		try {
			setscreen("Subscribers Report", "ViewSubscriberUI.fxml", "AttendantScreen.fxml", "Attendant Menu",
					openSubscribersReportButton);
			// Send request to the server
			SendObject<String> request = new SendObject<>("Get", "all subscribers");
			client.sendToServerSafely(request);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
     * Handles the action of opening the active parking sessions report screen.
     * Loads the corresponding UI and sends a request to the server to fetch all active sessions.
     */
	@FXML
	public void openActiveSessionsReport() {
		try {
			setscreen("Active Sessions Report", "ViewActiveSessionsUI.fxml", "AttendantScreen.fxml", "Attendant Menu",
					openActiveSessionsReportButton);
			// Send request to the server
			SendObject<String> request = new SendObject<>("Get", "all parking sessions");
			client.sendToServerSafely(request);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
     * Handles the action of opening the new subscriber registration screen.
     * Loads the corresponding UI to allow the attendant to register a new subscriber.
     */
	@FXML
	public void registerNewSubscriber() {
		try {
			setscreen("Registration Screen", "RegistrationScreen.fxml", "AttendantScreen.fxml", "Attendant Menu",
					registerNewSubscriberButton);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
     * Handles the back button action to navigate back to the login screen.
     */
	@FXML
	protected void handleBackButton() {
		// swap the TableView scene back to the connect screen
		handleButtonToLogin(backButton);
	}
}