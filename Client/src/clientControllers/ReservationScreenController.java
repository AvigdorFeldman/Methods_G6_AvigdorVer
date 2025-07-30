package clientControllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * UI-level controller for Reservation.fxml.
 * 
 * Delegates all of the real work to ReservationController. Handles button clicks from
 * the Reservation screen and calls the corresponding business logic in the superclass.
 */
public class ReservationScreenController extends ReservationController{

    // (These fx:id’s match what’s already in your Reservation.fxml)
    /** Reserve button wired via FXML. */
    @FXML private Button reserveButton;

    /** Cancel button wired via FXML. */
    @FXML private Button cancelButton;

    /**
     * Handles the Reserve button click event.
     * 
     * Called automatically by JavaFX when the user clicks “Reserve”.
     * Delegates to onReserve() in the superclass to perform validation,
     * send the request to the server, and show alerts.
     */
    @FXML
    private void submitReservationRequest() {
        // ReservationController.onReserve() will run the validation,
        // send the message to the server, show alerts, etc.
        onReserve();    
    }

    /**
     * Handles the Clear/Cancel button click event.
     * 
     * Called automatically by JavaFX when the user clicks “Clear”.
     * Delegates to onCancel() in the superclass to clear the form fields.
     */
    @FXML
    private void submitCancellation() {
        onCancel();
    }
}