package clientControllers;

import java.io.File;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import logic.Parkingsession;

/**
 * Controller for managing subscriber reports in a client-server parking management system.
 * This class extends the functionality of `ViewSubscriberController` by adding the ability to
 * export subscriber data to a CSV file and visualize subscriber activity through a bar chart.
 * 
 * The bar chart displays the number of parking sessions per month for the current year, allowing
 * users to analyze subscriber activity trends. Users can also export this data to a CSV file
 * using the export button.
 * 
 * The class utilizes JavaFX components for the user interface, including a bar chart for data
 * visualization and a button for exporting data.
 */
public class ReportSubscriberController extends ViewSubscriberController {
	@FXML
	private Button exportCsvButton;

	@FXML
	private BarChart<String, Number> barChart;
	@FXML
	private CategoryAxis xAxis;
	@FXML
	private NumberAxis yAxis;

	@Override
	public void initialize() {
		super.initialize(); // Setup from parent

		if (exportCsvButton != null) {
			exportCsvButton.setOnAction(e -> {
				try {
					FileChooser fileChooser = new FileChooser();
					fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
					File file = fileChooser.showSaveDialog(exportCsvButton.getScene().getWindow());
					if (file != null) {
						Util.exportToCSV(subscriberTable, file);
						showAlert("Exported table to subscribers.csv!");
					}
				} catch (Exception ex) {
					showAlert("Failed to export CSV: " + ex.getMessage());
				}
			});
		}
	}

    /**
     * Sets the list of historical sessions and updates the bar chart with subscriber activity data.
     *
     * @param history The list of parking sessions to display.
     */
	@Override
	public void setHistorySessions(List<Parkingsession> history) {
		super.setHistorySessions(history);
		Platform.runLater(() -> populateBarChart(history));
	}

    /**
     * Populates the bar chart to display subscriber activity by month.
     *
     * @param sessions The list of parking sessions to analyze.
     */
	private void populateBarChart(List<Parkingsession> sessions) {
		int[] monthly = new int[12];
		int currentYear = LocalDate.now().getYear();

		for (Parkingsession s : sessions) {
			LocalDate date = s.getInTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			if (date.getYear() == currentYear) {
				monthly[date.getMonthValue() - 1]++;
			}
		}

		XYChart.Series<String, Number> series = new XYChart.Series<>();
		for (int i = 0; i < 12; i++) {
			series.getData().add(new XYChart.Data<>(Month.of(i + 1).name(), monthly[i]));
		}
		barChart.getData().clear();
		barChart.getData().add(series);
	}

}