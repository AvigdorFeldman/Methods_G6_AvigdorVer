package clientControllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import logic.*;
import ocsf.client.*;

/**
 * Controller class for the drop-off screen in the client-side parking management system.
 * 
 * Handles user interactions for vehicle drop-off, including parking spot assignment
 * and reservation-based drop-off. Communicates with the {@link ParkingController} to
 * process actions and display feedback to the user via JavaFX components.
 */
public class DropOffScreenController extends Controller {
	
	/**
	 *  Controller responsible for parking-related logic and communication with the server.
	 */
	private ParkingController parkingController = new ParkingController();

	@FXML
	private Label assignedSpotLabel;

	@FXML
	private Label parkingCodeLabel;
	
	@FXML
	private TextField reservationCode;
	
	@FXML
	private Label codeWarningLabel;

	/////////////////////////////////////////////////
	/*----------SET AND INITIALIZE THINGS----------*/
	////////////////////////////////////////////////

	/**
	 * Initializes this controller with the specified client and subscriber information.
     * Also initializes the parking controller with the same context.
     *
     * @param client the client instance connected to the server
     * @param sub the subscriber using the application
	 */
	@Override
	public void setClient(BParkClient client, subscriber sub) {
		this.client = client;
		this.sub = sub;
		parkingController.setClient(client, sub);
		parkingController.setDropOffScreen(this);
	}
	
	/**
	 * Initializes the text field to allow only numeric input of up to 6 digits.
     * 
     * Called automatically by the JavaFX framework after the FXML is loaded.
	 */
	@FXML
	public void initialize() {
		reservationCode.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d{0,6}")) {
				reservationCode.setText(oldValue); // Revert to old value if invalid
				codeWarningLabel.setText("Numeric value up to 6 digits");
				codeWarningLabel.setVisible(true);
			}
			else
				codeWarningLabel.setVisible(false);
		});
	}

	//////////////////////////////////////////////
	/*-------------------ACTIONS----------------*/
	//////////////////////////////////////////////

	/**
	 * Handles the "Confirm Drop Off" action when the user wants to drop off their vehicle.
	 * 
     * If confirmed, requests the {@link ParkingController} to assign a parking spot,
     * generate a parking code, and create a parking session.
	 */
	public void submitParkingRequest() {
		try {
			if (!ShowAlert.showConfirmation("Confirm Vehicle dropoff",
					"Are you sure you want to dropoff your vehicle?")) {
				return; // user clicked Cancel
			}
			parkingController.confirmDropOff();
		} catch (Exception e) { // Errors shown as alerts
			ShowAlert.showAlert("Error", "An error occurred while processing your request: "+e.getMessage(), Alert.AlertType.ERROR);
			e.printStackTrace();
		}
	}
	
	/**
	 * Handles the drop-off process using a reservation code entered by the user.
     * 
     * Validates the reservation code, confirms the action with the user, and then
     * invokes the parking controller to process the reservation.
	 */
	public void submitParkingRequestUsingReservation() {
		String reservationCodeTrimmed = reservationCode.getText().trim();
		if (reservationCodeTrimmed.isEmpty()||!reservationCodeTrimmed.matches("\\d+")) {
            ShowAlert.showAlert("Error", "Code must be a number", AlertType.ERROR);
            return;
        }
		int code =Integer.parseInt(reservationCodeTrimmed);
		try {
			if (!ShowAlert.showConfirmation("Confirm Vehicle dropoff",
					"Are you sure you want to dropoff your vehicle using reservation: "+reservationCodeTrimmed)) {
				return; // user clicked Cancel
			}
			parkingController.implementDropoffUsingReservation(code);
		} catch (Exception e) {// Errors shown as alerts
			ShowAlert.showAlert("Error", "An error occurred while processing your request: "+e.getMessage(), Alert.AlertType.ERROR);
			e.printStackTrace();
		}
	}

	/**
	 * Displays the assigned parking spot in both a label and an information alert.
     *
     * @param spotId the assigned parking spot ID
	 */
	public void displayAssignedSpot(int spotId) {
		assignedSpotLabel.setText("Your parking spot is " + spotId);
	}

	/**
	 * Displays the generated parking code in both a label and an information alert.
     *
     * @param parkingCode the generated parking code
	 */
	public void displayParkingCode(int parkingCode) {
		parkingCodeLabel.setText("Your parking code is " + String.valueOf(parkingCode)); // display the parking code in
																							// the text field
	}

	/**
	 * Displays a success message indicating that the vehicle was successfully parked.
	 */
	public void showParkingSuccess(int spotId, int parkingCode) {
		ShowAlert.showSuccessAlert("Success", "Parking Success! spot assigned successfully!\nYour parking spot is: " + spotId+"\nYour parking code is: " + parkingCode);
	}

	/**
	 * Displays an error message indicating that no parking spots are currently available.
	 */
	public void showNoAvailability() {
		ShowAlert.showAlert("Error", "No parking spots available at the moment.", Alert.AlertType.ERROR);
	}

	/**
	 * Handles server responses by delegating to the {@link ParkingController}.
     *
     * @param message the message received from the server
	 */
	public void handleServerMessage(Object message) {
		parkingController.handleServerResponse(message);
	}
}