package clientControllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import logic.Reservation;
import logic.SendObject;

/**
 * Controller for viewing parking reservations in a client-server parking management system.
 * This class provides functionality to display, filter, and sort reservations in a table view,
 * as well as handle server messages containing updated reservation data.
 * 
 * The table displays detailed information about each reservation, and filtering options
 * allow users to view reservations by month and year.
 */
public class ViewReservationController extends Controller {

	@FXML
	protected TableView<Reservation> reservationTable;

	@FXML
	protected TableColumn<Reservation, Integer> colSubscriberId;

	@FXML
	protected TableColumn<Reservation, Integer> colSpotId;

	@FXML
	protected TableColumn<Reservation, LocalDate> colDate;

	@FXML
	protected TableColumn<Reservation, String> colStartTime;

	@FXML
	protected TableColumn<Reservation, String> colEndTime;

	@FXML
	protected Button sortByDateButton;

	@FXML
	protected Button sortBySubscriberIdButton;

	@FXML
	protected ComboBox<String> monthComboBox;

	@FXML
	protected ComboBox<Integer> yearComboBox;

	protected List<Reservation> allReservations = new ArrayList<>();
	protected List<Reservation> filteredReservations = new ArrayList<>();

	protected final List<String> months = Arrays.asList("All", "January", "February", "March", "April", "May", "June",
			"July", "August", "September", "October", "November", "December");
	protected final List<Integer> years = IntStream.rangeClosed(2020, 2030).boxed()
			.collect(Collectors.toCollection(ArrayList::new));

	/**
     * Initializes the table columns, ComboBoxes, and event handlers.
     * This method is automatically called after the FXML file is loaded.
     */
	@FXML
	public void initialize() {
		years.add(0, 0); // Add "All" as first year

		// Setup table columns
		colSubscriberId.setCellValueFactory(
				cellData -> new SimpleIntegerProperty(cellData.getValue().getSubscriberId()).asObject());
		colSpotId.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getSpot()).asObject());
		colDate.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDate()));
		colStartTime.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getStartTime()));
		colEndTime.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getEndTime()));

		// Setup ComboBoxes
		monthComboBox.getItems().setAll(months);
		monthComboBox.getSelectionModel().selectFirst(); // All
		yearComboBox.getItems().setAll(years);
		setupYearComboBox();

		monthComboBox.setOnAction(e -> filterReservations());
		yearComboBox.setOnAction(e -> filterReservations());

		if (sortByDateButton != null)
			sortByDateButton.setOnAction(e -> sortByDate());
		if (sortBySubscriberIdButton != null)
			sortBySubscriberIdButton.setOnAction(e -> sortBySubscriberId());
	}

    /**
     * Configures the year ComboBox to display "All" for the first item.
     */
	protected void setupYearComboBox() {
		yearComboBox.setCellFactory(cb -> new ListCell<Integer>() {
			@Override
			protected void updateItem(Integer item, boolean empty) {
				super.updateItem(item, empty);
				setText(item == null ? "" : (item == 0 ? "All" : item.toString()));
			}
		});
		yearComboBox.setButtonCell(new ListCell<Integer>() {
			@Override
			protected void updateItem(Integer item, boolean empty) {
				super.updateItem(item, empty);
				setText(item == null ? "" : (item == 0 ? "All" : item.toString()));
			}
		});
		yearComboBox.getSelectionModel().selectFirst(); // 0 = All
	}

    /**
     * Sets the list of reservations and applies filtering based on the selected month and year.
     *
     * @param reservations The list of reservations to display.
     */
	public void setReservations(List<Reservation> reservations) {
		this.allReservations = reservations;
		filterReservations();
	}

    /**
     * Filters the reservations based on the selected month and year.
     * Updates the table view with the filtered reservations.
     */
	protected void filterReservations() {
		String selectedMonth = monthComboBox.getValue();
		Integer selectedYear = yearComboBox.getValue();

		filteredReservations = allReservations.stream().filter(res -> {
			boolean monthOk = true, yearOk = true;
			if (selectedMonth != null && !"All".equals(selectedMonth)) {
				int monthIndex = months.indexOf(selectedMonth);
				monthOk = res.getDate().getMonthValue() == monthIndex;
			}
			if (selectedYear != null && selectedYear != 0) {
				yearOk = res.getDate().getYear() == selectedYear;
			}
			return monthOk && yearOk;
		}).collect(Collectors.toList());

		reservationTable.getItems().setAll(filteredReservations);
	}

    /**
     * Sorts the reservations by date in ascending order.
     */
	private void sortByDate() {
		colDate.setSortType(TableColumn.SortType.ASCENDING);
		reservationTable.getSortOrder().setAll(colDate);
		reservationTable.sort();
	}

	/**
	 * Sorts the reservations by subscriber ID in ascending order.
	 */
	private void sortBySubscriberId() {
		colSubscriberId.setSortType(TableColumn.SortType.ASCENDING);
		reservationTable.getSortOrder().setAll(colSubscriberId);
		reservationTable.sort();
	}

    /**
     * Handles incoming server messages and updates the reservation data accordingly.
     * This method processes messages containing updated reservation lists and refreshes
     * the table view.
     *
     * @param msg The server message containing updated reservation data.
     */
	@Override
	public void handleServerMessage(Object msg) {
		if (msg instanceof SendObject<?>) {
			if (((SendObject<?>) msg).getObj() instanceof List<?>) {
				List<?> updated = (List<?>) ((SendObject<?>) msg).getObj();
				if (!updated.isEmpty() && updated.get(0) instanceof Reservation) {
					Platform.runLater(() -> setReservations((List<Reservation>) updated));
				}
			}
		}
	}
}
