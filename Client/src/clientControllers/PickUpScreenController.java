package clientControllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import logic.*;
import ocsf.client.*;

/**
 * Controller class for the pickup screen in the client-side parking management system.
 *
 * Handles user interactions for vehicle pickup using a parking code and coordinates
 * with the {@link ParkingController} to verify codes, handle lost code recovery,
 * and provide feedback through the UI.
 */
public class PickUpScreenController extends Controller{

	private boolean serverConnection = true;
	private ParkingController parkingController = new ParkingController();
	@FXML
	TextField parkingCode;

	/**
	 * Initializes the text field to allow only numeric input of up to 6 digits.
     * 
     * Called automatically by the JavaFX framework after the FXML is loaded.
	 */
	@FXML
	public void initialize() {
		parkingCode.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d{0,6}")) {
				parkingCode.setText(oldValue); // Revert to old value if invalid
			}
		});
	}
	
	/**
	 * Initializes this controller with the specified client and subscriber.
     * 
     * Also sets the context for the internal {@link ParkingController}.
     *
     * @param client the client instance connected to the server
     * @param sub the subscriber using the application
	 */
	@Override
	public void setClient(BParkClient client, subscriber sub) {
		super.setClient(client, sub);
		parkingController.setClient(client, sub);
		parkingController.setPickUpScreen(this);
	}

	/**
     * Handles the "Lost Code" request by sending the parking code to the user's
     * email and phone via the server.
     * 
     * Displays confirmation and success/error popups based on the result.
	 */
	@FXML
	public void handleLostCodeRequest() {
		parkingCode.setText("");
		try {
			if (!ShowAlert.showConfirmation("Send Parking code",
					"Are you sure you want to send the code to\nEmail: " +sub.getEmail()+"\nPhone: "+sub.getPhone())) {
				return; // user clicked Cancel
			}
			parkingController.handleLostCode();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			ShowAlert.showAlert("Error", "Server communication failure.", AlertType.ERROR);
			e.printStackTrace();
			serverConnection = false;
		}
		if (serverConnection)
			ShowAlert.showAlert("Code sent!", "Code was sent to Email and phone", Alert.AlertType.INFORMATION);
	}

	/**
	 * Submits the entered parking code to the server for verification.
     * 
     * Validates the input and confirms submission. Displays appropriate
     * popups for success or error.
	 */
	@FXML
	public void submitParkingCode() {
		
		String code = parkingCode.getText().trim();
		if (code.length() < 6) {
			ShowAlert.showAlert("Error", "Enter a 6 digit code!", Alert.AlertType.ERROR);
		} else {
			if (!ShowAlert.showConfirmation("Confirm Parking Code submit",
					"Are you sure you want to submit the code " +code+"?")) {
				parkingCode.setText("");
				return; // user clicked Cancel
			}
			parkingCode.setText("");
			try {
				int parkingCodeInt = Integer.parseInt(code);
				parkingController.requestCarPickUp(parkingCodeInt);
				if (serverConnection) {
					showPickUpSuccess();
				}
			} catch (Exception e) {
				ShowAlert.showAlert("Error", e.getMessage(), AlertType.ERROR);
				serverConnection = false;
			}
		}
	}

	/**
	 * Displays a success popup indicating that the vehicle is being prepared for pickup.
	 */
	public void showPickUpSuccess() {
		ShowAlert.showAlert("Success",
				"Pickup Success!\nPlease wait while your vehicle moves to the vehicle collection point",
				Alert.AlertType.INFORMATION);
	}

	/**
	 * Displays a warning popup indicating that the user was late to pick up their vehicle.
	 */
	public void showLateArrivalMessage() {
		ShowAlert.showAlert("Late!", "You were late to pick up your vehicle", Alert.AlertType.WARNING);
	}

	/**
	 * Handles incoming messages from the server and delegates processing to the {@link ParkingController}.
     *
     * @param message the message received from the server
	 */
	public void handleServerMessage(Object message) {
		parkingController.handleServerResponse(message);
	}
}
