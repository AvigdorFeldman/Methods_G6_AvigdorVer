
package serverControllers;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.itextpdf.io.image.*;

public class PDFReport {


	public static void generatePdfReport(File csvFile, File chartImageFile, File pdfOutputFile) throws Exception {
	    System.out.println("Generating PDF report...");
	    System.out.println("CSV File Path: " + csvFile.getAbsolutePath());
	    System.out.println("Image File Path: " + chartImageFile.getAbsolutePath());

	    // Check if CSV and image files exist and are readable
	    if (!csvFile.exists() || csvFile.length() == 0) {
	        System.out.println("CSV file is missing or empty.");
	        return;  // Exit if CSV is missing
	    }

	    if (!chartImageFile.exists() || chartImageFile.length() == 0) {
	        System.out.println("Image file is missing or empty.");
	        return;  // Exit if image is missing
	    }

	    try {
	        // Attempt to initialize PDF writer and document
	        PdfWriter writer = new PdfWriter(pdfOutputFile);
	        PdfDocument pdfDoc = new PdfDocument(writer);
	        Document document = new Document(pdfDoc);

	        // Step 1: Add CSV data as a table to the PDF
	        List<String[]> csvData = readCsv(csvFile);
	        if (csvData.isEmpty()) {
	            System.out.println("CSV data is empty. Cannot proceed with PDF generation.");
	            return;  // Exit if CSV data is empty
	        }

	        Table table = new Table(csvData.get(0).length);
	        for (String[] row : csvData) {
	            for (String cell : row) {
	                table.addCell(new Cell().add(new Paragraph(cell)));
	            }
	        }
	        document.add(table);
	        System.out.println("CSV data added to the PDF.");

	        // Step 2: Add chart image to the PDF
	        if (chartImageFile.exists() && chartImageFile.length() > 0) {
	            ImageData imageData = ImageDataFactory.create(chartImageFile.getAbsolutePath());
	            Image image = new Image(imageData);
	            document.add(image);
	            System.out.println("Image added to the PDF.");
	        } else {
	            System.out.println("Chart image file is missing or empty.");
	        }

	        // Step 3: Close the document and write content to the file
	        document.close();

	        // Debugging: Confirm the PDF file has been created
	        if (pdfOutputFile.exists() && pdfOutputFile.length() > 0) {
	            System.out.println("PDF file successfully created at: " + pdfOutputFile.getAbsolutePath());
	        } else {
	            System.out.println("Failed to create PDF file.");
	        }
	    } catch (IOException e) {
	        // If there was an error with the file writing process
	        System.out.println("Error writing the PDF file:");
	        e.printStackTrace();
	    } catch (Exception e) {
	        // Catch any other exceptions and print the details
	        System.out.println("Error generating PDF:");
	        e.printStackTrace();
	    }
	}


	
	private static List<String[]> readCsv(File file) throws Exception {
	    List<String[]> rows = new ArrayList<>();

	    // Check if file exists and is readable
	    if (!file.exists() || !file.canRead()) {
	        System.out.println("File does not exist or is not readable: " + file.getAbsolutePath());
	        return rows;  // Return empty list if the file can't be read
	    }

	    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
	        String line;
	        while ((line = reader.readLine()) != null) {
	            if (line.trim().isEmpty()) {
	                // Skip empty lines (optional, depends on your requirement)
	            	System.out.println("Skip");
	                continue;
	            }

	            try {
	                // Split line by commas, handle quoted strings
	                String[] fields = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
	                // Remove wrapping quotes (if any)
	                for (int i = 0; i < fields.length; i++) {
	                    fields[i] = fields[i].replaceAll("^\"|\"$", ""); 
	                }
	                rows.add(fields);
	            } catch (Exception e) {
	                System.out.println("Error parsing line: " + line);
	                e.printStackTrace();  // Optional: Print detailed error
	            }
	        }
	    } catch (IOException e) {
	        System.out.println("Error reading the CSV file: " + file.getAbsolutePath());
	        e.printStackTrace();
	    }

	    return rows;
	}

}
