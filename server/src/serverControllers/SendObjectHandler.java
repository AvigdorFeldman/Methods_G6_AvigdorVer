package serverControllers;

import logic.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jdbc.DataBaseQuery;

/**
 * Handles the processing of SendObject requests, routing them to appropriate handlers
 * based on the action string and the type of the embedded object.
 */
public class SendObjectHandler {
	/**
	 * Main method to process incoming SendObject requests.
     * 
     * @param obj The received SendObject containing action and payload
     * @param con A DataBaseQuery instance for database operations
     * @return A response SendObject with results or messages
     * @throws Exception if action is null or unprocessable
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Serializable, T1 extends Serializable> SendObject<T1> sendObjectHandle(SendObject<T> obj,
			DataBaseQuery con) throws Exception {
		String action = obj.getObjectMessage();
		T object = obj.getObj();
		if (action == null) {
			throw new Exception("Null Action was received");
		} else if (object instanceof String) {
			// Uses handleStringType() method to handle String type objects
			if (action.contains("Get") || action.contains("Check")) {
				Object genericObject = handleStringType(action, (String) object, con);
				return replyDefiner(genericObject);
			}
		} else if (object instanceof Integer) {
			// Uses handleIntegerType() method to handle Integer type objects
			Object genericObject = handleIntegerType(action, (Integer) object, con);
			return replyDefiner(genericObject);
		} else if (action.contains("connect")) {
			if (object == null) {
				// For guest screen
				double percent = con.getPrecentageAvailableSpaceFromDatabase();
				return new SendObject<T1>("Percent", (T1) (Double) percent);
			}
			// Connects the subscriber to the client application
			Object genericObject = handleGetAction(object, con);
			return replyDefiner(genericObject);
		}else if(action.contains("loggedoff")||action.contains("login")) {
			if(object!=null)
				con.updateUserInDatabase((subscriber)object);
		}else if (action.contains("Update")) {
			if (action.contains("time in session")) {
				// Manages time extension update of parking sessions
				Parkingsession session = (Parkingsession) obj.getObj();
				if (con.checkExtendTimeParkingsessionWithAllReservations(session)) {
					handleUpdateAction(object, con);
					return new SendObject<T1>("Time Extension", (T1) "Accapted");
				} else {
					return new SendObject<T1>("Time Extension", (T1) "Not Accapted");
				}

			} else {
				// Updates other objects in DB
				Object genericObject = handleUpdateAction(object, con);
				return replyDefiner(genericObject);
			}
		} else if (action.contains("Create")) {
			// Uses handleCreateAction() to put new values in the Database
			Object genericObject = handleCreateAction(object, con);
			return replyDefiner(genericObject);
		} else if (action.contains("Send")) {
			if (object instanceof subscriber) {
				// Uses handleSendAction() to send Email to subscribers
				handleSendAction(action, (subscriber) object, con);
			} else {
				System.err.println("Unknown SendObject received: action = " + action + ", object = " + object);
				throw new Exception("No send option is capable without subscriber object");
			}
			
		} else if(action.contains("File to server")) { // Creates reports directory and creates a new file in it
			if (object instanceof FileTransferMessage) {
				FileTransferMessage fileMsg = (FileTransferMessage)object; 
			    File reportsDir = new File("reports");
			    if (!reportsDir.exists()) reportsDir.mkdirs();

			    File reportFile = new File(reportsDir, fileMsg.getFilename());
			    try (FileOutputStream fos = new FileOutputStream(reportFile)) {
			        fos.write(fileMsg.getData());
			    }
			}
		}else { // Default for not unknown SendObject
			System.err.println("Unknown SendObject received: action = " + action + ", object = " + object);
			throw new Exception("No possible classes were chosen");
		}// Default
		return null;
	}

	/**
	 * Defines a reply based on the type of the object returned.
     * 
     * @param <T1> The type of the response object, must be Serializable
     * @param genericObject Object to evaluate
     * @return A new SendObject instance containing a descriptive message and object
	 */
	@SuppressWarnings("unchecked")
	private static <T1 extends Serializable> SendObject<T1> replyDefiner(Object genericObject) {
		String reply = "received object";
		if (genericObject != null) {
			if (genericObject instanceof Boolean) {
				reply = "received Boolean";
			} else if (genericObject instanceof Parkingsession) {
				reply = "received Parkingsession";
			} else if (genericObject instanceof subscriber) {
				reply = "received subscriber";
			} else if (genericObject instanceof ParkingSpot) {
				reply = "received ParkingSpot";
			} else if (genericObject instanceof Reservation) {
				reply = "received Reservation";
			} else if (genericObject instanceof SendObject) {

				return (SendObject<T1>) genericObject;
			}

		}
		// Default message (returns null)
		return new SendObject<T1>(reply, (T1) genericObject);
	}

	/**
     * Handles actions where the object is an Integer.
     * 
     * @param <T1> The return type, must be Serializable
     * @param action The specified action to perform
     * @param intObject The Integer object received
     * @param con A DataBaseQuery instance
     * @return A SendObject containing result based on integer processing
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Serializable, T1 extends Serializable> SendObject<T1> handleIntegerType(String action,
			Integer intObject, DataBaseQuery con) {
		if (action.contains("Check")) {
			if (action.equals("Check new Parking Code")) {
				// Returns boolean if the code is being used
				boolean isUsed = true;
				int code = intObject;
				isUsed = con.checkParkingCodeInAllActiveSessionsInDatabase(code);
				return new SendObject<T1>("isUsed", (T1) (Boolean) isUsed);
			} else if (action.equals("Check received Parking Code")) {
				// Returns the session by code if exists
				Parkingsession mySession = null;
				int parkingcode = intObject;
				mySession = con.getActiveParkingsessionWithThatCodeFromDatabase(parkingcode);
				return new SendObject<T1>("Parkingsession from code", (T1) (Parkingsession) mySession);
			}
		} else if (action.contains("Update")) {
			if (action.contains("Update spot to Free")) {
				// Update spot to free
				int spotId = intObject;
				ParkingSpot spot = new ParkingSpot(spotId,SpotStatus.FREE);
				con.updateParkingSpotInDatabase(spot);
			}
		} else if (action.contains("Get")) {
			if (action.contains("SubscribersResesrvations")) {
				// Return list of reservations of the subscriber intObject
				List<Reservation> reservationListOfSubscriber = new ArrayList<>();
				reservationListOfSubscriber = con.getReservationListOfSubscriberbyIdFromDatabase(intObject);
				return new SendObject<T1>("Reservation list of subscriber",
						(T1) (List<Reservation>) reservationListOfSubscriber);
			} else if (action.contains("history")) {
				// Return list of past parking sessions of the subscriber intObject
				List<Parkingsession> historyParkingsessionsListOfSubscriber = new ArrayList<>();
				int subscriberId = intObject;
				historyParkingsessionsListOfSubscriber = con
						.gethistoryParkingsessionsListOfSubscriberbyIdFromDatabase(subscriberId);
				if (historyParkingsessionsListOfSubscriber == null) {
				}
				// send back the list
				return new SendObject<T1>("Parkingsession list of subscriber",
						(T1) (List<Parkingsession>) historyParkingsessionsListOfSubscriber);
			} else if (action.contains("Active Parkingsessions")) {
				// Return list of active parking sessions of the subscriber intObject
				List<Parkingsession> activeParkingsessionsListOfSubscriber = new ArrayList<>();
				int subscriberId = intObject;
				activeParkingsessionsListOfSubscriber = con
						.getActiveParkingsessionsListOfSubscriberbyIdFromDatabase(subscriberId);
				// send back the list
				return new SendObject<T1>("Active Sessions",
						(T1) (List<Parkingsession>) activeParkingsessionsListOfSubscriber);
			} else if (action.contains("Parkingsession")) {
				// Return parking session by id
				Parkingsession session = null;
				int sessionId = intObject;
				session = con.getParkingsessionById(sessionId);
				if (session != null) {
					return new SendObject<T1>("Session found", (T1) session);
				} else {
					return new SendObject<T1>("Session found:", (T1) "False");
				}
			} else if (action.contains("reservation with code")) {
				Reservation reservation;
				int reservationCode = intObject;
				reservation = con.getReservationByCode(reservationCode);
				return new SendObject<T1>("Received reservation", (T1) reservation);
			} else if (action.contains("reservation with id")) {
				Reservation reservation;
				int reservationId = intObject;
				reservation = con.getReservationById(reservationId);
				if(reservation==null)
					return new SendObject<T1>("No such", (T1) "Reservation");
				else
					return new SendObject<T1>("Received reservation", (T1) reservation);
			}
		}
		return null;
	}

	/**
     * Handles Email/SMS sending for subscribers based on action.
     * 
     * @param action The send action
     * @param object The subscriber object
     * @param con Database connector
     * @throws Exception if the email is invalid or no active session found
	 */
	private static <T extends Serializable> void handleSendAction(String action, subscriber object, DataBaseQuery con)
			throws Exception {
		if (action.contains("Email/SMS")) {
			String to = object.getEmail();
			// Email validation
			if (to == null || !to.contains("@"))
				throw new Exception("Subscriber doesn't have a legal Email");
			else if (action.equals("Send late message by Email/SMS")) {
				// Send Email about being late
				SendEmail.sendMail(to, "Late retrivel!",
						"Hello,\nWe inform you picked up your vehicle later than expected.\nNote that in the future it might incur additional charges.");
			} else if (action.equals("Send Parking Code by Email/SMS")) {
				// Send parking code to subscriber by Email
				int parkingCode = 1000000;
				parkingCode = con.getSubscriberLastActiveParkingsessionParkingCode(object.getId());
				if (parkingCode == 1000000) {
					throw new Exception("No active parking sessions for the user");
				}
				SendEmail.sendMail(to, "Parking Code reminder", String.format(
						"Hello,\nYour last Parking Code in your last/current active session is: %d\nPlease enter the code you received in the app.",
						parkingCode));
			}
		}

	}

	/**
     * Handles actions where the object is a String.
     * 
     * @param <T1> Return type, must be Serializable
     * @param action The specified action to perform
     * @param object The String object received
     * @param con A DataBaseQuery instance
     * @return A SendObject containing result based on string processing
     * @throws Exception if database query fails
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Serializable, T1 extends Serializable> SendObject<T1> handleStringType(String action,
			String object, DataBaseQuery con) throws Exception {
		if (action.contains("Check") && object.contains("Availability")) {
			// Returns availablity boolean
			double availablePrecentage = 0; 
			availablePrecentage = con.getPrecentageAvailableSpaceFromDatabase();
			if (availablePrecentage > 0)
				return new SendObject<T1>("Availability", (T1) (Boolean) true);
			else
				return new SendObject<T1>("Availability", (T1) (Boolean) false);
		} else if (action.contains("Get")) {
			if (object.equals("Free spot")) {
				ParkingSpot spot;
				DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
				spot = con.getFreeParkingSpotFromDatabase(LocalDate.now(), LocalTime.now().format(timeFormatter),
						LocalTime.now().plusHours(4).format(timeFormatter)).get(0);
				if (spot != null) {
					// Returns an available spot
					spot.setStatus(SpotStatus.OCCUPIED);
					handleUpdateAction(spot, con);
					return new SendObject<T1>("new Spot", (T1) (ParkingSpot) spot);
				}

			} else if (object.equals("all reservations")) {
				// Returns a list of all resrervations
				List<Reservation> allReservationList = new ArrayList<>();
				allReservationList = con.getAllReservationList();
				return new SendObject<T1>("Received all reservations", (T1) (List<Reservation>) allReservationList);
			} else if (object.equals("all subscribers")) {
				// Returns a list of all subscribers
				List<subscriber> allSubscribersList = new ArrayList<subscriber>();
				allSubscribersList = con.getAllSubscribersList();
				return new SendObject<T1>("Received all subscribers", (T1) (List<subscriber>) allSubscribersList);
			} else if (object.equals("active parking sessions")) {
				// Returns a list of all parkingsessions
				List<Parkingsession> allActiveParkingsessions = new ArrayList<Parkingsession>();
				allActiveParkingsessions = con.getAllActiveParkingsession();
				return new SendObject<T1>("Received active parking sessions",
						(T1) (List<Parkingsession>) allActiveParkingsessions);
			}else if(object.equals("all parking spots")){
				List<ParkingSpot> allParkingSpots = new ArrayList<ParkingSpot>();
				allParkingSpots = con.getAllParkingSpots();
				return new SendObject<T1>("Received active parking sessions",
						(T1) (List<ParkingSpot>) allParkingSpots);
			}else if(action.contains("Get ActiveSessions")) {
				File reportsDir = new File("reports"); // Relative path to your reports directory
				if (!reportsDir.exists()) {
				    reportsDir.mkdirs(); // Ensure the directory exists
				}
				File csvFile = new File(reportsDir, "ActiveSessionsReport_" + object.toString() + ".csv");
				File pngFile1 = new File(reportsDir, "ActiveSessionsChart_" + object.toString() + ".png");
				File pngFile2 = new File(reportsDir, "ParkingSpotsChart_" + object.toString() + ".png");
				File[] images = new File[]{pngFile1,pngFile2};
				File ActiveSessionsPdf = new File(reportsDir, "ActiveSessionsReport_"+object.toString()+".pdf");
				PDFReport.generatePdfReport(csvFile,images,ActiveSessionsPdf, "Active Sessions "+object.toString());
				byte[] data = Files.readAllBytes(ActiveSessionsPdf.toPath());
				FileTransferMessage message = new FileTransferMessage(ActiveSessionsPdf.getName(), data);
				return new SendObject<T1>("ActiveSessionsPDF",(T1)(FileTransferMessage)message);
			}

		}
		// Default or fallback return value
		return new SendObject<T1>("Invalid request", null);
	}

	/**
     * Retrieves an object from the database based on the subscriber details.
     * 
     * @param object<T>, must be Serializable. The input object to process (subscriber expected)
     * @param <T1> Return type, must be Serializable.
     * @param con Database connector
     * @return Retrieved object or error message
     * @throws Exception if retrieval fails
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Serializable, T1 extends Serializable> T1 handleGetAction(T object, DataBaseQuery con)
			throws Exception {
		try {
			if (object instanceof subscriber) {
				subscriber user = (subscriber) object;
				subscriber subscriber = null;
				if (user.getCode() > 100000 && user.getName() != null) {
					// Retrieve User from the database using Code
					subscriber = con.getUserUsingCodeFromDatabase(user.getCode(), user.getName());
				} else if (user.getTag() != null) {
					// Retrieve User from the database using Tag
					subscriber = con.getUserUsingTagFromDatabase(user.getTag());
				}
				if (subscriber != null)
					return (T1) subscriber;
			} else {
				return (T1) new SendObject<T1>("Error", (T1) "Name or Code or Tag is incorrect");
			}
			return null;
		} catch (Exception e) {// SQLException e
			throw new Exception("Error retrieving data from database", e);
		}
	}

	/**
     * Updates objects in the database.
     * 
     * @param object <T> Input object type, must extend Serializable, Object to be updated
     * @param <T1> Return object type, must extend Serializable
     * @param con Database connector
     * @return A SendObject response if applicable, else null
     * @throws Exception if database update fails
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Serializable, T1 extends Serializable> SendObject<T1> handleUpdateAction(T object, DataBaseQuery con) throws Exception {
		try {
			if (object instanceof subscriber) {
				subscriber user = (subscriber) object;
				// Update User In the database using received object
				con.updateUserInDatabase(user);
				if(user.getEmail().equals(con.getUserUsingTagFromDatabase(user.getTag()).getEmail()))
					return new SendObject<T1>("Subscriber",(T1)"updated successfully");
				return new SendObject<T1>("Subscriber",(T1)"could not be updated, another user has this email");
			} else if (object instanceof Parkingsession) {
				Parkingsession session = (Parkingsession) object;
				// Update Parkingsession In the database using received object
				con.updateParkingsessionInDatabase(session);
			} else if (object instanceof ParkingSpot) {
				ParkingSpot spot = (ParkingSpot) object;
				// Update ParkingSpot In the database using received object
				con.updateParkingSpotInDatabase(spot);
			} else if (object instanceof Object[]) {
				if(((Object[])object)[1] instanceof Reservation) {
					Object objectArr[] = (Object[])object;
					int reservationNum = (Integer) objectArr[0];
					Reservation reservation = (Reservation) objectArr[1];
					// Update Reservation In the database using received object
					con.updateReservationInDatabase(reservationNum,reservation);
					return new SendObject<T1>("Reservation",(T1)"Updated");
				}
			}
		} catch (Exception e) { // SQLException e
			throw new Exception("Error updating data to database", e);
		}
		return null;
	}

	/**
     * Handles creation of new objects in the database.
     * 
     * @param object <T> Input object type, must extend Serializable, Object to create
     * @param <T1> Return object type, must extend Serializable
     * @param con Database connector
     * @return A SendObject response describing the creation result
     * @throws Exception if creation fails
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Serializable, T1 extends Serializable> SendObject<T1> handleCreateAction(T object,
			DataBaseQuery con) throws Exception {
		try {
			if (object instanceof subscriber) {
				subscriber user = (subscriber) object;
				boolean checkEmail = false;
				checkEmail = con.checkUserEmailDuplicates(user);
				if (!checkEmail) {
					Object codeAndTag[] = new Object[2];
					codeAndTag[0] = (Integer) generateCode(con, "Subscriber");
					codeAndTag[1] = (String) generateTag(con);
					user.setCode((Integer) codeAndTag[0]);
					user.setTag((String) codeAndTag[1]);
					// Create User In the database using received object
					con.createUserInDatabase(user);
					SendEmail.sendMail(user.getEmail(), "Welcome to BPark", String.format(
							"Hello %s!\nWe're happy you decided to join BPark. Here are your Log in options:\nCode:%d\nTag:%s",
							user.getName(), (Integer) codeAndTag[0], (String) codeAndTag[1]));
					return new SendObject<T1>("Subscriber created", (T1) (Object[]) codeAndTag);
				} else {
					return new SendObject<T1>("Subscriber not created", (T1) (String) "Email exists");
				}
			}
			if (object instanceof Parkingsession) {
				Parkingsession session = (Parkingsession) object;
				// Create Parkingsession In the database received object
				con.createParkingsessionInDatabase(session);
				return new SendObject<T1>("Parkingsession", (T1) (String) "Created");
			}
			if (object instanceof Reservation) {
				Reservation reservation = (Reservation) object;
				// create Reservation in the database using received object
				ParkingSpot spot = null;
				if (con.getPrecentageAvailableSpaceFromDatabase() >= 40) {
					spot = con.getFreeParkingSpotFromDatabase(reservation.getDate(), reservation.getStartTime(),
							reservation.getEndTime()).get(0);
				} else {
					return new SendObject<T1>("Reservation",
							(T1) (String) "Not Created because there is less than 40% space available");
				}
				if (spot != null) {
					spot.setStatus(SpotStatus.RESERVED);
					con.updateParkingSpotInDatabase(spot);
					int reservationCode = generateCode(con,"Reservation");
					Reservation reservationToBeSent = new Reservation(spot.getSpotId(), reservation.getSubscriberId(),
							reservation.getDate(), reservation.getStartTime(), reservation.getEndTime(),reservationCode);
					int reservationId = con.createReservationInDatabase(reservationToBeSent);
					reservationToBeSent.setId(reservationId);
					con.updateReservationInDatabase(reservationId, reservationToBeSent);
					return new SendObject<T1>("Reservation",
							(T1) (String) String.format("Created with code:%d", reservationCode));
				} else {
					return new SendObject<T1>("Reservation", (T1) (String) "Not Created");
				}
			}
		} catch (Exception e) {// SQLException e
			throw new Exception("Error creating data in database", e);

		}
		return new SendObject<T1>("Error", (T1) (String) "creating data in database");
	}

	/**
	 * Generates a unique RFID tag string.
     * 
     * @param con Database connector
     * @return A unique RFID tag string
	 */
	private static String generateTag(DataBaseQuery con) {
		Random random = new Random();
		StringBuilder tag;
		boolean isDifferent = false;
		do {
			tag = new StringBuilder();
			for (int i = 0; i < 12; i++) {
				int byteValue = random.nextInt(256); // 0 to 255
				tag.append(String.format("%02X", byteValue));
			}
			isDifferent = con.checkRFIDTagDifferentFromAllSubscribers(tag.toString());
		} while (!isDifferent);
		return tag.toString();
	}

	/**
	 * Generates a unique subscriber code.
     * 
     * @param con Database connector
     * @return A unique integer code
	 */
	private static Integer generateCode(DataBaseQuery con, String type) {
		int code;
		Random random = new Random(); // Generate code
		boolean isDifferent = false;
		do {
			code = 100000 + random.nextInt(900000);
			if(type.equals("Subscriber"))
				isDifferent = con.checkCodeDifferentFromAllSubscribers(code);
			else
				isDifferent = con.checkCodeDifferentFromAllReservations(code);
		} while (!isDifferent);
		return code;
	}
}
