package logic;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Represents a reservation for a parking spot made by a subscriber.
 * This class holds information such as subscriber ID, parking spot ID,
 * reservation date, and start/end times.
 */
@SuppressWarnings("serial")
public class Reservation implements Serializable{
	private int id;
	private final int subscriberId;
	private final int spotId;
	private final LocalDate date;
	private String startTime;
	private String endTime;
	private int code;
 
	/**
	 * Constructs a new Reservation object with the specified details.
     *
     * @param id            the ID of the reservation
     * @param spotId        the ID of the reserved parking spot
     * @param subscriber_id the ID of the subscriber making the reservation
     * @param date          the date of the reservation
     * @param startTime     the start time of the reservation (format: "HH:mm")
     * @param endTime       the end time of the reservation (format: "HH:mm")
     * @param code          the reservation code
	 */
	public Reservation(int subscriber_id, int spotId, LocalDate date, String startTime, String endTime, int code) {
		this.subscriberId = subscriber_id;
		this.spotId = spotId;
		this.date = date;
		this.startTime = startTime;
		this.endTime = endTime;
		this.code = code;
	}

	
	/**
	 * Returns the ID of the reservation
	 * 
	 * @return the reservation ID
	 */
	public int getId() {
		return id;
	}

	
	/**
	 * Returns the code of the reservation
	 * 
	 * @return the reservation code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Returns the ID of the subscriber who made the reservation.
     *
     * @return the subscriber ID
	 */
	public int getSubscriberId() {
		return subscriberId;
	}

	/**
	 * Returns the ID of the reserved parking spot.
     *
     * @return the spot ID
	 */
	public int getSpot() {
		return spotId;
	}

	/**
	 * Returns the date of the reservation.
     *
     * @return the reservation date
	 */
	public LocalDate getDate() {
		return date;
	}

	/**
	 * Returns the start time of the reservation.
     *
     * @return the start time as a string
	 */
	public String getStartTime() {
		return startTime;
	}

	/**
	 * Returns the end time of the reservation.
     *
     * @return the end time as a string
	 */
	public String getEndTime() {
		return endTime;
	}
	
	/**
	 * Sets the reservation id
	 * 
	 * @param Id
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Sets the reservation code
	 * 
	 * @param code
	 */
	public void setCode(int code) {
		this.code = code;
	}
	
	/**
	 * Sets the start time of the reservation.
     *
     * @param startTime the new start time (format: "HH:mm")
	 */
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	
	/**
	 * Sets the end time of the reservation.
     *
     * @param endTime the new end time (format: "HH:mm")
	 */
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
}
