package serverControllers;

import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jdbc.DataBaseQuery;

public class MonthlyTaskRunner implements Runnable {
	private DataBaseQuery con;
	public MonthlyTaskRunner(DataBaseQuery con) {
		this.con = con;
	}
	@Override
    public void run() {
        while (true) {
            try {
                LocalDateTime now = LocalDateTime.now();

                // If it's the 1st day of the month and around 00:00 (midnight)
                if ((now.getDayOfMonth() == 1 && now.toLocalTime().isBefore(LocalTime.of(0, 1)))) {
                    System.out.println("Running monthly task at " + now);
                    File reportsDir = new File("reports");
    				if (!reportsDir.exists()) {
    					reportsDir.mkdirs(); 
    				}
                    MonthlyReport report = new MonthlyReport(con,now.getYear(),now.getMonth());
                    File monthlyReportPdf = new File(reportsDir, "MonthlyReport_" + now.getMonth()+"_"+now.getYear() + ".pdf");
                    report.getPDF(monthlyReportPdf);
                    // Sleep for 1 day to avoid multiple triggers on the same day
                    Thread.sleep(24 * 60 * 60 * 1000); // 24 hours
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
