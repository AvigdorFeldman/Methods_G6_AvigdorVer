package clientControllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import logic.Role;
import logic.SendObject;
import logic.subscriber;

/**
 * Controller class for the subscriber registration screen.
 * Handles subscriber input validation, local creation of subscriber objects, and communication with the server
 * to register new subscribers.
 * Displays feedback messages based on server responses.
 */
public class RegistrationController extends Controller{
	
	@FXML
	private Button backButton;
    @FXML
    private TextField idField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField emailField;
    @FXML 
    private Label idWarningLabel;
    @FXML 
    private Label nameWarningLabel;
    @FXML 
    private Label phoneWarningLabel;
    @FXML 
    private Label emailWarningLabel;
	
    
    /**
     * Field validation with popups
     */
    @FXML
    private void initialize() {
        // ID validation
        idField.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.matches("\\d{0,9}")) {
            	idField.setText(oldText);
                idField.setStyle("-fx-border-color: red;");
                idWarningLabel.setText("ID must be numeric (max 9 digits)");
                idWarningLabel.setVisible(true);
            } else {
                idField.setStyle(null);
                idWarningLabel.setVisible(false);
            }
        });
        
        idField.focusedProperty().addListener((obs, oldFocus, newFocus) -> {
            if (!newFocus) { // focus lost
                String text = idField.getText();
                if (text.matches("\\d{9}")) {
                    idWarningLabel.setVisible(false);
                }
            }
        });

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
     * Handles the action of registering a new subscriber.
     * Collects user input, validates fields (ID, name, phone, email), creates a {@code subscriber} object,
     * and sends it to the server for registration. Shows alerts for validation errors or confirmation dialogs.
     */
    @FXML
    public void registerNewSubscriber() {
		// Get subscriber details from the screen
    	String idText = idField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        
		if (name.isEmpty() || email.isEmpty() || idText.isEmpty() || phone.isEmpty()) {
        	ShowAlert.showAlert("Error","One of the fields is empty!.",AlertType.ERROR);
            return;
        }
		
        // Validate ID is a 9-digit number
        if (!idText.matches("\\d{9}")) {
            ShowAlert.showAlert("Error", "ID must be exactly 9 digits.", AlertType.ERROR);
            return;
        }
        // Store ID as an integer so we can use it in the subscriber constructor
        int id = Integer.parseInt(idText);

        // Validate name contains only letters and spaces
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
        /*
        Explanation of the email format:
	        ^ 				Start of the string
	        [\\w.-]+ 		User name matches one or more word characters (a-z, A-Z, 0-9, _), dots ., or dash -
	        @ 				Must contain a literal @
	        [\\w.-]+ 		Domain name (e.g., gmail.com) - same as before
	        \\. 			A literal dot . separating domain and top-level domain
	        [a-zA-Z]{2,} 	Top-level domain (e.g., com, org) - must be at least 2 characters long
	        $ 				End of the string
        */             
        
        // create the subscriber locally
        subscriber newSubscriber = new subscriber(id, name, phone, email, Role.SUBSCRIBER, false, null, "", 0);;
        // send it to server to check if it already exists
        // if it does, the server will return an error message
        // if it doesn't, the server will create the new subscriber and return success
        if (!ShowAlert.showConfirmation("Confirm Subscriber Registration",
				"Are you sure you want to create this subscriber?")) {

			return; // user clicked Cancel
		}
        sendNewSubscriberToServer(newSubscriber);
        
        // clear the screen fields after sending
        nameField.clear();
        emailField.clear();
        idField.clear();
        phoneField.clear();
        idWarningLabel.setVisible(false); 
        nameWarningLabel.setVisible(false);
        phoneWarningLabel.setVisible(false);
        emailWarningLabel.setVisible(false);
    }

    
    /**
     * Sends a new subscriber object to the server for registration.
     * @param newSubscriber the {@code subscriber} object to send to the server
     */
	public void sendNewSubscriberToServer(subscriber newSubscriber) {
        try {
            client.sendToServerSafely(new SendObject<subscriber>("Create new Subscriber", newSubscriber));
        } catch (Exception e) {
            Platform.runLater(() -> ShowAlert.showAlert("Error","Registration failed: " + e.getMessage(),AlertType.ERROR));
            e.printStackTrace();
        }
    }


	/**
     * Handles messages received from the server in response to subscriber registration.
     * Displays success or error alerts depending on the server's response content.
     * @param msg the message object received from the server; can be a {@code SendObject} or {@code String}
     */
	public void handleServerMessage(Object msg) {
		// If the message is a SendObject, it contains the subscriber object
		if (msg instanceof SendObject<?>) {
			SendObject<?> response = (SendObject<?>) msg;
			if ("Subscriber created".equals(response.getObjectMessage())) {
				Object codeAndTag[] = (Object[])response.getObj(); 
				Platform.runLater(() -> {
					ShowAlert.showSuccessAlert("Success", "Subscriber registered successfully!\nSubscriber Log In Code:" + codeAndTag[0] + "\nRFID Tag: " + codeAndTag[1]);});
			}
			else {
				Platform.runLater(() -> ShowAlert.showAlert("Error","Unexpected message type received: " + response.getObjectMessage()+" "+response.getObj(),AlertType.ERROR));
			}
			return;
		}
		// If the message is a String, it indicates a response message
		if (msg instanceof String) {
			String response = (String) msg;
			Platform.runLater(() -> ShowAlert.showAlert("Error",response,AlertType.ERROR));
			return;
		}
	}
}