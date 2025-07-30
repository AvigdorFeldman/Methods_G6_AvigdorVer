package clientControllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import logic.*;
import ocsf.client.*;

/**
 * Controller class for the time extension screen in the client-side parking management system.
 *
 * Allows users to extend their parking sessions, load existing sessions by ID, and view all active sessions.
 * Integrates with {@link ParkingController} for back-end communication and updates.
 */
public class TimeExtensionScreenController extends Controller {

	@FXML
	private Spinner<Integer> hourSpinner;

	@FXML
	private Spinner<Integer> minuteSpinner;

	@FXML
	private TextField ParkingsessionIdField;
	
	@FXML
	private Label ParkingsessionIdLabel;

	@FXML
	private TableView<Parkingsession> currentParkingSessionTable;

	@FXML
	private TableColumn<Parkingsession, Integer> colSessionId;

	@FXML
	private TableColumn<Parkingsession, Integer> colSpotId;

	@FXML
	private TableColumn<Parkingsession, Date> colInTime;

	@FXML
	private TableColumn<Parkingsession, Date> colOutTime;

	/**
	 *  Parking controller used to handle logic for this screen.
	 */
	private ParkingController parkingController = new ParkingController();

	/**
	 * Currently selected session for extension.
	 */
	private Parkingsession session;

	/**
	 * Flag indicating if a session was recently extended.
	 */
	private boolean extendedSessionId = false;

	/**
	 * Initializes the screen with client and subscriber, sets up message listener and loads session data.
     *
     * @param client the client used to communicate with the server
     * @param sub    the current subscriber using the system
	 */
	public void setClient(BParkClient client, subscriber sub) {
		this.client = client;
		this.sub = sub;
		parkingController.setClient(client, sub);
		parkingController.setTimeExtensionScreen(this);
		client.setMessageListener(parkingController::handleServerResponse);
		Platform.runLater(() -> {
			displayActiveSessions();
		});
	}

	/**
	 * Initializes all UI components, including spinners, table columns, and listeners.
     * 
     * Automatically called by the JavaFX framework.
	 */
	@FXML
	private void initialize() {
		// Initialize Spinner for Hours
		SpinnerValueFactory<Integer> hourValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 4, 0);
		hourSpinner.setValueFactory(hourValueFactory);
		// Initialize Spinner for Minutes
		SpinnerValueFactory<Integer> minuteValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
		minuteSpinner.setValueFactory(minuteValueFactory);
		// Restrict session ID field to digits only
		ParkingsessionIdField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*")) {
				ParkingsessionIdField.setText(oldValue); // Revert to old value if invalid
				ParkingsessionIdField.setStyle("-fx-border-color: red;");
				ParkingsessionIdLabel.setText("ID must be numeric");
				ParkingsessionIdLabel.setVisible(true);
	            } else {
	            	ParkingsessionIdField.setStyle(null);
	            	ParkingsessionIdLabel.setVisible(false);
	            }
	        });
		// Initialize TableView columns
		colSessionId.setCellValueFactory(new PropertyValueFactory<>("sessionId"));
		colSpotId.setCellValueFactory(new PropertyValueFactory<>("spotId"));

		// Set up Date formatting for InTime and OutTime columns
		colInTime.setCellValueFactory(new PropertyValueFactory<>("inTime"));
		colInTime.setCellFactory(column -> {
			return new TableCell<Parkingsession, Date>() {
				@Override
				protected void updateItem(Date item, boolean empty) {
					super.updateItem(item, empty);
					if (item != null) {
						SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
						setText(format.format(item));
					} else {
						setText(null);
					}
				}
			};
		});
		// Format and style out-time column
		colOutTime.setCellValueFactory(new PropertyValueFactory<>("outTime"));
		colOutTime.setCellFactory(column -> new TableCell<Parkingsession, Date>() {
			@Override
			protected void updateItem(Date item, boolean empty) {
				super.updateItem(item, empty);

				if (empty || item == null) {
					setText(null);
					setStyle("");
					return;
				}

				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				setText(format.format(item));

				int index = getIndex(); // ✅ this is valid inside TableCell
				ObservableList<Parkingsession> items = getTableView().getItems();

				if (index >= 0 && index < items.size()) {
					Parkingsession rowItem = items.get(index);
					if (session != null && extendedSessionId && rowItem.getSessionId() == session.getSessionId()) {
						setStyle("-fx-background-color: #ffe599;"); // light yellow
					} else {
						setStyle(""); // reset
					}
				} else {
					setStyle(""); // clear if index is out of range
				}
			}
		});
		// Row styling based on selection
		currentParkingSessionTable.setRowFactory(tv -> new TableRow<Parkingsession>() {
			@Override
			protected void updateItem(Parkingsession item, boolean empty) {
				super.updateItem(item, empty);
				if (item == null || empty) {
					setStyle("");
				} else {
					if (session != null && item.getSessionId() == session.getSessionId()) {
						setStyle("-fx-background-color: #a3d2ca;"); // Light green
					} else {
						setStyle("");
					}
				}
			}
		});
	}

	/**
	 * Displays all active parking sessions in the table view.
	 */
	public void displayActiveSessions() {
		List<Parkingsession> sessions;
		try {
			sessions = parkingController.getActiveParkingSessions();
			ObservableList<Parkingsession> data = FXCollections.observableArrayList(sessions);
			currentParkingSessionTable.setItems(data);
		} catch (Exception e) {
			ShowFail("Error getting Active sessions");
			e.printStackTrace();
		}
	}

	/**
	 * Handles user action to load a session by ID.
     * 
     * Highlights the session in the table if found.
	 * @throws Exception if something unexpected happend
	 * and shows alert based on the the exception
	 */
	@FXML
	private void handleLoadSession() throws Exception {
		try {
			String parkingsessionId = ParkingsessionIdField.getText();
			int parkingId = Integer.parseInt(parkingsessionId);
			session = parkingController.getSessionById(parkingId);
			extendedSessionId = false;
			currentParkingSessionTable.refresh();
			for (Parkingsession ps : currentParkingSessionTable.getItems()) {
				if (ps.getSessionId() == session.getSessionId()) {
					currentParkingSessionTable.scrollTo(ps);
					break;
				}
			}
		} catch (NumberFormatException e) {
			ShowFail("Enter a valid Parkingsession");
		}
	}

	/**
	 * Handles the extension of time for a selected parking session.
     * 
     * Validates the input, applies the new time, and updates the session via server.
	 */
	@FXML
	private void handleExtendTime() {
		extendedSessionId = false;
		long hour = hourSpinner.getValue();
		long minute = minuteSpinner.getValue();
		// Input validation
		if (hour > 3 && minute > 0 || hour == 0 && minute == 0 || session == null) {
			if (hour > 3 && minute > 0)
				ShowFail("Time Extension must be up to 4 hours!");
			else if (hour == 0 && minute == 0) {
				ShowFail("Time Extension must be at least a minute!");
			} else {
				ShowFail("Session was not found");
			}
		} else {
			Date timeBefore = session.getOutTime();
			session.setOutTime(new Date(session.getOutTime().getTime() + hour * 60 * 60 * 1000 + minute * 60 * 1000));
			try {
				if (!ShowAlert.showConfirmation("Confirm Time Extension",
						"Are you sure you want to extend time by " + hour + " hour(s) and " + minute + " minute(s)?")) {
					extendedSessionId = false;
					session = null;
					currentParkingSessionTable.refresh();

					return; // user clicked Cancel
				}
				
				// Updates the session
				parkingController.ExtendTime(session);	
				Platform.runLater(() -> {
					displayActiveSessions();
					try {
						handleLoadSession();
					} catch (Exception e) {
						ShowFail("Time hasn't extended");
					}
					currentParkingSessionTable.refresh();
					if(!session.getOutTime().equals(timeBefore)) {			
						ShowAlert.showSuccessAlert("Extended Time Success", "Time was successfully extended");
						extendedSessionId = true;
						clearForm();
						session = null;
					}
					else {
						ShowFail("Time could not be extended, the spot is reserved at that time");
					}	
				});		
			} catch (Exception e) {
				ShowFail("Time hasn't extended");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Clears the input form and resets the UI.
	 */
	private void clearForm() {
		ParkingsessionIdField.setText("");
		initialize();
	}

	/**
	 * Handles incoming messages from the server and delegates to the {@link ParkingController}.
     *
     * @param message the incoming server message
	 */
	public void handleServerMessage(Object message) {
		parkingController.handleServerResponse(message);
	}

	/**
	 * Displays a failure popup with the given message.
     *
     * @param string the error message to display
	 */
	public void ShowFail(String string) {
		ShowAlert.showAlert("Error", string, AlertType.ERROR);
	}
}
