package clientControllers;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import logic.Parkingsession;


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
	@Override
	public void initialize() {
		super.initialize();

		if (exportCsvButton != null) {
			exportCsvButton.setOnAction(e -> {
				try {
					FileChooser fileChooser = new FileChooser();
					fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
					File file = fileChooser.showSaveDialog(exportCsvButton.getScene().getWindow());
					if (file != null) {
						Util.exportToCSV(sessionTable, file);
						showAlert("Exported table to " + file.getName());
					}
				} catch (Exception ex) {
					showAlert("Failed to export CSV: " + ex.getMessage());
				}
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
	}

    /**
     * Updates the line chart to display active sessions by hour.
     */
	private void updateLineChart() {
	    int[] hourlyCounts = new int[24];
	    for (Parkingsession session : allSessions) {
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
	}

}