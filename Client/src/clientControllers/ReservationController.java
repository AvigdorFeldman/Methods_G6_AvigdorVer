package clientControllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
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

    @FXML private TableView<Reservation> futureReservationsTable;
    @FXML private TableColumn<Reservation,String> colSpot;
    @FXML private TableColumn<Reservation,LocalDate> colDate;
    @FXML private TableColumn<Reservation,String> colStart;
    @FXML private TableColumn<Reservation,String> colEnd;

    /**
     * Initializes the controller.
     * Configures the table columns for displaying future reservations.
     * Called automatically by the JavaFX runtime after FXML loading.
     */
    @FXML
    public void initialize() {
        // configure table columns
        colSpot .setCellValueFactory(new PropertyValueFactory<>("spot"));
        colDate .setCellValueFactory(new PropertyValueFactory<>("date"));
        colStart.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colEnd  .setCellValueFactory(new PropertyValueFactory<>("endTime"));
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
            showAlert(AlertType.ERROR, "Date, start time and end time are required.");
            return false;
        }

        LocalTime startT, endT;
        try {
            startT = LocalTime.parse(start);
            endT   = LocalTime.parse(end);
        } catch (DateTimeParseException ex) {
            showAlert(AlertType.ERROR, "Time must be in HH:MM format.");
            return false;
        }

        // Ensure start < end
        if (!startT.isBefore(endT)) {
            showAlert(AlertType.ERROR, "Start time must be before end time.");
            return false;
        }

        // Combine date & start into a LocalDateTime
        LocalDateTime now            = LocalDateTime.now();
        LocalDateTime reservationStart = LocalDateTime.of(date, startT);

        // Must be at least 24h in the future
        if (reservationStart.isBefore(now.plusHours(24))) {
            showAlert(AlertType.ERROR, "Reservations must be placed at least 24 hours in advance.");
            return false;
        }

        // Must be no more than 7 days ahead
        if (reservationStart.isAfter(now.plusDays(7))) {
            showAlert(AlertType.ERROR, "Reservations cannot be made more than 7 days in advance.");
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
                startTimeField.getText().trim(), endTimeField.getText().trim());
        String payload = String.format("Create Reservation: %s %s %s", datePicker.getValue(),
                startTimeField.getText().trim(), endTimeField.getText().trim());
        if (!ShowAlert.showConfirmation("Confirm Reservation",
                "Are you sure you want to " +payload+" ?")) {
            clearForm();
            return; // user clicked Cancel
        }
        // Wrap <payload, subscriberId> in a SendObject<Integer>
        SendObject<Reservation> req = new SendObject<>(payload, reservation);
        client.sendToServerSafely(req);

        showAlert(AlertType.INFORMATION,
                "Reservation request sent for subscriber #" + sub.getId() + ":\n" + payload);

        clearForm();
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
            if(sendObject.getObj() instanceof String)
                Platform.runLater(() -> showAlert(AlertType.INFORMATION, sendObject.getObjectMessage()+" "+sendObject.getObj()));
            else if(sendObject.getObj() instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<Reservation> list = (List<Reservation>) sendObject.getObj();
                Platform.runLater(() -> futureReservationsTable.getItems().setAll(list));
            }
        } else {
            Platform.runLater(() -> showAlert(AlertType.ERROR, message.toString()));
        }
    }

    /**
     * Displays an alert pop-up to the user.
     *
     * @param type The type of alert (e.g., INFORMATION, ERROR)
     * @param text The message text to display
     */
    protected void showAlert(AlertType type, String text) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(text);
        a.showAndWait();
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