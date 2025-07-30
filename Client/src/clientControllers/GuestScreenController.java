package clientControllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

/**
 * Controller class for the guest screen in the parking management system.
 * This screen displays the current free space percentage in the parking lot
 * for guest users and allows navigation back to the login screen.
 * 
 * This class extends {@link Controller}, which provides shared
 * logic for screen transitions and client-server communication.
 */
public class GuestScreenController extends Controller {

    /**
     * The current percentage of free parking spaces.
     */
    private double percent;

    /**
     * Back button to return to the login screen.
     */
    @FXML
    private Button backButton;

    /**
     * Label to display the percentage of free parking space.
     */
    @FXML
    private Label freeSpaceLabel;

    
    /**
     * Circular progress bar to show the precentage visually
     */
    @FXML
    private ProgressIndicator progressInd;
    
    /**
     * Initializes the guest screen after FXML loading.
     * This method updates the displayed free space using the stored percentage.
     */
    @FXML
    private void initialize() {
    	progressInd.setPrefWidth(120);
    	progressInd.setPrefHeight(120);
        updateFreeSpace(percent);
    }

    /**
     * Updates the label and progress indicator to display the current free space percentage.
     *
     * @param percent The percentage of free parking spaces to display.
     */
    public void updateFreeSpace(double percent) {
        freeSpaceLabel.setText("Free Space: " + percent + "%");
        double progress = percent / 100.0;
        progressInd.setProgress(progress);
    }

    /**
     * Handles the action of the back button.
     * Navigates the user back to the login screen.
     */
    @FXML
    protected void handleBackButton() {
        // Swap the scene back to the login screen
        handleButtonToLogin(backButton);
    }

    /**
     * Sets the free space percentage and updates the label accordingly.
     *
     * @param num The new percentage of free parking space.
     */
    public void set_percent(double num) {
        percent = num;
        updateFreeSpace(percent);
    }
}
