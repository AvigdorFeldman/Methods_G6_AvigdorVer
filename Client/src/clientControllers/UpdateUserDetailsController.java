package clientControllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import logic.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import ocsf.client.*;

/**
 * Controller class for updating subscriber details.
 * Handles form validation, updates subscriber information locally, and sends updated data to the server.
 * Displays feedback to the user based on the server's response.
 */
public class UpdateUserDetailsController extends Controller {

	@FXML
	private TextField nameField;
	@FXML
	private TextField phoneField;
	@FXML
	private TextField emailField;

	/**
     * Handles the update action triggered from the UI.
     * Validates name, phone, and email fields.
     * If validation succeeds, updates the local subscriber object
     * and sends the updated data to the server.
     */
	@FXML
	private void handleUpdate() {
		String name = nameField.getText();
		String phone = phoneField.getText();
		String email = emailField.getText();
		if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
			showAlert(AlertType.ERROR, "ERROR", "please fill every field");
		} else {
			if (!name.matches("[a-zA-Z\\s]+")) {
	            ShowAlert.showAlert("Error", "Name must contain only letters and spaces.", AlertType.ERROR);
	            return;
	        }
	        // Validate phone is a 10-digit number XXX-XXX-XXXX
	        if (!phone.matches("\\d{3}-\\d{3}-\\d{4}")) {
	            ShowAlert.showAlert("Error", "Phone number must be exactly 10 digits, and in this format:XXX-XXX-XXXX", AlertType.ERROR);
	            return;
	        }
	        // Validate email format using regex
	        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
	            ShowAlert.showAlert("Error",
	            		"Email must be of the format: username@domain.tld\n\n"
	            		+ "Examples of valid email:\n"
	            		+ "definitely.not.a.bot@skynet.ai\n"
	            		+ "nobody@nowhere.org\n"
	            		+ "ilove.spam@gmail.com\n"
	            		+ "nobody.important@e.braude.ac.il\n"
	            		, AlertType.ERROR);
	            return;
	        }
			sub.setEmail(email);
			sub.setName(name);
			sub.setPhone(phone);
			client.sendToServerSafely(new SendObject<subscriber>("Update", sub));
		}
	}

	
	/**
     * Populates the text fields with the current subscriber's details.
     * @param sub the {@code subscriber} whose details will be displayed for editing
     */
	public void update(subscriber sub) {
		nameField.setText(sub.getName());
		phoneField.setText(sub.getPhone());
		emailField.setText(sub.getEmail());
	}

	
	/**
     * Displays an alert dialog with the specified type, title, and message.
     *
     * @param type    the {@code AlertType} to display (e.g., ERROR, INFORMATION)
     * @param title   the title of the alert dialog
     * @param message the message content of the alert dialog
     */
	private void showAlert(AlertType type, String title, String message) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	/**
     * Sets the client and subscriber instance, and populates the fields with subscriber data.
     *
     * @param client the {@code BParkClient} instance used for server communication
     * @param sub    the {@code subscriber} to update
     */
	@Override
	public void setClient(BParkClient client, subscriber sub) {
		super.setClient(client, sub);
		update(sub);
	}
	
	
	 /**
     * Handles messages received from the server after an update request.
     * Displays success or error alerts based on the server's response.
     *
     * @param msg the message object received from the server; expected to be a {@code SendObject}
     */
	public void handleServerMessage(Object msg) {
		// If the message is a SendObject, it contains the subscriber object
		if (msg instanceof SendObject<?>) {
			SendObject<?> response = (SendObject<?>) msg;
			if(response.getObjectMessage().equals("Subscriber")) {
				if(((String)response.getObj()).equals("updated successfully"))
					Platform.runLater(()->{ShowAlert.showAlert("Success",(String)response.getObj() , AlertType.INFORMATION);});
				else
					Platform.runLater(()->{ShowAlert.showAlert("Error",(String)response.getObj() , AlertType.ERROR);});
			}
		}
	}
}