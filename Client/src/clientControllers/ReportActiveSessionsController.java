package clientControllers;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Alert.AlertType;
import logic.*;


/**
 * Controller for exporting active parking sessions to a CSV file in a client-server parking management system.
 * This class extends the functionality of `ViewActiveSessionsController` by adding the ability to export
 * the table data to a CSV file and visualize active sessions by hour in a line chart.
 * 
 * The table displays detailed information about each parking session, and users can save this data
 * to a CSV file using the export button. The line chart visualizes active sessions by hour.
 * 
 */
public class ReportActiveSessionsController extends ViewActiveSessionsController {

	@FXML
	private Button exportCsvButton;
	
	@FXML
	private LineChart<Number, Number> activeSessionLineChart;
	@FXML
	private NumberAxis xAxis;
	@FXML
	private NumberAxis yAxis;
	@FXML
	private Button showPDF;
	@FXML
	private Button refreshButton;
	@FXML
	private PieChart parkingSpotChart;
	@FXML
	@Override
	public void initialize() {
		super.initialize();
		
		if (exportCsvButton != null) {
			exportCsvButton.setOnAction(e -> {
				try {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
					String defaultName = "ActiveSessionsReport_" + date.format(formatter) + ".csv";

					 File reportFile = new File("reports/" + defaultName);
					if (reportFile != null) {
						File imageFile1 = new File("reports/ActiveSessionsChart_"+date.format(formatter)+".png");
						Util.saveChartAsImage(activeSessionLineChart, imageFile1);
						File imageFile2 = new File("reports/ParkingSpotsChart_"+date.format(formatter)+".png");
						Util.saveChartAsImage(parkingSpotChart, imageFile2);
						Util.exportToCSV(sessionTable, reportFile);
						Util.sendReportFileToServer(reportFile, client, "File to server");
						Util.sendReportFileToServer(imageFile1, client, "File to server");
						Util.sendReportFileToServer(imageFile2, client, "File to server");
						ShowAlert.showSuccessAlert("Success", "Exported table to " + reportFile.getName());
					}
				} catch (Exception ex) {
					ShowAlert.showAlert("Error", "Failed to export CSV, try again " ,AlertType.ERROR);
				}
			});
		}
		if (showPDF != null) {
			showPDF.setOnAction(e ->{
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				String today = date.format(formatter);
				try {
					client.sendToServer(new SendObject<String>("Get ActiveSessions", today));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			});
		}
		if(refreshButton != null) {
			refreshButton.setOnAction(e ->{
				SendObject<String> request = new SendObject<>("Get", "active parking sessions");
				client.sendToServerSafely(request);
				SendObject<String> request2 = new SendObject<>("Get", "all parking spots");
				client.sendToServerSafely(request2);
			});
		}if(datePick != null) {
			datePick.setOnAction(event -> {
				refresh();
				Platform.runLater(()->updateLineChart());
				
			});
		}
	}
	
	
    /**
     * Sets the list of sessions and updates the line chart with active session data.
     *
     * @param sessions The list of parking sessions to display.
     */
	@Override
	public void setSessions(List<Parkingsession> sessions) {
		super.setSessions(sessions);
		sessionTable.refresh();
		updateLineChart();
		Platform.runLater(()->updatePieChart());
		
	}

    /**
     * Updates the line chart to display active sessions by hour.
     */
	private void updateLineChart() {
	    int[] hourlyCounts = new int[24];
	    for (Parkingsession session : dateSessions) {
	        if (session.getActive()) {
	            LocalDateTime in = session.getInTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	            int hour = in.getHour();
	            hourlyCounts[hour]++;
	        }
	    }

	    XYChart.Series<Number, Number> series = new XYChart.Series<>();
	    series.setName("Active Sessions by Hour");
	    for (int hour = 0; hour < 24; hour++) {
	        series.getData().add(new XYChart.Data<>(hour, hourlyCounts[hour]));
	    }

	    // Ensure full range 0–23 always shown
	    xAxis.setAutoRanging(false);
	    xAxis.setLowerBound(0);
	    xAxis.setUpperBound(23);
	    xAxis.setTickUnit(1);

	    activeSessionLineChart.getData().clear();
	    activeSessionLineChart.getData().add(series);
	    
	    Platform.runLater(() -> activeSessionLineChart.layout());
	        
	}
	
	 /**
     * Updates the pie chart with parking spot statuses: Occupied, Free, Reserved.
     */
    private void updatePieChart() {
        int occupiedCount = 0;
        int freeCount = 0;
        int reservedCount = 0;

        // Loop through the sessions and count the different parking spot statuses
        for (ParkingSpot spot : parkingSpots) {
            if (spot.getStatus() == SpotStatus.OCCUPIED) {
                occupiedCount++;
            } else if (spot.getStatus() == SpotStatus.FREE) {
                freeCount++;
            } else if (spot.getStatus() == SpotStatus.RESERVED) {
                reservedCount++;
            }
        }

        // Clear old data and update with new data
        parkingSpotChart.getData().clear();

        // Create new pie chart data with updated counts
        PieChart.Data occupied = new PieChart.Data("Occupied", occupiedCount);
        PieChart.Data free = new PieChart.Data("Free", freeCount);
        PieChart.Data reserved = new PieChart.Data("Reserved", reservedCount);

        // Add the updated data to the pie chart
        parkingSpotChart.getData().addAll(occupied, free, reserved);

        // Force layout update to reflect changes
        Platform.runLater(() -> {
            parkingSpotChart.layout();
        });
    }
	
}