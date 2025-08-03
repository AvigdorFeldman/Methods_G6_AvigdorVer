package clientControllers;

import java.io.File;
import java.io.IOException;
import java.time.Year;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import logic.SendObject;

/**
 * Controller for exporting parking reservations to a CSV file in a client-server parking management system.
 * This class extends the functionality of `ViewReservationController` by adding the ability to export
 * the table data to a CSV file and visualize daily reservation counts in a line chart.
 * 
 * The table displays detailed information about each reservation, and users can save this data
 * to a CSV file using the export button. The line chart visualizes daily reservation counts
 * for a specific month and year.
 */
public class ReportReservationController extends ViewReservationController {

	@FXML
	private LineChart<Number, Number> reservationsLineChart;

	@FXML
	private Button exportCsvButton;
	@FXML
	private Button showPDF;
	@FXML
	private Button refreshButton;

	@FXML
	public void initialize() {
		super.initialize(); // Load base logic
		if (exportCsvButton != null) {
			exportCsvButton.setOnAction(e -> {
				try {
					String selectedMonth = monthComboBox.getValue();
					Integer selectedYear = yearComboBox.getValue();
					String defaultName = "ReservationsReport_" + selectedMonth + "_" + selectedYear + ".csv";

					 File reportFile = new File("reports/" + defaultName);
					if (reportFile != null) {
						File imageFile = new File("reports/ReservationsChart_"+ selectedMonth + "_" + selectedYear + ".png");
						Util.saveChartAsImage(reservationsLineChart, imageFile);
						Util.exportToCSV(reservationTable, reportFile);
						Util.sendReportFileToServer(reportFile, client, "File to server");
						Util.sendReportFileToServer(imageFile, client, "File to server");
						showAlert("Exported table to " + reportFile.getName());
					}
				} catch (Exception ex) {
					showAlert("Failed to export CSV: " + ex.getMessage());
				}
			});
		}
		if (showPDF != null) {
			showPDF.setOnAction(e ->{
				String selectedMonth = monthComboBox.getValue();
				Integer selectedYear = yearComboBox.getValue();
				String today = selectedMonth + "_" + selectedYear;
				try {
					client.sendToServer(new SendObject<String>("Get Reservation Report", today));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			});
		}
		if(refreshButton != null) {
			refreshButton.setOnAction(e ->{
				SendObject<String> request = new SendObject<>("Get", "all reservations");
				client.sendToServerSafely(request);
			});
		}
	}

    /**
     * Filters the reservations and updates the line chart with daily reservation counts.
     */
	@Override
	protected void filterReservations() {
		super.filterReservations(); // Call base logic
		updateLineChart(); // Add chart functionality
	}

	
    /**
     * Updates the line chart to display daily reservation counts for the selected month and year.
     */
	private void updateLineChart() {
		reservationsLineChart.getData().clear();

		String selectedMonth = monthComboBox.getValue();
		Integer selectedYear = yearComboBox.getValue();

		if (selectedMonth == null || "All".equals(selectedMonth) || selectedYear == null || selectedYear == 0) {
			reservationsLineChart.setTitle("Select specific month and year to see daily reservation count");
			return;
		}

		int monthIndex = months.indexOf(selectedMonth); // January=1
		int daysInMonth = java.time.Month.of(monthIndex).length(Year.isLeap(selectedYear));

		int[] counts = new int[daysInMonth + 1]; // 1-based indexing
		filteredReservations.stream()
				.filter(res -> res.getDate().getMonthValue() == monthIndex && res.getDate().getYear() == selectedYear)
				.forEach(res -> counts[res.getDate().getDayOfMonth()]++);

		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		series.setName("Reservations per Day");

		for (int day = 1; day <= daysInMonth; day++) {
			series.getData().add(new XYChart.Data<>(day, counts[day]));
		}
		reservationsLineChart.getData().add(series);
		reservationsLineChart.setTitle("Reservations in " + selectedMonth + " " + selectedYear);
		
		Platform.runLater(() -> reservationsLineChart.layout());
	}

	private void showAlert(String msg) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
		alert.showAndWait();
	}
}
