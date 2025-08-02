package clientControllers;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import logic.Parkingsession;
import logic.SendObject;

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
	private Button viewSubscribersReport;
	@FXML
	private Button createReportButton;
	@FXML
	private Button viewSubscriberReport;
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
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
					String defaultName = "SubscribersReport_" + LocalDateTime.now().format(formatter) + ".csv";

					File reportFile = new File("reports/" + defaultName);
					
					if (reportFile != null) {
						Util.exportToCSV(subscriberTable, reportFile);
						Util.sendReportFileToServer(reportFile, client, "File to server");						
						showAlert("Exported table to subscribers.csv!");
					}
				} catch (Exception ex) {
					showAlert("Failed to export CSV: " + ex.getMessage());
				}
			});
		}
		if (viewSubscribersReport != null) {
			viewSubscribersReport.setOnAction(e -> {
				try {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
					client.sendToServer(new SendObject<String>("Get Subscribers Report",LocalDateTime.now().format(formatter)));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			});
		}if (createReportButton != null) {
			createReportButton.setOnAction(e -> {
				try {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
					String defaultName = "SubscriberReport_" + subId + ".csv";

					File reportFile = new File("reports/" + defaultName);
					
					if (reportFile != null) {
						Util.exportToCSV(historyTable, reportFile);
						File imageFile = new File("reports/SubscriberHistorySessionsChart_" + subId +".png");
						Util.saveChartAsImage(barChart, imageFile);
						Util.sendReportFileToServer(reportFile, client, "File to server");	
						Util.sendReportFileToServer(imageFile, client, "File to server");	
						showAlert("Exported table to subscribers.csv!");
					}
				} catch (Exception ex) {
					showAlert("Failed to export CSV: " + ex.getMessage());
				}
			});
		}if (viewSubscriberReport != null) {
			viewSubscriberReport.setOnAction(e -> {
				String today=subId+",";
				for (logic.subscriber subscriber : allSubscribers) {
					if(subscriber.getId()==subId) {
						today += String.format("Sub. ID: %d\nSub. name: %s\nEmail: %s\nPhone: %s\nRole: %s\nLogged in: %s",subscriber.getId(),subscriber.getName(),subscriber.getEmail(),subscriber.getPhone(),subscriber.getRole(),subscriber.getLoggedIn());
					}
				}
				try {
					client.sendToServer(new SendObject<String>("Get Subscriber Report", today));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
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