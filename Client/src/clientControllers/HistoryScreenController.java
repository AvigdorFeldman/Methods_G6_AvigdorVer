package clientControllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.text.SimpleDateFormat;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import logic.*;
import ocsf.client.*;

/**
 * Controller class for the history screen in the client-side parking management system.
 * This screen displays a table of past parking sessions for a specific subscriber.
 * The table includes session ID, spot ID, entry and exit timestamps, and whether the session was late.
 * The controller communicates with {@link HistoryController} to fetch data from the server.
 */
public class HistoryScreenController extends Controller {

    private HistoryController controller = new HistoryController();

    @FXML
    private TableView<Parkingsession> parkingTable;

    @FXML
    private TableColumn<Parkingsession, Integer> colSessionId;

    @FXML
    private TableColumn<Parkingsession, Integer> colSpotId;

    @FXML
    private TableColumn<Parkingsession, String> colInTime;

    @FXML
    private TableColumn<Parkingsession, String> colOutTime;

    @FXML
    private TableColumn<Parkingsession, String> colLate;

    /**
     * Sets the BParkClient and subscriber for this screen and initializes the data controller.
     * Once set, it triggers the display of parking history on the JavaFX application thread.
     *
     * @param client the client instance used for server communication
     * @param sub the currently logged-in subscriber
     */
    public void setClient(BParkClient client, subscriber sub) {
        super.setClient(client, sub);
        controller.setClient(client, sub);
        Platform.runLater(() -> displayHistory());
    }

    /**
     * Initializes the table columns in the history screen.
     * Configures bindings for displaying session data, including formatted dates and "late" status.
     */
    @FXML
    public void initialize() {
        colSessionId.setCellValueFactory(new PropertyValueFactory<>("sessionId"));
        colSpotId.setCellValueFactory(new PropertyValueFactory<>("spotId"));

        colInTime.setCellValueFactory(cellData -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return javafx.beans.binding.Bindings.createStringBinding(() -> 
                sdf.format(cellData.getValue().getInTime()));
        });

        colOutTime.setCellValueFactory(cellData -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return javafx.beans.binding.Bindings.createStringBinding(() -> 
                sdf.format(cellData.getValue().getOutTime()));
        });

        colLate.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> 
                cellData.getValue().isLate() ? "Yes" : "No"));
    }

    /**
     * Fetches the parking session history from the server and populates the table.
     * Displays an alert in case of failure to retrieve data.
     */
    @FXML
    public void displayHistory() {
        List<Parkingsession> sessions;
        try {
            sessions = controller.getSessions();
            ObservableList<Parkingsession> data = FXCollections.observableArrayList(sessions);
            parkingTable.setItems(data);
        } catch (Exception e) {
            ShowAlert.showAlert("Error", "Error getting history: " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Handles messages received asynchronously from the server.
     *
     * @param message the message object received from the server
     */
    public void handleServerMessage(Object message) {
        controller.handleServerMessage(message);
    }
}
