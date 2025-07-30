package clientControllers;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.function.Supplier;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Utility class providing helper methods for client operations.
 * 
 * Includes methods for exporting TableView data to CSV files and waiting
 * for server responses with a timeout.
 */
public class Util {

    /**
     * Exports the contents of a JavaFX TableView to a CSV file.
     * 
     * Writes the table headers and all data rows to the specified file,
     * escaping quotes and commas as needed.
     *
     * @param table The TableView containing data to export.
     * @param file  The destination CSV file.
     * @throws Exception if an I/O error occurs during writing.
     */
    public static void exportToCSV(TableView<?> table, File file) throws Exception {
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
                    String cellText = (cell != null ? cell.toString() : "").replace("\"", "\"\"");
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
     * Periodically checks the provided Supplier<Boolean> until it returns true
     * or the timeout expires.
     *
     * @param timeoutMillis     Maximum time to wait in milliseconds.
     * @param responseCondition Supplier returning true when the response is ready.
     * @return true if the condition was met before the timeout.
     * @throws Exception if the timeout is exceeded.
     */
    public static boolean waitForServerResponse(long timeoutMillis, Supplier<Boolean> responseCondition) throws Exception {
        long startTime = System.currentTimeMillis();
        while (!responseCondition.get()) {
            Thread.sleep(10);
            if (System.currentTimeMillis() - startTime > timeoutMillis) {
                throw new Exception("Server response timed out after " + timeoutMillis + " milliseconds");
            }
        }
        return true;
    }
}