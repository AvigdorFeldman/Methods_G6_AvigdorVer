package clientControllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import logic.*;
import ocsf.client.*;

/**
 * Controller class for handling reservation operations in the client application.
 * 
 * Manages the UI interactions for creating, validating, submitting, and displaying
 * parking spot reservations.
 */
public class ReservationController extends Controller{

    // *** FXML-injected fields (no spot field any more) ***
    @FXML private DatePicker datePicker;
    @FXML private TextField startTimeField;
    @FXML private TextField endTimeField;
    @FXML private TextField reservationIdField;
    @FXML private Label reservationIdWarningLabel;

    @FXML private TableView<Reservation> futureReservationsTable;
    @FXML private TableColumn<Reservation,Integer> colId;
    @FXML private TableColumn<Reservation,String> colSpot;
    @FXML private TableColumn<Reservation,LocalDate> colDate;
    @FXML private TableColumn<Reservation,String> colStart;
    @FXML private TableColumn<Reservation,String> colEnd;
    
    private Reservation reservation = null;
	@FXML private Label startTimeWarningLabel;
	@FXML private Label endTimeWarningLabel;

    private void setReservation(Reservation r){
    	this.reservation = r;
    	futureReservationsTable.refresh();
    }
    
    /**
     * Initializes the controller.
     * Configures the table columns for displaying future reservations.
     * Called automatically by the JavaFX runtime after FXML loading.
     */
    @FXML
    public void initialize() {
        // configure table columns
    	colId .setCellValueFactory(new PropertyValueFactory<>("Id"));
    	colSpot .setCellValueFactory(new PropertyValueFactory<>("spot"));
        colDate .setCellValueFactory(new PropertyValueFactory<>("date"));
        colStart.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colEnd  .setCellValueFactory(new PropertyValueFactory<>("endTime"));
        
        // Add HH:MM formatting listeners
        addTimeFieldFormatter(startTimeField, startTimeWarningLabel);
        addTimeFieldFormatter(endTimeField,endTimeWarningLabel);
        
        // Handle numeric input for reservation ID field
        reservationIdField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                reservationIdField.setText(oldValue); // Revert to old value if invalid
                reservationIdField.setStyle("-fx-border-color: red;");
                reservationIdWarningLabel.setText("ID must be numeric");
                reservationIdWarningLabel.setVisible(true);
            } else {
                reservationIdWarningLabel.setVisible(false);
            }
        });
        
        // Row highlighting logic
        futureReservationsTable.setRowFactory(tv -> new TableRow<Reservation>() {
            @Override
            protected void updateItem(Reservation item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    if (reservation!=null && item.getId() == reservation.getId()) {
                        setStyle("-fx-background-color: lightgreen;"); // Apply a green background when matching
                    } else {
                        setStyle(""); // Reset style if it doesn't match
                    }
                } else {
                    setStyle(""); // Default style for empty items
                }
            }
        });
    }

    /**
     * Makes sure the input of HH:MM is kept
     * 
     * @param timeField
     */
    private void addTimeFieldFormatter(TextField timeField, Label timeErrorLabel) {
        timeField.textProperty().addListener((obs, oldText, newText) -> {
            // Keep only digits
            String digits = newText.replaceAll("[^\\d]", "");
            if (digits.length() > 4) {
                digits = digits.substring(0, 4);
            }

            // Format to HH:MM
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < digits.length(); i++) {
                if (i == 2) formatted.append(':');
                formatted.append(digits.charAt(i));
            }

            // Avoid recursive loop
            if (!formatted.toString().equals(timeField.getText())) {
                timeField.setText(formatted.toString());
                timeField.positionCaret(formatted.length());
            }
        });

        // Optional: Validate format and value when field loses focus
        timeField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                String value = timeField.getText();
                if (!value.matches("([01]\\d|2[0-3]):[0-5]\\d")) {
                    timeField.setStyle("-fx-border-color: red;");
                    timeField.setStyle("-fx-border-color: red;");  // Add red border
                    timeErrorLabel.setText("Invalid time format!");  // Show error message
                    timeErrorLabel.setVisible(true);  // Make error label visible
                } else {
                    timeField.setStyle(null);  // Reset the border style
                    timeErrorLabel.setVisible(false);  // Hide error label
                }
            }
        });
    }
    
    /**
     * Validates the reservation form input.
     * 
     * Checks that the date is set, times are in HH:MM format,
     * start time is before end time, and the reservation is between
     * 24 hours and 7 days in the future.
     *
     * @return true if the reservation is valid; false otherwise
     */
    protected boolean validateReservation() {
        LocalDate date = datePicker.getValue();
        String start = startTimeField.getText().trim();
        String end   = endTimeField.getText().trim();

        // Basic non‐empty check
        if (date == null || start.isEmpty() || end.isEmpty()) {
        	ShowAlert.showAlert("Validation failed", "Date, start time and end time are required.", AlertType.ERROR);
            return false;
        }

        LocalTime startT, endT;
        try {
            startT = LocalTime.parse(start);
            endT   = LocalTime.parse(end);
        } catch (DateTimeParseException ex) {
        	ShowAlert.showAlert("Validation failed", "Time must be in HH:MM format. hours:0-23, minutes:0-59", AlertType.ERROR);
            return false;
        }

        // Ensure start < end
        if (!startT.isBefore(endT)) {
            ShowAlert.showAlert("Validation failed", "Start time must be before end time.", AlertType.ERROR);
            return false;
        }

        // Combine date & start into a LocalDateTime
        LocalDateTime now            = LocalDateTime.now();
        LocalDateTime reservationStart = LocalDateTime.of(date, startT);

        // Must be at least 24h in the future
        if (reservationStart.isBefore(now.plusHours(24))) {
        	ShowAlert.showAlert("Validation failed", "Reservations must be placed at least 24 hours in advance.", AlertType.ERROR);
            return false;
        }

        // Must be no more than 7 days ahead
        if (reservationStart.isAfter(now.plusDays(7))) {
        	ShowAlert.showAlert("Validation failed", "Reservations cannot be made more than 7 days in advance.", AlertType.ERROR);
            return false;
        }

        return true;
    }

    /**
     * Handles the reserve button action.
     * 
     * Validates the reservation, confirms with the user, sends the reservation request
     * to the server, and refreshes the future reservations table.
     * 
     * 
     * Called by ReservationScreenController#submitReservationRequest()
     */
    protected void onReserve() {
        if (!validateReservation())
            return;
        
        // Build a simple payload like "2025-06-10 09:00 11:00"

        Reservation reservation = new Reservation(0, sub.getId(), datePicker.getValue(),
                startTimeField.getText().trim(), endTimeField.getText().trim(),0);
        String payload = String.format("Create Reservation: %s %s %s", datePicker.getValue(),
                startTimeField.getText().trim(), endTimeField.getText().trim());
        if (!ShowAlert.showConfirmation("Confirm Reservation",
                "Are you sure you want to " +payload+" ?")) {
            return; // user clicked Cancel
        }
        // Wrap <payload, subscriberId> in a SendObject<Integer>
        SendObject<Reservation> req = new SendObject<>(payload, reservation);
        client.sendToServerSafely(req);

        
        getFutureReservationsFor(); // Immediately refresh the table
    }

    /**
     * Handles the cancel button action.
     * 
     * Clears the form input fields.
     * 
     * Called by ReservationScreenController#submitCancellation()
     */
    protected void onCancel() {
        clearForm();
    }

    /**
     * Clears the reservation form fields.
     * Sets the date to today and empties the time fields.
     */
    private void clearForm() {
        datePicker.setValue(LocalDate.now());
        startTimeField.clear();
        endTimeField.clear();
    }

    /**
     * Requests future reservations for the current subscriber from the server.
     * 
     * Sends a command message wrapped in a SendObject.
     * The server is expected to respond with a list of reservations.
     */
    protected void getFutureReservationsFor() {
        // The “command” string can be anything your server expects, for example:
        String command = "GetSubscribersResesrvations";

        // Wrap <command, subscriberId> in a SendObject<Integer>
        SendObject<Integer> req = new SendObject<>(command, sub.getId());
        client.sendToServerSafely(req);
    }

    /**
     * Loads the reservation by id
     */
    @FXML
    public void handleLoadButton() {
        String reservationId = reservationIdField.getText().trim();

        // Validation: Check if the input is a valid number
        if (reservationId.isEmpty() || !reservationId.matches("\\d+")) {
            reservationIdWarningLabel.setText("Please enter a valid reservation ID.");
            reservationIdWarningLabel.setVisible(true);
            return;
        }

        // Send load reservation request to the server
        int reservationIdInt = Integer.parseInt(reservationId);
        SendObject<Integer> req = new SendObject<>("Get reservation with id", reservationIdInt);
        client.sendToServerSafely(req);

        // Clear warning message
        reservationIdWarningLabel.setVisible(false);
    }
    
    /**
     * Deletes reservation by its id
     */
    @FXML
    public void handleDeleteReservationButton() {

        // Validation: Check if the input is a valid number
        if (reservation==null) {
        	ShowAlert.showAlert("Error", "You must choose a valid reservation id", AlertType.ERROR);
            return;
        }

        // Ask for user confirmation before deleting
        if (!ShowAlert.showConfirmation("Confirm Deletion", 
                "Are you sure you want to delete reservation: " + reservation.getId() + "?")) {
            return; // User clicked Cancel
        }

        // Send delete reservation request to the server
        reservation.setStartTime(null);
        Object reservationToSend[] = {reservation.getId(), reservation};
        SendObject<Object[]> req = new SendObject<>("Update", reservationToSend);
        client.sendToServerSafely(req);

        getFutureReservationsFor();
        reservation = null;
    }
    
    /**
     * Entry point for handling all messages received from the server.
     *
     * Processes lists of reservations or server notifications and updates the UI accordingly.
     *
     * @param message The message object received from the server
     */
    public void handleServerMessage(Object message) {
        if (message instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<Reservation> list = (List<Reservation>) message;
            Platform.runLater(() -> futureReservationsTable.getItems().setAll(list));
        } else if (message instanceof SendObject<?>) {
            SendObject<?> sendObject = (SendObject<?>)message;
            if(sendObject.getObj() instanceof String) {
            	if(((String)sendObject.getObj()).contains("Not")||((String)sendObject.getObjectMessage()).contains("Error"))
            		Platform.runLater(() -> ShowAlert.showAlert("Failed", sendObject.getObjectMessage()+" "+sendObject.getObj(), AlertType.ERROR));
            	else {
            		Platform.runLater(() -> ShowAlert.showSuccessAlert("Success", sendObject.getObjectMessage()+" "+sendObject.getObj()));
            		clearForm();
            		if(!reservationIdField.getText().trim().isEmpty()) {
            			reservationIdField.clear();
            			reservationIdWarningLabel.setVisible(false);
            		}
            	}
            }
            else if(sendObject.getObj() instanceof List<?>) {
            		@SuppressWarnings("unchecked")
            		List<Reservation> list = (List<Reservation>) sendObject.getObj();        		
            		Platform.runLater(() -> futureReservationsTable.getItems().setAll(list));
            }
            else if(sendObject.getObj() instanceof Reservation) {
            		setReservation((Reservation)sendObject.getObj());
            		if(reservation==null||reservation.getSubscriberId()!=sub.getId()||reservation.getStartTime()==null) {
            			Platform.runLater(() -> {reservationIdWarningLabel.setText("");
            			reservationIdWarningLabel.setVisible(false);
            			futureReservationsTable.refresh();
            			ShowAlert.showAlert("Failed", "No such reservation", AlertType.ERROR);
            			reservation=null;});
            		}else {            			
            			Platform.runLater(() ->{reservationIdWarningLabel.setText("loaded");reservationIdWarningLabel.setVisible(true);
            			futureReservationsTable.refresh();});    	
            		}
            	}
        } else {
            Platform.runLater(() -> ShowAlert.showAlert("Unknown Failure",message.toString(), AlertType.ERROR));
        }
    }


    /**
     * Sets the client and subscriber objects for this controller.
     *
     * Also immediately requests the future reservations for this subscriber.
     *
     * @param client The BParkClient instance
     * @param sub The subscriber object
     */
    @Override
    public void setClient(BParkClient client, subscriber sub) {
        super.setClient(client, sub); 
        getFutureReservationsFor();
    }
}