package serverControllers;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.AreaBreakType;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import jdbc.DataBaseQuery;
import logic.*;

/**
 * Generates a comprehensive monthly PDF report that includes data and visualizations
 * for parking sessions, reservations, subscribers, and parking spots.
 * 
 * The report is generated using data fetched from the database via a {@link DataBaseQuery}
 * and includes:
 * 
 * Tabular data for each entity type
 * Bar charts and stacked bar charts for visual representation
 * 
 * Charts are rendered using JavaFX and embedded into the PDF using iText.
 * 
 */
public class MonthlyReport {

	private DataBaseQuery con;
	private int year;
	private Month month;
	private boolean firstSection = true;

	/**
     * Constructs a MonthlyReport for the given year and month using the specified database connection.
     *
     * @param con   Database connection object.
     * @param year  Year of the report.
     * @param month Month of the report.
	 */
	public MonthlyReport(DataBaseQuery con, int year, Month month) {
		this.con = con;
		this.year = year;
		this.month = month;
	}

	/**
     * Generates a PDF report file summarizing the parking activity of the specified month and year.
     *
     * It retrieves all relevant data from the database, filters by date, and builds a report containing:
     * Parking sessions
     * Reservations (used and canceled)
     * Parking spots usage
     * Late subscriber sessions
     * 
     * @param outputPdfFile File to save the PDF report to.
	 */
	@SuppressWarnings({ "unchecked" })
	public void getPDF(File outputPdfFile) {
		// TODO Auto-generated method stub
		List<Parkingsession> parkingsessionList = new ArrayList<>();
		List<ParkingSpot> parkingSpotList = new ArrayList<>();
		List<subscriber> subscriberList = new ArrayList<>();
		List<Reservation> reservationList = new ArrayList<>();
		try {
			SendObject<Serializable> result = SendObjectHandler
					.sendObjectHandle(new SendObject<String>("Get", "all parking sessions"), con);
			parkingsessionList = (List<Parkingsession>) result.getObj();
			result = SendObjectHandler.sendObjectHandle(new SendObject<String>("Get", "all parking spots"), con);
			parkingSpotList = (List<ParkingSpot>) result.getObj();
			result = SendObjectHandler.sendObjectHandle(new SendObject<String>("Get", "all subscribers"), con);
			subscriberList = (List<subscriber>) result.getObj();
			result = SendObjectHandler.sendObjectHandle(new SendObject<String>("Get", "all reservations"), con);
			reservationList = (List<Reservation>) result.getObj();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		// Filter by year and month
		parkingsessionList = parkingsessionList.stream().filter(ps -> {
			LocalDate localDate = ps.getInTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			return localDate.getYear() == year && localDate.getMonth() == month;
		}).collect(Collectors.toList());

		reservationList = reservationList.stream()
				.filter(res -> res.getDate().getYear() == year && res.getDate().getMonth() == month)
				.collect(Collectors.toList());

		try {
			PdfWriter writer = new PdfWriter(outputPdfFile);
			PdfDocument pdfDoc = new PdfDocument(writer);
			Document document = new Document(pdfDoc);
			PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");

			// Title
			document.add(new Paragraph("Monthly Report: " + month + " " + year).setFont(boldFont).setFontSize(20)
					.setTextAlignment(TextAlignment.CENTER));
			document.add(
					new Paragraph("Generated on: " + new Date()).setFontSize(10).setTextAlignment(TextAlignment.RIGHT));
			// BarCharts creations
			Image parkingsessionBarChart = createSessionsPerDayChartFX(parkingsessionList);
			Image reservationBarChart = createReservationsPieChartFX(reservationList);
			Image parkingSpotsParkingSessionChart = createParkingSessionsPerSpotChart(parkingsessionList,
					parkingSpotList);
			Image lateSubscriberChart = createLateSubscribersChartFX(parkingsessionList, subscriberList);
			// Sections - Tables
			addSection(document, "Parking Sessions", boldFont, parkingsessionList, parkingsessionBarChart);
			addSection(document, "Reservations", boldFont, reservationList, reservationBarChart);
			addSection(document, "Parking Spots", boldFont, parkingSpotList, parkingSpotsParkingSessionChart);
			addSection(document, "Subscribers", boldFont, subscriberList, lateSubscriberChart);

			document.close();
			System.out.println("Monthly PDF report generated: " + outputPdfFile.getAbsolutePath());
		} catch (Exception e) {
			System.out.println("Failed to generate PDF:");
			e.printStackTrace();
		}
	}

	/**
     * Adds a section to the PDF document including a title, a table generated from the data,
     * and an optional chart image.
     *
     * @param document PDF document to add content to.
     * @param title    Section title.
     * @param boldFont Font for section titles and headers.
     * @param list     Data list to display in table form.
     * @param chart    Chart image to include in the section (can be null).
     * @param <T>      The type of data in the list.
     */
	private <T> void addSection(Document document, String title, PdfFont boldFont, List<T> list, Image chart) {
		if (!firstSection) {
			document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
		}
		firstSection = false;

		document.add(new Paragraph(title).setFont(boldFont).setFontSize(16).setMarginTop(15)
				.setTextAlignment(TextAlignment.LEFT));

		if (list == null || list.isEmpty()) {
			document.add(new Paragraph("No data available."));
			return;
		}

		T firstItem = list.get(0);

		List<Field> allFields = new ArrayList<>();

		// Collect classes from topmost superclass down to the actual class
		List<Class<?>> classHierarchy = new ArrayList<>();
		Class<?> current = firstItem.getClass();
		while (current != null && current != Object.class) {
			classHierarchy.add(0, current); // Insert at start to reverse the order
			current = current.getSuperclass();
		}

		// Now collect fields in order: superclass first, subclass later
		for (Class<?> cls : classHierarchy) {
			allFields.addAll(Arrays.asList(cls.getDeclaredFields()));
		}

		// Exclude the "history" field if the type is subscriber
		if (firstItem instanceof subscriber) {
			allFields.removeIf(field -> field.getName().equalsIgnoreCase("history"));
			allFields.removeIf(field -> field.getName().equalsIgnoreCase("loggedIn"));
		}

		List<String> fieldNames = allFields.stream().map(Field::getName).collect(Collectors.toList());

		Table table = new Table(fieldNames.size());

		// Add header cells
		for (String header : fieldNames) {
			table.addHeaderCell(new Cell().add(new Paragraph(header).setFont(boldFont).setFontSize(9)).setPadding(3)
					.setTextAlignment(TextAlignment.LEFT));
		}

		// Add data rows
		for (T item : list) {
			for (String field : fieldNames) {
				try {
					Field f = allFields.stream().filter(fl -> fl.getName().equals(field)).findFirst().orElse(null);

					if (f != null) {
						f.setAccessible(true);
						Object value = f.get(item);
						table.addCell(
								new Cell().add(new Paragraph(String.valueOf(value)).setFontSize(8)).setPadding(2));
					} else {
						table.addCell(new Cell().add(new Paragraph("N/A")));
					}
				} catch (Exception e) {
					table.addCell(new Cell().add(new Paragraph("N/A")));
				}
			}
		}

		table.setWidth(UnitValue.createPercentValue(100));
		document.add(table);
		// Add chart image if provided
		if (chart != null) {
			chart.setAutoScale(true);
			chart.setMarginTop(15);
			document.add(chart);
		}

	}

	/**
     * Renders a bar chart using JavaFX based on a given data map.
     *
     * @param title       Chart title.
     * @param xAxisLabel  Label for the X-axis.
     * @param yAxisLabel  Label for the Y-axis.
     * @param data        Data to plot.
     * @param chartWidth  Width of the chart.
     * @param chartHeight Height of the chart.
     * @return Image object to embed in a PDF.
     * @throws Exception If JavaFX rendering fails.
	 */
	@SuppressWarnings("unused")
	private Image renderBarChart(String title, String xAxisLabel, String yAxisLabel, Map<String, Integer> data,
			int chartWidth, int chartHeight) throws Exception {
		final CountDownLatch latch = new CountDownLatch(1);
		final BufferedImage[] chartImage = new BufferedImage[1];

		Platform.runLater(() -> {
			try {
				CategoryAxis xAxis = new CategoryAxis();
				xAxis.setLabel(xAxisLabel);

				int max = data.values().stream().max(Integer::compareTo).orElse(1);
				NumberAxis yAxis = new NumberAxis(0, max + 1, 1);
				yAxis.setLabel(yAxisLabel);
				yAxis.setTickUnit(1);
				yAxis.setMinorTickCount(0);

				BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
				barChart.setTitle(title);
				barChart.setCategoryGap(10);
				barChart.setBarGap(0);
				barChart.setPrefSize(chartWidth, chartHeight);

				XYChart.Series<String, Number> series = new XYChart.Series<>();
				for (Map.Entry<String, Integer> entry : data.entrySet()) {
					series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
				}

				barChart.getData().add(series);

				Scene scene = new Scene(barChart);
				WritableImage fxImage = barChart.snapshot(new SnapshotParameters(), null);
				chartImage[0] = javafx.embed.swing.SwingFXUtils.fromFXImage(fxImage, null);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				latch.countDown();
			}
		});

		latch.await();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(chartImage[0], "png", baos);
		byte[] imageBytes = baos.toByteArray();
		ImageData imageData = ImageDataFactory.create(imageBytes);

		return new Image(imageData).setWidth(UnitValue.createPercentValue(100));
	}

	/**
	 * Renders a pie chart using JavaFX based on a given data map.
	 *
	 * @param title  Chart title.
	 * @param data   Data to plot.
	 * @param width  Width of the chart.
	 * @param height Height of the chart.
	 * @return Image object to embed in a PDF.
	 * @throws Exception If JavaFX rendering fails.
	 */
	@SuppressWarnings("unused")
	private Image renderPieChart(String title, Map<String, Integer> data, int width, int height) throws Exception {
	    final CountDownLatch latch = new CountDownLatch(1);
	    final BufferedImage[] chartImage = new BufferedImage[1];

	    Platform.runLater(() -> {
	        try {
	            PieChart pieChart = new PieChart();
	            pieChart.setTitle(title);

	            for (Map.Entry<String, Integer> entry : data.entrySet()) {
	                PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue());
	                pieChart.getData().add(slice);
	            }

	            Scene scene = new Scene(pieChart, width, height);
	            WritableImage fxImage = pieChart.snapshot(new SnapshotParameters(), null);
	            chartImage[0] = javafx.embed.swing.SwingFXUtils.fromFXImage(fxImage, null);
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            latch.countDown();
	        }
	    });

	    latch.await();
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ImageIO.write(chartImage[0], "png", baos);
	    byte[] imageBytes = baos.toByteArray();
	    ImageData imageData = ImageDataFactory.create(imageBytes);
	    return new Image(imageData).setWidth(UnitValue.createPercentValue(100));
	}


	/**
     * Creates a bar chart showing how many parking sessions occurred each day of the month.
     *
     * @param sessions List of parking sessions.
     * @return Image of the chart.
     * @throws Exception If chart rendering fails.
	 */
	private Image createSessionsPerDayChartFX(List<Parkingsession> sessions) throws Exception {
		YearMonth yearMonth = YearMonth.of(year, month.getValue());
		int daysInMonth = yearMonth.lengthOfMonth();
		Map<String, Integer> dayCounts = new LinkedHashMap<>();

		for (int i = 1; i <= daysInMonth; i++) {
			dayCounts.put(String.valueOf(i), 0);
		}

		for (Parkingsession session : sessions) {
			LocalDate date = session.getInTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			if (date.getYear() == year && date.getMonthValue() == month.getValue()) {
				String day = String.valueOf(date.getDayOfMonth());
				dayCounts.put(day, dayCounts.get(day) + 1);
			}
		}

		return renderBarChart("Parking Sessions Per Day - " + month + " " + year, "Day", "Sessions", dayCounts, 800,
				600);
	}

	/**
	 * Creates a pie chart showing the distribution of reservations (used vs canceled) for the month.
	 *
	 * @param reservations List of reservations.
	 * @return Image of the chart.
	 * @throws Exception If chart rendering fails.
	 */
	private Image createReservationsPieChartFX(List<Reservation> reservations) throws Exception {
	    Map<String, Integer> counts = new LinkedHashMap<>();
	    counts.put("Used", 0);
	    counts.put("Canceled", 0);

	    for (Reservation reservation : reservations) {
	        if (reservation.getStartTime() == null) {
	            counts.put("Canceled", counts.get("Canceled") + 1);
	        } else {
	            counts.put("Used", counts.get("Used") + 1);
	        }
	    }

	    return renderPieChart("Reservations Distribution - " + month + " " + year, counts, 800, 600);
	}

	/**
     * Creates a bar chart of late parking session exits grouped by subscriber.
     *
     * @param sessions    List of parking sessions.
     * @param subscribers List of subscribers.
     * @return Image of the chart.
     * @throws Exception If chart rendering fails.
	 */
	private Image createLateSubscribersChartFX(List<Parkingsession> sessions, List<subscriber> subscribers)
			throws Exception {
		Map<Integer, Integer> lateCounts = new TreeMap<>();
		for (subscriber s : subscribers) {
			lateCounts.put(s.getId(), 0);
		}

		for (Parkingsession session : sessions) {
			LocalDate date = session.getInTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			if (date.getYear() == year && date.getMonthValue() == month.getValue() && session.isLate()) {
				int subId = session.getSubscriberId();
				lateCounts.put(subId, lateCounts.getOrDefault(subId, 0) + 1);
			}
		}

		Map<String, Integer> displayData = new LinkedHashMap<>();
		for (Map.Entry<Integer, Integer> entry : lateCounts.entrySet()) {
			displayData.put("ID " + entry.getKey(), entry.getValue());
		}

		return renderBarChart("Late Vehicle Exits by Subscribers - " + month + " " + year, "Subscriber", "Late Exits",
				displayData, 800, 500);
	}

	/**
     * Creates a bar chart showing the number of sessions that occurred at each parking spot.
     *
     * @param sessions List of parking sessions.
     * @param spots    List of parking spots.
     * @return Image of the chart.
     * @throws Exception If chart rendering fails.
	 */
	private Image createParkingSessionsPerSpotChart(List<Parkingsession> sessions, List<ParkingSpot> spots)
			throws Exception {
		Map<Integer, Integer> spotCounts = new TreeMap<>();
		for (ParkingSpot spot : spots) {
			spotCounts.put(spot.getSpotId(), 0);
		}

		for (Parkingsession session : sessions) {
			LocalDate date = session.getInTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			if (date.getYear() == year && date.getMonthValue() == month.getValue()) {
				int spotId = session.getSpotId();
				spotCounts.put(spotId, spotCounts.getOrDefault(spotId, 0) + 1);
			}
		}

		Map<String, Integer> displayData = new LinkedHashMap<>();
		for (Map.Entry<Integer, Integer> entry : spotCounts.entrySet()) {
			displayData.put("Spot " + entry.getKey(), entry.getValue());
		}

		return renderBarChart("Parking Sessions per Spot - " + month + " " + year, "Parking Spot", "Sessions",
				displayData, 800, 400);
	}

}


