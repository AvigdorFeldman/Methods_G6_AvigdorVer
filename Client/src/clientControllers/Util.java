package clientControllers;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.chart.Chart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.WritableImage;
import logic.FileTransferMessage;
import logic.SendObject;
import ocsf.client.BParkClient;

/**
 * Utility class providing helper methods for client operations.
 * 
 * Includes methods for exporting TableView data to CSV files and waiting for
 * server responses with a timeout.
 */
public class Util {

	/**
	 * Exports the contents of a JavaFX TableView to a CSV file.
	 * 
	 * Writes the table headers and all data rows to the specified file, escaping
	 * quotes and commas as needed.
	 *
	 * @param table The TableView containing data to export.
	 * @param file  The destination CSV file.
	 * @throws Exception if an I/O error occurs during writing.
	 */
	public static void exportToCSV(TableView<?> table, File file) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm"); // hour date format

		try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
			// Header
			for (int i = 0; i < table.getColumns().size(); i++) {
				TableColumn<?, ?> col = table.getColumns().get(i);
				writer.print(col.getText());
				if (i < table.getColumns().size() - 1)
					writer.print(",");
			}
			writer.println();

			// Data rows
			for (Object item : table.getItems()) {
				for (int i = 0; i < table.getColumns().size(); i++) {
					TableColumn col = table.getColumns().get(i);
					Object cell = col.getCellData(item);

					String cellText = "";
					if (cell != null) {
						if (cell instanceof Date) {
							cellText = dateFormat.format((Date) cell);
						} else {
							cellText = cell.toString();
						}
					}

					cellText = cellText.replace("\"", "\"\"");
					if (cellText.contains(",") || cellText.contains("\""))
						cellText = "\"" + cellText + "\"";

					writer.print(cellText);
					if (i < table.getColumns().size() - 1)
						writer.print(",");
				}
				writer.println();
			}
		}
	}

	/**
	 * Waits for a server response condition to become true, with a timeout.
	 * 
	 * Periodically checks the provided Supplier<Boolean> until it returns true or
	 * the timeout expires.
	 *
	 * @param timeoutMillis     Maximum time to wait in milliseconds.
	 * @param responseCondition Supplier returning true when the response is ready.
	 * @return true if the condition was met before the timeout.
	 * @throws Exception if the timeout is exceeded.
	 */
	public static boolean waitForServerResponse(long timeoutMillis, Supplier<Boolean> responseCondition)
			throws Exception {
		long startTime = System.currentTimeMillis();
		while (!responseCondition.get()) {
			Thread.sleep(10);
			if (System.currentTimeMillis() - startTime > timeoutMillis) {
				throw new Exception("Server response timed out after " + timeoutMillis + " milliseconds");
			}
		}
		return true;
	}

	/**
	 * Sends file to server
	 * 
	 * @param file
	 */
	public static void sendReportFileToServer(File file, BParkClient client, String str) {
		try {
			if (file == null)
				throw new Exception();
			byte[] data = Files.readAllBytes(file.toPath());
			FileTransferMessage message = new FileTransferMessage(file.getName(), data);
			client.sendToServerSafely(new SendObject<FileTransferMessage>(str, message));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void saveChartAsImage(Chart chart, File file) {
		try {
			// Snapshot the chart to an image
			WritableImage image = chart.snapshot(null, null);

			// Convert JavaFX image to BufferedImage and save to file
			ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);

			System.out.println("Chart saved to " + file.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void getPDF(Object msg) {
		FileTransferMessage fileMsg = (FileTransferMessage) ((SendObject<?>) msg).getObj();
		File reportsDir = new File("reports");
		if (!reportsDir.exists())
			reportsDir.mkdirs();
		File reportFile = new File(reportsDir, fileMsg.getFilename());
		try (FileOutputStream fos = new FileOutputStream(reportFile)) {
			fos.write(fileMsg.getData());
			Desktop.getDesktop().open(reportFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}