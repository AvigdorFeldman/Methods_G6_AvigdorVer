package clientControllers;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Alert.AlertType;
import logic.Reservation;
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
	private BarChart<String, Number> reservationsBarChart;

	@FXML
	private Button exportCsvButton;
	@FXML
	private Button showPDF;
	@FXML
	private Button refreshButton;

	/**
	 * Initializes UI componants
	 */
	@FXML
	@Override
	public void initialize() {
		super.initialize(); // Load base logic
		if (exportCsvButton != null) {
			exportCsvButton.setOnAction(e -> { // Sends all files to the server
				try {
					String selectedMonth = monthComboBox.getValue();
					Integer selectedYear = yearComboBox.getValue();
					String defaultName = "ReservationsReport_" + selectedMonth + "_" + selectedYear + ".csv";
					File reportsDir = new File("reports"); // Relative path to your reports directory
					if (!reportsDir.exists()) {
					    reportsDir.mkdirs(); // Ensure the directory exists
					}
					 File reportFile = new File("reports/" + defaultName);
					if (reportFile != null) {
						File imageFile = new File("reports/ReservationsChart_"+ selectedMonth + "_" + selectedYear + ".png");
						Util.saveChartAsImage(reservationsBarChart, imageFile);
						Util.exportToCSV(reservationTable, reportFile);
						Util.sendReportFileToServer(reportFile, client, "File to server");
						Util.sendReportFileToServer(imageFile, client, "File to server");
						ShowAlert.showSuccessAlert("Success", "Exported table to " + reportFile.getName());
					}
				} catch (Exception ex) {
					ShowAlert.showAlert("Error", "Failed to export CSV, try again " ,AlertType.ERROR);
				}
			});
		}
		if (showPDF != null) { // Creates PDF file and shows it
			showPDF.setOnAction(e ->{
				String selectedMonth = monthComboBox.getValue();
				Integer selectedYear = yearComboBox.getValue();
				String today = selectedMonth + "_" + selectedYear;
				File reportsDir = new File("reports");
		        File csvFile = new File(reportsDir, "ReservationsReport_" + today + ".csv");
		        File pngFile = new File(reportsDir, "ReservationsChart_" + today + ".png");

		        // Check if the necessary files exist before sending the request
		        if (!csvFile.exists() || !pngFile.exists()) {
		            // If either file is missing, show an alert to inform the user
		            ShowAlert.showAlert("Error", "Required files are missing or could not be found.", AlertType.ERROR);
		            return;  // Prevent sending the request if the files are missing
		        }
				try {
					client.sendToServer(new SendObject<String>("Get Reservation Report", today));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			});
		}
		if(refreshButton != null) { // Refreshes reservation list
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
		updateBarChart(); // Add chart functionality
	}

	
	private void updateBarChart() {
	    reservationsBarChart.getData().clear(); // Clear existing data

	    String selectedMonth = monthComboBox.getValue();
	    Integer selectedYear = yearComboBox.getValue();

	    if (selectedMonth == null || "All".equals(selectedMonth) || selectedYear == null || selectedYear == 0) {
	        reservationsBarChart.setTitle("Select a specific month and year to see daily reservation counts");
	        return;
	    }

	    int monthIndex = months.indexOf(selectedMonth); // January=1
	    YearMonth yearMonth = YearMonth.of(selectedYear, monthIndex);
	    int daysInMonth = yearMonth.lengthOfMonth();

	    // Maps to hold counts for each day
	    Map<String, Integer> activeCounts = new LinkedHashMap<>();
	    Map<String, Integer> canceledCounts = new LinkedHashMap<>();

	    // Initialize maps with days of the month
	    for (int i = 1; i <= daysInMonth; i++) {
	        String day = String.valueOf(i);
	        activeCounts.put(day, 0);
	        canceledCounts.put(day, 0);
	    }

	    if (filteredReservations != null) {
	        for (Reservation reservation : filteredReservations) {
	            LocalDate date = reservation.getDate();
	            if (date.getYear() == selectedYear && date.getMonthValue() == monthIndex) {
	                String day = String.valueOf(date.getDayOfMonth());
	                if (reservation.getStartTime() == null) {
	                    // Count as canceled if start time is null
	                    canceledCounts.put(day, canceledCounts.get(day) + 1);
	                } else if (reservation.getEndTime() != null) {
	                    // Count as active if both start and end times are present
	                    activeCounts.put(day, activeCounts.get(day) + 1);
	                }
	            }
	        }
	    }

	    // Create Series for used and canceled reservations
	    XYChart.Series<String, Number> usedSeries = new XYChart.Series<>();
	    usedSeries.setName("Used Reservations");

	    XYChart.Series<String, Number> canceledSeries = new XYChart.Series<>();
	    canceledSeries.setName("Canceled Reservations");
	    // Add data to the series
	    for (int day = 1; day <= daysInMonth; day++) {
	        String dayString = String.valueOf(day);
	        int activeCount = activeCounts.get(dayString);
	        int canceledCount = canceledCounts.get(dayString);
	        usedSeries.getData().add(new XYChart.Data<>(dayString, activeCount));
	        canceledSeries.getData().add(new XYChart.Data<>(dayString, canceledCount));
	    }

	    // Add both series to the chart
	    reservationsBarChart.getData().add(usedSeries);
	    reservationsBarChart.getData().add(canceledSeries);

	    // Set chart title
	    reservationsBarChart.setTitle("Reservations in " + selectedMonth + " " + selectedYear);

	    // Ensure chart layout is updated on the JavaFX Application thread
	    Platform.runLater(() -> reservationsBarChart.layout());
	}




}
