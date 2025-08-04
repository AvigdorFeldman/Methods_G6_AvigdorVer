package clientControllers;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import logic.FileTransferMessage;
import logic.ParkingSpot;
import logic.Parkingsession;
import logic.SendObject;

/**
 * Controller for the "View Active Sessions" screen in the client application.
 *
 * This class views the display of active parking sessions in a table view,
 * allowing users to view session details such as session ID, subscriber ID,
 * parking spot ID, and more. It also handles server messages to update the
 * session data dynamically.
 * 
 * Note: This controller is designed to work with JavaFX and assumes proper
 * initialization of FXML components.
 */
public class ViewActiveSessionsController extends Controller {

	@FXML
	protected TableView<Parkingsession> sessionTable;
	@FXML
	protected TableColumn<Parkingsession, Integer> colSessionId;
	@FXML
	protected TableColumn<Parkingsession, Integer> colSubscriberId;
	@FXML
	protected TableColumn<Parkingsession, Integer> colSpotId;
	@FXML
	protected TableColumn<Parkingsession, Integer> colParkingCode;
	@FXML
	protected TableColumn<Parkingsession, java.util.Date> colInTime;
	@FXML
	protected TableColumn<Parkingsession, java.util.Date> colOutTime;
	@FXML
	protected TableColumn<Parkingsession, Boolean> colExtended;
	@FXML
	protected TableColumn<Parkingsession, Boolean> colLate;
	@FXML
	protected TableColumn<Parkingsession, Boolean> colActive;
	@FXML
	protected Button backButton;
	@FXML
	protected DatePicker datePick;
	
	protected LocalDate date;
	protected List<Parkingsession> allSessions = new ArrayList<>();
	protected List<Parkingsession> dateSessions = new ArrayList<>();
	protected List<ParkingSpot> parkingSpots = new ArrayList<>();
	
    /**
     * Initializes the table view and its columns.
     *
     * This method sets up the cell value factories for each column to bind
     * parking session properties to the table view.
     */
	@FXML
	public void initialize() {
		datePick.setValue(LocalDate.now());
		colSessionId.setCellValueFactory(
				cellData -> new SimpleIntegerProperty(cellData.getValue().getSessionId()).asObject());
		colSubscriberId.setCellValueFactory(
				cellData -> new SimpleIntegerProperty(cellData.getValue().getSubscriberId()).asObject());
		colSpotId
				.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getSpotId()).asObject());
		colParkingCode.setCellValueFactory(
				cellData -> new SimpleIntegerProperty(cellData.getValue().getParkingCode()).asObject());
		colInTime.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getInTime()));
		colOutTime.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getOutTime()));
		colExtended.setCellValueFactory(
				cellData -> new SimpleBooleanProperty(cellData.getValue().isExtended()).asObject());
		colLate.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isLate()).asObject());
		colActive
				.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().getActive()).asObject());
		if(datePick != null) {
			datePick.setOnAction(event -> {
				refresh();
			});
		}
	}

    /**
     * Sets the parking sessions to be displayed in the table view.
     *
     * @param sessions List of parking sessions to display.
     */
	public void setSessions(List<Parkingsession> sessions) {
		this.allSessions = sessions;
		filterByDate();
	}
	
	protected void refresh() {
		if (datePick.getValue() != null) {
            if(!datePick.getValue().isAfter(LocalDate.now())) {
            	date = datePick.getValue();
            	filterByDate();
            	sessionTable.refresh();
            }
            else {
            	ShowAlert.showAlert("Error", "Enter a past or current date only!", AlertType.ERROR);
            }
        } else {
            date = null;                
        }
	}
	
	private void filterByDate() {
		dateSessions.clear();
		if(allSessions !=null) {
			for (Parkingsession parkingsession : allSessions) {
				if ((parkingsession.getInTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).equals(date)){
					dateSessions.add(parkingsession);
				}
			}
		}
		sessionTable.getItems().setAll(dateSessions);
	}

    /**
     * Handles incoming server messages to update the parking session data.
     *
     * If the message contains a list of parking sessions, the table view is
     * updated with the new data.
     * If the message contains a list of parking spots, the pi chart is
     * updated with the new data.
     * Shows PDF
     *
     *
     * @param msg The server message containing session data.
     */
	@SuppressWarnings("unchecked")
	public void handleServerMessage(Object msg) {
		if (msg instanceof SendObject<?>) {
			if (((SendObject<?>) msg).getObj() instanceof List<?>) {
				List<?> updated = (List<?>) ((SendObject<?>) msg).getObj();
				if (!updated.isEmpty() && updated.get(0) instanceof Parkingsession) {
					Platform.runLater(() -> setSessions((List<Parkingsession>) updated));
				}
				if (!updated.isEmpty() && updated.get(0) instanceof ParkingSpot) {
					Platform.runLater(() -> parkingSpots=(List<ParkingSpot>)updated);
				}
			}
			else if(((SendObject<?>) msg).getObj() instanceof FileTransferMessage &&((SendObject<?>) msg).getObjectMessage().equals("ActiveSessionsPDF")) {
				Util.getPDF(msg);
			}
		}
	}

}