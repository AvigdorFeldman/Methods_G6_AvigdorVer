package clientControllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import logic.*;
import javafx.scene.control.Label;
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
	@FXML 
	private Label nameWarningLabel;
	@FXML 
	private Label phoneWarningLabel;
	@FXML 
	private Label emailWarningLabel;
	
	/**
	 * Form validation and labels
	 */
	@FXML
	private void initialize(){
		// Name validation
        nameField.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.matches("[a-zA-Z\\s]*")) {
            	nameField.setText(oldText);
                nameField.setStyle("-fx-border-color: red;");
                nameWarningLabel.setText("Name must contain only letters and spaces");
                nameWarningLabel.setVisible(true);
            } else {
                nameField.setStyle(null);
                nameWarningLabel.setVisible(false);
            }
        });
        
        nameField.focusedProperty().addListener((obs, oldFocus, newFocus) -> {
            if (!newFocus) {
                String text = nameField.getText();
                if (text.matches("[a-zA-Z\\s]+")) {
                    nameWarningLabel.setVisible(false);
                }
            }
        });
        
        

        phoneField.textProperty().addListener((obs, oldText, newText) -> {
            // Remove everything except digits
            String digits = newText.replaceAll("[^\\d]", "");

            // Limit to 10 digits
            if (digits.length() > 10) {
                digits = digits.substring(0, 10);
            }

            // Build formatted string
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < digits.length(); i++) {
                if (i == 3 || i == 6) {
                    formatted.append('-');
                }
                formatted.append(digits.charAt(i));
            }

            // Avoid unnecessary setText calls
            if (!phoneField.getText().equals(formatted.toString())) {
                phoneField.setText(formatted.toString());
                phoneField.positionCaret(formatted.length()); // Keep caret at end
            }

            // Show warning if input is not complete
            if (digits.length() < 10) {
                phoneField.setStyle("-fx-border-color: red;");
                phoneWarningLabel.setText("Phone must be 10 digits: XXX-XXX-XXXX");
                phoneWarningLabel.setVisible(true);
            } else {
                phoneField.setStyle(null);
                phoneWarningLabel.setVisible(false);
            }
        });

        phoneField.focusedProperty().addListener((obs, oldFocus, newFocus) -> {
            if (!newFocus) {
                String digits = phoneField.getText().replaceAll("[^\\d]", "");
                if (digits.length() == 10) {
                    phoneWarningLabel.setVisible(false);
                }
            }
        });
        
        // Email validation
        emailField.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.matches("^[\\w.-]*@?[\\w.-]*\\.?[a-zA-Z]{0,}$")) {
            	emailField.setText(oldText);
                emailField.setStyle("-fx-border-color: red;");
                emailWarningLabel.setText("Invalid email format (e.g. user@example.com)");
                emailWarningLabel.setVisible(true);
            } else {
                emailField.setStyle(null);
                emailWarningLabel.setVisible(false);
            }
        });
        
        emailField.focusedProperty().addListener((obs, oldFocus, newFocus) -> {
            if (!newFocus) {
                String text = emailField.getText();
                if (text.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                    emailWarningLabel.setVisible(false);
                }
            }
        });
	}
	
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
			ShowAlert.showAlert("ERROR", "please fill every field",AlertType.ERROR);
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
					Platform.runLater(()->{ShowAlert.showSuccessAlert("Success",(String)response.getObj());});
				else
					Platform.runLater(()->{ShowAlert.showAlert("Error",(String)response.getObj() , AlertType.ERROR);});
			}
		}
	}
}