package serverControllers;

import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;

import jdbc.DataBaseQuery;

/**
 * A scheduled task runner that generates a monthly PDF report on the first day of each month.
 * 
 * This class continuously checks the system time and, when the first day of the month arrives at midnight,
 * it generates a new report using {@link MonthlyReport}. It stores the report in a "reports" directory.
 * After generating the report, it sleeps for 24 hours to avoid duplicate executions on the same day.
 * 
 */
public class MonthlyTaskRunner implements Runnable {
	private DataBaseQuery con;
	private int lastRunYear = -1;
	private Month lastRunMonth = null;
	
	/**
	 * Constructor of the class
	 * 
	 * @param con connection to database
	 */
	public MonthlyTaskRunner(DataBaseQuery con) {
		this.con = con;
	}
	
	/**
     * Continuously runs a loop to check whether it's the first day of the month at or just after midnight.
     * If so, it generates a monthly report PDF using the {@link MonthlyReport} class.
     * The report is saved to a file named "MonthlyReport_{Month}_{Year}.pdf" in the "reports" directory.
     * 
     * If the condition is met, the thread sleeps for 24 hours to avoid regenerating the report.
     * Otherwise, it sleeps for 1 hour before checking again
	 */
	@Override
    public void run() {
        while (true) {
            try {
            	LocalDateTime now = LocalDateTime.now();
				int currentYear = now.getYear();
				Month currentMonth = now.getMonth();
				if (now.getDayOfMonth() == 1 &&
					(currentMonth != lastRunMonth || currentYear != lastRunYear)) {
                    File reportsDir = new File("reports");
    				if (!reportsDir.exists()) {
    					reportsDir.mkdirs(); 
    				}
                    MonthlyReport report = new MonthlyReport(con,now.getYear(),now.getMonth());
                    File monthlyReportPdf = new File(reportsDir, "MonthlyReport_" + now.getMonth()+"_"+now.getYear() + ".pdf");
                    report.getPDF(monthlyReportPdf);
                    // Update last run
					lastRunMonth = currentMonth;
					lastRunYear = currentYear;
                    // Sleep for 1 hour to avoid multiple triggers on the same day
                    Thread.sleep(60 * 60 * 1000); // 1 hour
                } else {
                    // Sleep for 1 hour and check again
                    Thread.sleep(60 * 60 * 1000); // 1 hour
                }
            } catch (InterruptedException e) {
                System.out.println("Monthly task thread interrupted.");
                break;
            }
        }
    }

}
