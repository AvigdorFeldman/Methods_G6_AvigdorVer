package clientControllers;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

/**
 * Utility class for showing different types of alert dialogs in the JavaFX UI.
 * 
 * Provides static methods for showing informational, error, and confirmation alerts.
 */
public class ShowAlert {

    /**
     * Shows an alert dialog with the given title, message, and alert type.
     *
     * @param title     The title of the alert window.
     * @param message   The content text to display.
     * @param alertType The type of alert (e.g., INFORMATION, ERROR).
     */
    public static void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows a confirmation dialog with OK and Cancel options.
     *
     * @param title   The title of the confirmation dialog.
     * @param message The content text to display.
     * @return true if the user clicked OK; false otherwise.
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Shows an alert success dialog with the given title and message
     * 
     * @param title   The title of the confirmation dialog.
     * @param message The content text to display.
     */
    public static void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("✔ Operation Successful!");
        alert.setContentText(message);
        alert.setGraphic(new Label("✅")); 

        alert.showAndWait();
    }
}