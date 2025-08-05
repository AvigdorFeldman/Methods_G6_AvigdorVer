package clientControllers;

import java.io.File;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import logic.FileTransferMessage;
import logic.SendObject;

/**
 * Controller class for the admin screen.
 * Allows administrators to navigate to different reports, including reservation, subscriber, and active session reports.
 */
public class AdminScreenController extends Controller{
	
	@FXML Button openReservationReportButton;
	@FXML Button openSubscribersReportButton;
	@FXML Button openActiveSessionsReportButton;
	@FXML Button showPDF;
	@FXML Button backButton;
	@FXML private ComboBox<Integer> yearComboBox;
	@FXML private ComboBox<Month> monthComboBox;
	private boolean response = false;
	private final List<Month> months = Arrays.asList(Month.JANUARY, Month.FEBRUARY, Month.MARCH, Month.APRIL, Month.MAY, Month.JUNE,
			Month.JULY, Month.AUGUST, Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER, Month.DECEMBER);
    @FXML
    public void initialize() {
    	int currentYear = LocalDate.now().getYear();
	    yearComboBox.getItems().add(currentYear);  // Add the current year
	    for (int i = 1; i < 10; i++) {  // Add the previous 9 years (or adjust as needed)
	        yearComboBox.getItems().add(currentYear - i);
	    }
	    yearComboBox.setValue(currentYear);
	    Month currentMonth = LocalDate.now().getMonth();
	    for (Month month : months) {
			monthComboBox.getItems().add(month);
		}
	    monthComboBox.setValue(currentMonth);
	}
	
	/**
     * Sets a handler to execute when the back button is pressed.
     * @param backHandler a {@code Runnable} specifying the action to perform when navigating back
     */
	public void setBackHandler(Runnable backHandler) {
		this.backHandler = backHandler;
	}
	
	
	/**
     * Handles the action of opening the reservation report screen.
     * Loads the corresponding UI and sends a request to the server to retrieve all reservations.
     */
	@FXML
	public void openReservationReport() {
		try {
			setscreen("Reservations Report", "ReportReservationScreenUI.fxml", "AdminScreen.fxml", "Admin Menu",
					openReservationReportButton);
			// Send request to the server
			SendObject<String> request = new SendObject<>("Get", "all reservations");
			client.sendToServerSafely(request);
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
     * Handles the action of opening the subscribers report screen.
     * Loads the corresponding UI and sends a request to the server to retrieve all subscribers.
     */
	@FXML
	public void openSubscribersReport() {
		try {
			setscreen("Subscribers Report", "ReportSubscriberUI.fxml", "AdminScreen.fxml", "Admin Menu",
					openSubscribersReportButton);
			// Send request to the server
			SendObject<String> request = new SendObject<>("Get", "all subscribers");
			client.sendToServerSafely(request);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
     * Handles the action of opening the active sessions report screen.
     * Loads the corresponding UI and sends a request to the server to retrieve all active parking sessions.
     */
	@FXML
	public void openActiveSessionsReport() {
		try {
			setscreen("Active Sessions Report", "ReportActiveSessionsUI.fxml", "AdminScreen.fxml", "Admin Menu",
					openActiveSessionsReportButton);
			// Send request to the server
			SendObject<String> request = new SendObject<>("Get", "all parking sessions");
			client.sendToServerSafely(request);
			SendObject<String> request2 = new SendObject<>("Get", "all parking spots");
			client.sendToServerSafely(request2);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Shows the chosen monthly report if exist, otherwise shows an alert
	 */
	public void handleMonthlyReport() {
			SendObject<String> request = new SendObject<>("Get", "MonthlyReport_"+monthComboBox.getValue()+"_"+yearComboBox.getValue());
			client.sendToServerSafely(request);
			try{
				Util.waitForServerResponse(20000, ()->response);
				File file = new File("reports/","MonthlyReport_"+monthComboBox.getValue()+"_"+yearComboBox.getValue()+".pdf");
				if(!file.exists()) {
					throw new Exception();
				}
			}catch(Exception e) {
				ShowAlert.showAlert("Error", "Such file doesn't exist", AlertType.ERROR);
			}
	}

	/**
     * Handles the back button action to navigate back to the login screen.
     */
    @FXML protected void handleBackButton() {
		// swap the TableView scene back to the connect screen
    	handleButtonToLogin(backButton);
	}
    
    /**
     * Handles incoming server messages and shows monthly reports
     *
     * @param msg The server message containing monthly report.
     */
    public void handleServerMessage(Object message) {
		if(message instanceof SendObject<?>) {
			SendObject<?> sendobject = (SendObject<?>)message;
			if(sendobject.getObj() instanceof FileTransferMessage&&sendobject.getObjectMessage().equals("MonthlyReportPDF")) {
				Util.getPDF(message);
			}
			response = true;
		}
	}
    
}