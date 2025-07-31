package clientControllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import logic.Parkingsession;
import logic.Role;
import logic.SendObject;
import logic.subscriber;

/**
 * Controller for viewing subscribers and their parking session history in a
 * client-server parking management system. This class provides functionality
 * for filtering, sorting, and viewing subscriber data, as well as displaying
 * parking session history for individual subscribers.
 * 
 * The controller interacts with the server to fetch subscriber and parking
 * session data, and updates the UI components accordingly.
 */
public class ViewSubscriberController extends Controller {

	@FXML
	protected TableView<subscriber> subscriberTable;

	@FXML
	protected TableColumn<subscriber, Integer> colSubscriberId;
	@FXML
	protected TableColumn<subscriber, String> colName;
	@FXML
	protected TableColumn<subscriber, String> colEmail;
	@FXML
	protected TableColumn<subscriber, String> colPhone;
	@FXML
	protected TableColumn<subscriber, String> colTag;
	@FXML
	protected TableColumn<subscriber, Role> colRole;

	@FXML
	protected ComboBox<String> roleComboBox;

	@FXML
	protected Button sortBySubscriberIdButton;
	@FXML
	protected Button backButton;

	protected List<subscriber> allSubscribers = new ArrayList<>();
	protected List<subscriber> filteredSubscribers = new ArrayList<>();
	protected final List<String> roles = Arrays.asList("All", "MANAGER", "SUBSCRIBER", "ATTENDANT");

	@FXML
	private TextField subscriberIdField;

	@FXML
	private Button viewHistoryButton;

	@FXML
	private TableView<Parkingsession> historyTable;

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

	@SuppressWarnings("unused")
	private List<Parkingsession> historySessions;

	/**
	 * Initializes the UI components and sets up event handlers for buttons and
	 * combo boxes. This method is automatically called after the FXML file is
	 * loaded.
	 */
	@FXML
	public void initialize() {
		// Column setup
		colSubscriberId
				.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
		colName.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getName()));
		colEmail.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getEmail()));
		colPhone.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPhone()));
		colTag.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTag()));
		colRole.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getRole()));

		// Combo box
		roleComboBox.getItems().setAll(roles);
		roleComboBox.getSelectionModel().selectFirst();
		roleComboBox.setOnAction(e -> filterSubscribers());

		if (sortBySubscriberIdButton != null) {
			sortBySubscriberIdButton.setOnAction(e -> sortBySubscriberId());
		}

		// History table setup
		colSessionId.setCellValueFactory(new PropertyValueFactory<>("sessionId"));
		colSpotId.setCellValueFactory(new PropertyValueFactory<>("spotId"));
		colInTime.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(
				() -> new SimpleDateFormat("dd/MM/yyyy HH:mm").format(cellData.getValue().getInTime())));
		colOutTime.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(
				() -> new SimpleDateFormat("dd/MM/yyyy HH:mm").format(cellData.getValue().getOutTime())));
		colLate.setCellValueFactory(cellData -> javafx.beans.binding.Bindings
				.createStringBinding(() -> cellData.getValue().isLate() ? "Yes" : "No"));
		viewHistoryButton.setOnAction(e -> fetchHistory());
	}

	/**
	 * Fetches the parking session history for the subscriber with the entered ID.
	 * Sends a request to the server to retrieve the history data.
	 * 
	 * @throws NumberFormatException if the entered subscriber ID is not a valid
	 *                               number.
	 */
	private void fetchHistory() {
		String idText = subscriberIdField.getText().trim();
		if (idText.isEmpty()) {
			showAlert("Please enter a Subscriber ID");
			return;
		}

		try {
			int subscriberId = Integer.parseInt(idText);
			client.sendToServerSafely(new SendObject<Integer>("Get history", subscriberId));
		} catch (NumberFormatException e) {
			showAlert("Subscriber ID must be a number.");
		}
	}

	/**
	 * Populates the history table with the provided list of parking sessions.
	 * 
	 * @param sessions The list of parking sessions to display in the history table.
	 */
	private void populateHistoryTable(List<Parkingsession> sessions) {
		ObservableList<Parkingsession> data = FXCollections.observableArrayList(sessions);
		historyTable.setItems(data);
	}

	/**
	 * Can be reused in subclass. 
	 * Filters the subscriber list based on the selected
	 * role in the combo box. Updates the table view with the filtered list.
	 */
	protected void filterSubscribers() {
		String selectedRole = roleComboBox.getValue();

		filteredSubscribers = allSubscribers.stream().filter(sub -> {
			if (selectedRole == null || selectedRole.equals("All"))
				return true;
			return sub.getRole().name().equals(selectedRole);
		}).collect(Collectors.toList());

		subscriberTable.getItems().setAll(filteredSubscribers);
	}

    /**
     * Sorts the subscriber table by subscriber ID in ascending order.
     */
	@SuppressWarnings("unchecked")
	protected void sortBySubscriberId() {
		colSubscriberId.setSortType(TableColumn.SortType.ASCENDING);
		subscriberTable.getSortOrder().setAll(colSubscriberId);
		subscriberTable.sort();
	}

    /**
     * Sets the list of subscribers and updates the table view.
     * 
     * @param subscribers The list of subscribers to display in the table view.
     */
	public void setSubscribers(List<subscriber> subscribers) {
		this.allSubscribers = subscribers;
		filterSubscribers();
	}

    /**
     * Sets the list of parking session history and updates the history table.
     * 
     * @param history The list of parking sessions to display in the history table.
     */
	public void setHistorySessions(List<Parkingsession> history) {
		if (history != null) {
			this.historySessions = history;
		} else {
			this.historySessions = new ArrayList<Parkingsession>();
		}
		populateHistoryTable(history);
	}

    /**
     * Handles server messages and updates the UI components based on the received data.
     * 
     * @param msg The message received from the server.
     */
	@SuppressWarnings("unchecked")
	@Override
	public void handleServerMessage(Object msg) {
		if (msg instanceof SendObject<?>) {
			SendObject<?> so = (SendObject<?>) msg;

			// Handle subscriber list updates
			if (so.getObj() instanceof List<?>) {
				List<?> updated = (List<?>) so.getObj();
				if (!updated.isEmpty() && updated.get(0) instanceof subscriber) {
					Platform.runLater(() -> setSubscribers((List<subscriber>) updated));
				}
			}

			// Handle history data per specific subscriber
			if ("Parkingsession list of subscriber".equals(so.getObjectMessage())) {
				Object obj = so.getObj();
				if (obj instanceof List<?>) {
					List<?> list = (List<?>) obj;
					if (!list.isEmpty() && list.get(0) instanceof Parkingsession) {
						setHistorySessions((List<Parkingsession>) list);
					} else {
						// empty the table and chart if no sessions found
						setHistorySessions(new ArrayList<>());
					}
				}
			}
		}
	}

	protected void showAlert(String msg) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
		alert.showAndWait();
	}
}