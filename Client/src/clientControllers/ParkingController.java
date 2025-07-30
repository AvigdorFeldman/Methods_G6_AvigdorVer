package clientControllers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javafx.application.Platform;
import logic.*;
import ocsf.client.*;

/**
 * Controller class for managing parking logic for subscribers.
 * Handles parking sessions, spot assignment, reservations, code generation, and server communication.
 */
public class ParkingController {
	private BParkClient client;
	private subscriber sub;
	private PickUpScreenController pickUpScreen;
	private ParkingSpot spot;
	private boolean isAvailable = false;
	private volatile boolean isUsedCode = true;
	private volatile boolean responseReceived = false;
	private Parkingsession mySession;
	private DropOffScreenController dropOffScreen;
	private TimeExtensionScreenController TimeExtensionScreen;
	private String respond = null;
	private List<Parkingsession> sessions;
	private Parkingsession timeExtendSession;
	private Reservation reservation;

	/**
	 * Sets the client and subscriber for this controller.
     *
     * @param client the client to communicate with the server
     * @param sub    the subscriber using the parking service
	 */
	public void setClient(BParkClient client, subscriber sub) {
		this.client = client;
		this.sub = sub;
	}

	/**
	 * Handles the server response and updates internal state based on the action.
     *
     * @param message the response object received from the server
	 */
	public void handleServerResponse(Object message) {
		if (message instanceof SendObject<?>) {
			SendObject<?> msg = (SendObject<?>) message;
			String action = msg.getObjectMessage();
			Object obj = msg.getObj();
			if (action.equals("Availability")) {
				isAvailable = (Boolean) obj;
			} else if (action.equals("Parkingsession from code")) {
				mySession = (Parkingsession) obj;
			} else if (action.equals("isUsed")) {
				isUsedCode = (Boolean) obj;
			} else if (action.equals("new Spot")) {
				spot = (ParkingSpot) obj;
			} else if (action.equals("Time Extension")) {
				respond = (String) obj;
			} else if (action.equals("Active Sessions")&&obj instanceof List<?>) {
				sessions = (List<Parkingsession>) obj;
			} else if (action.equals("Session found")) {
				if (obj instanceof Parkingsession)
					timeExtendSession = (Parkingsession) obj;
			}else if (action.contains("reservation")) {
				reservation = (Reservation)obj;
			}
			responseReceived = true;
		}
	}
	

	/**
	 * Sets the controller for the Pick-Up screen.
     *
     * @param pickUpScreen the controller to set
	 */
	public void setPickUpScreen(PickUpScreenController pickUpScreen) {
		this.pickUpScreen = pickUpScreen;
	}

	/**
	 * Sets the controller for the Drop-Off screen.
     *
     * @param dropOffScreen the controller to set
	 */
	public void setDropOffScreen(DropOffScreenController dropOffScreen) {
		this.dropOffScreen = dropOffScreen;
	}

	/**
	 * Sets the controller for the Time Extension screen.
     *
     * @param TimeExtensionScreen the controller to set
	 */
	public void setTimeExtensionScreen(TimeExtensionScreenController TimeExtensionScreen) {
		this.TimeExtensionScreen = TimeExtensionScreen;
	}

	/**
	 * Assigns and returns a parking spot from the server.
     *
     * @return a free ParkingSpot
     * @throws Exception if response not received or no spot found
	 */
	public ParkingSpot assignParkingSpot() throws Exception {
		responseReceived = false;
		// Send to server
		client.sendToServerSafely(new SendObject<String>("Get", "Free spot")); 
		// wait until response is received
		Util.waitForServerResponse(20000,()->this.responseReceived);
		return spot;
	}

	/**
	 * Confirms a car drop-off by creating a new parking session.
     *
     * @throws Exception if no subscriber is found or server fails
	 */
	public void confirmDropOff() throws Exception {
		// if there's no available parking spot
		if (!checkParkingSpotAvailability()) {
			Platform.runLater(() -> {
				dropOffScreen.showNoAvailability(); // show no availability message
			});
		} else {
			Date inTime = new Date();
			Date outTime = new Date(inTime.getTime() + 4 * 60 * 60 * 1000); // 4 hour later
			ParkingSpot spot = assignParkingSpot(); // get free parking spot
			int parkingCode = generateParkingCode(); // generate parking code
			// subscriber1 != null for testing/checking
			if (sub != null) {
				Parkingsession session = new Parkingsession(0, sub.getId(), spot.getSpotId(), parkingCode, inTime,
						outTime, false, false, true);
				// send session to server
				client.sendToServerSafely(new SendObject<Parkingsession>("Create new", session));
				int spotId = spot.getSpotId();
				Platform.runLater(() -> {
					dropOffScreen.showParkingSuccess(); // show success message
					dropOffScreen.displayAssignedSpot(spotId);
					dropOffScreen.displayParkingCode(parkingCode);
				});
				// reset the controller for next use
				mySession = null;
				spot = null;
				isAvailable = false;
				isUsedCode = true;
			}
			else { // if subscriber is null, throw an exception
				throw new Exception("Subscriber is null, cannot create parking session.");
			}
		}
	}

	/**
	 * Checks for available parking spots from the server.
     *
     * @return true if a spot is available, false otherwise
     * @throws Exception if response is not received
	 */
	public boolean checkParkingSpotAvailability() throws Exception {
		// Reset flags
		isAvailable = false; // Assume no availability initially;
		responseReceived = false;

		client.sendToServerSafely(new SendObject<String>("Check", "Availability")); // get avaiability from server

		// wait until response is received
		Util.waitForServerResponse(20000,()->this.responseReceived);
		return isAvailable;
	}

	/**
	 * Handles the car pick-up process using a parking code.
     *
     * @param code the parking code entered by the user
     * @throws Exception if code is invalid or session not found
	 */
	public void requestCarPickUp(int code) throws Exception {
		try {
			responseReceived = false;
			sendParkingCode(code);
			// Poll until response is received
			Util.waitForServerResponse(20000,()->this.responseReceived);
			if (mySession != null) { // this should be retrieved from the database using the parking code
				if (mySession.getOutTime().before(new Date())) {
					markLateArrival(mySession); // if the session is late, mark it
				}
				mySession.setOutTime(new Date());
				mySession.setActive(false);
				sub.getHistory().add(mySession); // add session to subscriber's history
				releaseSpot(mySession.getSpotId()); // release the parking spot
				client.sendToServerSafely(new SendObject<Parkingsession>("Update Session", mySession)); // updates session
				// reset the controller for next use
				mySession = null;
				spot = null;
				isAvailable = false;
				isUsedCode = true;
			} else
				throw new Exception();
		} catch (Exception e) {
			// Log or handle exception
			e.printStackTrace();
			throw new Exception("Error processing parking code, the code could be wrong.");
		}
	}

	/**
	 * Marks a parking session as late and notifies the subscriber by Email/SMS.
     *
     * @param session the parking session to mark as late
     * @throws Exception if communication with the server fails
	 */
	public void markLateArrival(Parkingsession session) throws Exception {
		session.setLate(true);
		// send message to client about being late
		try {
			client.sendToServerSafely(new SendObject<subscriber>("Send late message by Email/SMS", sub));// V
		} catch (Exception e) {
			System.err.println("Error getting parking code from server: " + e.getMessage());
			e.printStackTrace();
			throw new Exception();
		}
		Platform.runLater(() -> {
			pickUpScreen.showLateArrivalMessage(); // show late arrival message
		});
	}

	/**
	 * Requests the server to send the lost parking code to the user via Email/SMS.
     *
     * @throws Exception if communication with the server fails
	 */
	public void handleLostCode() throws Exception {
		try {
			client.sendToServerSafely(new SendObject<subscriber>("Send Parking Code by Email/SMS", sub));// V
		} catch (Exception e) {
			System.err.println("Error getting parking code from server: " + e.getMessage());

			e.printStackTrace();
			throw new Exception();
		}
	}

	/**
	 * Sends the entered parking code to the server to retrieve the session.
     *
     * @param parkingCode the code entered by the user
     * @throws Exception if communication with the server fails
	 */
	public void sendParkingCode(int parkingCode) throws Exception {
		// Should return the Parkingsession from the DB
		try {
			client.sendToServerSafely(new SendObject<Integer>("Check received Parking Code", parkingCode));

		} catch (Exception e) {
			System.err.println("Error sending parking code to server: " + e.getMessage());
			e.printStackTrace();
			throw new Exception();
		}
	}

	/**
	 * Releases the parking spot by marking it as free in the database.
     *
     * @param spotId the ID of the spot to release
	 */
	public void releaseSpot(int spotId) {
		client.sendToServerSafely(new SendObject<Integer>("Update spot to Free", spotId));
	}

	public int generateParkingCode() throws Exception {
		int parkingCode;

		do {
			Random random = new Random(); // Generate code
			parkingCode = 100000 + random.nextInt(900000);

			// Reset flags
			isUsedCode = true;
			responseReceived = false;

			// Send to server
			client.sendToServerSafely(new SendObject<Integer>("Check new Parking Code", parkingCode));
			Util.waitForServerResponse(20000,()->this.responseReceived);
		} while (isUsedCode); // if used, repeat
		return parkingCode;
	}

	/**
	 * Retrieves all active parking sessions for the subscriber.
     *
     * @return list of active parking sessions
     * @throws Exception if server fails to respond
	 */
	public List<Parkingsession> getActiveParkingSessions() throws Exception {
		responseReceived = false;
		client.sendToServerSafely(new SendObject<Integer>("Get Active Parkingsessions", sub.getId()));
		Util.waitForServerResponse(20000,()->this.responseReceived);
		List<Parkingsession> activeSessions = new ArrayList<>();
		activeSessions.addAll(sessions);
		sessions = null;
		return activeSessions;
	}

	/**
	 * Sends an updated parking session to the server to extend the out time.
     *
     * @param session the session with the updated time
     * @return the server's response message
     * @throws Exception if server fails to respond
	 */
	public String ExtendTime(Parkingsession session) throws Exception {
		responseReceived = false;
		client.sendToServerSafely(new SendObject<Parkingsession>("Update time in session", session));
		Util.waitForServerResponse(20000,()->this.responseReceived);
		String TimeExtensionRespond = respond;
		respond = null;
		return TimeExtensionRespond;
	}

	/**
	 * Retrieves a specific parking session by ID for the current subscriber.
     *
     * @param parkingId the ID of the session to retrieve
     * @return the corresponding parking session
     * @throws Exception if session not found or subscriber mismatch
	 */
	public Parkingsession getSessionById(int parkingId) throws Exception {
		responseReceived = false;
		// gets Parkingsession from the server through parkingId
		client.sendToServerSafely(new SendObject<Integer>("Get Parkingsession", parkingId));
		Util.waitForServerResponse(20000,()->this.responseReceived);
		Parkingsession session = null;
		if (timeExtendSession != null) {
			if (timeExtendSession.getSubscriberId() == sub.getId()) {
				session = new Parkingsession(timeExtendSession.getSessionId(), timeExtendSession.getSubscriberId(),
						timeExtendSession.getSpotId(), timeExtendSession.getParkingCode(),
						timeExtendSession.getInTime(), timeExtendSession.getOutTime(), true, false, true);
				timeExtendSession = null;
			} else {
				timeExtendSession = null;
				TimeExtensionScreen.ShowFail("Session was not found");
			}
		} else
			TimeExtensionScreen.ShowFail("Session was not found");
		return session;
	}

	/**
	 * Creates a parking session based on an existing reservation.
     * Validates the time window and handles code generation.
     *
     * @param reservationCode the reservation code entered by the user
     * @throws Exception if reservation is invalid or time constraints not met
	 */
	public void implementDropoffUsingReservation(int reservationCode) throws Exception {
		Object reservationAndCode[] = new Object[2];
		reservation = getReservation(reservationCode); // gets reservation through the received code
		if (reservation.getEndTime() != null && reservation.getSubscriberId()==sub.getId()) {
			// Validates implementation time
			if (!isWithinAcceptableTime(reservation)) {
				if (LocalDateTime.now().isAfter(
			            reservation.getDate().atTime(
			                LocalTime.parse(reservation.getStartTime())
			            ).plusMinutes(15))
			        ) {
					reservation.setStartTime(null); // Indicates reservation hasn't been used	
					reservationAndCode[0] = reservationCode;
					reservationAndCode[1] = reservation;
					client.sendToServerSafely(new SendObject<Object[]>("Update",reservationAndCode)); 		
				}
				throw new Exception("subscriber didn't come in the right time, cannot create parking session through reservation. Try Submit Parking Request.");
			}
			LocalDateTime inDateTime = LocalDateTime.of(reservation.getDate(), LocalTime.parse(reservation.getStartTime()));
	        LocalDateTime outDateTime = LocalDateTime.of(reservation.getDate(), LocalTime.parse(reservation.getEndTime()));

	        // Convert LocalDateTime to Date
	        Date inTime = Date.from(inDateTime.atZone(ZoneId.systemDefault()).toInstant());
	        Date outTime = Date.from(outDateTime.atZone(ZoneId.systemDefault()).toInstant());
	        
			spot = new ParkingSpot(reservation.getSpot(), SpotStatus.OCCUPIED); // ParkingSpot from the reservation
			int parkingCode = generateParkingCode(); // generate parking code
			// sub != null for testing/checking
			if (sub != null) {
				Parkingsession session = new Parkingsession(0, sub.getId(), spot.getSpotId(), parkingCode, inTime,
						outTime, false, false, true);
				// send session to server
				client.sendToServerSafely(new SendObject<Parkingsession>("Create new", session));
				reservation.setEndTime(null); // Indicates reservation has been used
				reservationAndCode[0] = reservationCode;
				reservationAndCode[1] = reservation;
				client.sendToServerSafely(new SendObject<Object[]>("Update",reservationAndCode)); 
				int spotId = spot.getSpotId();
				Platform.runLater(() -> {
					dropOffScreen.showParkingSuccess(); // show success message
					dropOffScreen.displayAssignedSpot(spotId);
					dropOffScreen.displayParkingCode(parkingCode);
				});
				// reset the controller for next use
				reservation = null;
				mySession = null;
				spot = null;
				isAvailable = false;
				isUsedCode = true;
			}

			else { // if sub is null, throw an exception
				throw new Exception("Subscriber is null, cannot create parking session.");
			}
		} else {
			throw new Exception("Reservation is null, cannot create parking session.");
		}
	}

	/**
	 * Retrieves a reservation object from the server using the reservation code.
     *
     * @param code the reservation code
     * @return the corresponding Reservation object
     * @throws Exception if server fails to respond
	 */
	private Reservation getReservation(int code) throws Exception {
		responseReceived = false;
		client.sendToServerSafely(new SendObject<Integer>("Get reservation with code", code));
		Util.waitForServerResponse(20000,()->this.responseReceived);
		return reservation;
	}
	
	/**
	 * Checks if the user has arrived within an acceptable time window (early or ≤15 minutes late).
     *
     * @param reservation the reservation to validate
     * @return true if within acceptable time, false otherwise
	 */
	private boolean isWithinAcceptableTime(Reservation reservation) {
	    LocalDateTime now = LocalDateTime.now();
	    LocalDateTime reservationDateTime = reservation.getDate()
	            .atTime(LocalTime.parse(reservation.getStartTime()));
	    // Check if same day
	    if (!now.toLocalDate().equals(reservation.getDate())) {
	        return false; // Entirely different day - reject
	    }
	    // For same day:
	    if (now.isBefore(reservationDateTime)) {
	        return true; // Early arrival - allow but don't nullify
	    } else {
	        long minutesLate = Duration.between(reservationDateTime, now).toMinutes();
	        return minutesLate <= 15;
	    }
	}
}