package clientControllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * Controller class for the Main Menu screen in the client-side parking management application.
 * Provides navigation to various user operations such as parking, retrieval, reservation,
 * history viewing, time extension, and user detail updates.
 * This class also adjusts the visibility of UI elements based on the user type (e.g., terminal vs. subscriber).
 */
public class MainMenuController extends Controller {

    @FXML
    Button historybutton;

    @FXML
    Button dropoff;

    @FXML
    Button Pickup;

    @FXML
    Button Reservation;

    @FXML
    Button Extension;

    @FXML
    Button Update;

    /**
     * Handles the back button click event and navigates the user back to the login screen.
     */
    @FXML
    protected void handleBackButton() {
        // Swap the TableView scene back to the connect screen
        handleButtonToLogin(historybutton);
    }

    /**
     * Navigates to the reservation request screen.
     */
    @FXML
    public void reservationRequest() {
        this.setscreen("ReservationScreen", "Reservation.fxml", "MainMenuScreen.fxml", "Main Menu", historybutton);
    }

    /**
     * Navigates to the parking (drop-off) screen.
     */
    @FXML
    public void parkingRequest() {
        this.setscreen("DropOffScreen", "DropOffScreen.fxml", "MainMenuScreen.fxml", "Main Menu", historybutton);
    }

    /**
     * Navigates to the vehicle retrieval (pickup) screen.
     */
    @FXML
    public void retrieveRequest() {
        this.setscreen("Pickupscreen", "PickUpScreen.fxml", "MainMenuScreen.fxml", "Main Menu", historybutton);
    }

    /**
     * Navigates to the history screen where the user can view past parking activity.
     */
    @FXML
    public void historyRequest() {
        this.setscreen("HistoryScreen", "HistoryScreen.fxml", "MainMenuScreen.fxml", "Main Menu", historybutton);
    }

    /**
     * Navigates to the screen for requesting a time extension on an active parking session.
     */
    @FXML
    public void openTimeExtensionScreen() {
        this.setscreen("TimeExtensionScreen", "TimeExtensionScreen.fxml", "MainMenuScreen.fxml", "Main Menu", historybutton);
    }

    /**
     * Navigates to the screen for updating user account details.
     */
    @FXML
    public void openUpdateUserDetails() {
        this.setscreen("UpdateUserDetails", "UpdateUserDetails.fxml", "MainMenuScreen.fxml", "Main Menu", historybutton);
    }

    /**
     * Adjusts visibility and management of buttons on the main menu based on the user role.
     *
     * @param terminal true if the user is a terminal operator, false if the user is a subscriber.
     */
    public void hidebuttons(boolean terminal) {
        if (terminal) {
            historybutton.setVisible(false);
            historybutton.setManaged(false);
            Reservation.setVisible(false);
            Reservation.setManaged(false);
            Update.setVisible(false);
            Update.setManaged(false);
        } else {
            dropoff.setVisible(false);
            dropoff.setManaged(false);
            Pickup.setVisible(false);
            Pickup.setManaged(false);
        }
    }
}
