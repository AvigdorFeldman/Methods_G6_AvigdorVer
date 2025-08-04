
package serverControllers;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * The {@code PDFReport} class provides methods to generate PDF reports from a CSV file and accompanying chart images.
 * This class supports reading data from a CSV file, inserting that data as a table into a PDF document,
 * and appending one or more chart images to the generated report.
 * The report includes a customizable title, creation date, and optional extra information.
 */
public class PDFReport {


	/**
	 * Generates a PDF report from the provided CSV file and chart images.
     * The report will contain a title, creation date, an optional extra information section, and a table populated
     * with the CSV data. Additionally, any provided chart images will be added to the PDF.
     *
     * @param csvFile         The CSV file containing data to be included in the report as a table.
     * @param chartImageFiles An array of image files to be included in the report (e.g., charts).
     * @param pdfOutputFile   The output PDF file where the generated report will be saved.
     * @param reportName      The title of the report, which will be displayed at the top of the PDF.
     * @param extraInfo       Optional extra information to be displayed below the title in the PDF.
     * @throws Exception If an error occurs while generating the PDF (e.g., file writing errors, invalid CSV data).
	 */
	@SuppressWarnings("resource")
	public static void generatePdfReport(File csvFile, File[] chartImageFiles, File pdfOutputFile, String reportName, String extraInfo) throws Exception {
	    System.out.println("Generating PDF report...");

	    // Check if CSV and image files exist and are readable
	    if (!csvFile.exists() || csvFile.length() == 0) {
	        System.out.println("CSV file is missing or empty.");
	        return;  // Exit if CSV is missing
	    }

	    try {
	        // Attempt to initialize PDF writer and document
	        PdfWriter writer = new PdfWriter(pdfOutputFile);
	        PdfDocument pdfDoc = new PdfDocument(writer);
	        Document document = new Document(pdfDoc);
	        PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");
	        // Step 1: Add the main report title
	        Paragraph title = new Paragraph(reportName)
	                .setFont(boldFont)       // Apply bold font
	                .setFontSize(24)         // Set font size to 24
	                .setTextAlignment(TextAlignment.CENTER);  // Center the text
	        document.add(title);
	        Paragraph dateParagraph = new Paragraph("Created on: " + new Date())
	                .setFontSize(12)          // Set font size to 12 for the date
	                .setTextAlignment(TextAlignment.RIGHT);  // Right-align the date
	        document.add(dateParagraph);
	        
	        if(extraInfo!=null) {
		        Paragraph par = new Paragraph(extraInfo)
		                .setFontSize(12)         // Set font size to 24
		                .setTextAlignment(TextAlignment.LEFT);  // Center the text
		        document.add(par);
		    }
	        // Step 1: Add CSV data as a table to the PDF
	        List<String[]> csvData = readCsv(csvFile);
	        if (csvData.isEmpty()) {
	            System.out.println("CSV data is empty. Cannot proceed with PDF generation.");
	            return;  // Exit if CSV data is empty
	        }

	        Table table = new Table(csvData.get(0).length);
	        for (String[] row : csvData) {
	            for (String cell : row) {
	                table.addCell(new Cell().add(new Paragraph(cell).setWidth(70)));
	            }
	        }

	        table.setWidth(csvData.get(0).length);
	        document.add(table);

	        // Step 2: Add chart image to the PD
	        if(chartImageFiles!=null) {
		        for(File file: chartImageFiles) {
			        if (file.exists() && file.length() > 0) {
			            ImageData imageData = ImageDataFactory.create(file.getAbsolutePath());
			            Image image = new Image(imageData);
			            document.add(image);
			            System.out.println("Image added to the PDF.");
			        } else {
			            System.out.println("Chart image file is missing or empty.");
			        }
		        }
	        }
	        // Step 3: Close the document and write content to the file
	        document.close();

	        // Debugging: Confirm the PDF file has been created
	        if (pdfOutputFile.exists() && pdfOutputFile.length() > 0) {
	            System.out.println("PDF file successfully created");
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


	
	/**
	 * Reads data from a CSV file and returns it as a list of string arrays, where each array represents a row of data.
     * The method handles CSV lines, ensuring quoted fields are processed correctly.
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
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
