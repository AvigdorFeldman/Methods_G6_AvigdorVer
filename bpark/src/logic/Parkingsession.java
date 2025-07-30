package logic;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents a parking session within the client-server architecture.
 * 
 * This class is used to transfer session data between the client and the server.
 * It includes details about the subscriber, parking spot, entry and exit times,
 * and status flags such as whether the session is active, late, or extended.
 * 
 * Implements {@link Serializable} to allow the session object to be transmitted
 * over the network between client and server.
 */
public class Parkingsession implements Serializable{
	    private int sessionId;
	    private int subscriberId;
	    private int spotId;
	    private int parkingCode; // This could be used for a unique session identifier or parking code
	    private Date inTime;
	    private Date outTime;
	    private boolean extended;
	    private boolean late;
	    private boolean active;

	    /**
	     * Constructs a new {@code Parkingsession} object with the given parameters.
	     *
	     * @param sessionId    the unique ID of the parking session
	     * @param subscriberId the ID of the subscriber using the session
	     * @param spotId       the ID of the parking spot assigned
	     * @param parkingCode  the code used to identify or verify the session
	     * @param inTime       the time when the session started
	     * @param outTime      the time when the session is expected to end
	     * @param extended     whether the session has been extended
	     * @param late         whether the car was picked up late
	     * @param active       whether the session is currently active
	     */
	    public Parkingsession(int sessionId, int subscriberId, int spotId, int parkingCode, Date inTime, Date outTime, boolean extended, boolean late, boolean active) {
	        this.sessionId = sessionId;
	        this.subscriberId = subscriberId;
	        this.spotId = spotId;
	        this.parkingCode = parkingCode;
	        this.inTime = inTime;
	        this.outTime = outTime;
	        this.extended = extended;
	        this.late = late;
	        this.active = active;
	    }

	    /**
	     * Returns whether the session is currently active.
	     *
	     * @return {@code true} if the session is active, {@code false} otherwise
	     */
	    public boolean getActive() {
			return active;
		}

	    /**
	     * Returns the session ID.
	     *
	     * @return the session ID
	     */
	    public int getSessionId() {
	        return sessionId;
	    }

	    /**
	     * Returns the unique parking code for this session.
	     *
	     * @return the parking code
	     */
	    public int getParkingCode() {
			return parkingCode;
		}

	
		/**
		 * Returns the subscriber ID.
		 *
     	 * @return the subscriber ID
		 */
		public int getSubscriberId() {
	        return subscriberId;
	    }

	    /**
	     * Returns the ID of the parking spot.
	     *
	     * @return the parking spot ID
	     */
	    public int getSpotId() {
	        return spotId;
	    }

	    /**
	     * Returns the start time of the parking session.
	     *
	     * @return the check-in time
	     */
	    public Date getInTime() {
	        return inTime;
	    }

	    /**
	     * Returns the end time of the parking session.
	     *
	     * @return the check-out time
	     */
	    public Date getOutTime() {
	        return outTime;
	    }

	    /**
	     * Returns whether the session was extended.
	     *
	     * @return {@code true} if extended, {@code false} otherwise
	     */
	    public boolean isExtended() {
	        return extended;
	    }

	    /**
	     * Returns whether the session ended late.
	     *
	     * @return {@code true} if the session was late, {@code false} otherwise
	     */
	    public boolean isLate() {
	        return late;
	    }

	    // Setters
	    /**
		  * Sets the active status of the session.
		  *
		  * @param active {@code true} to mark session as active, {@code false} otherwise
		 */
		public void setActive(boolean active) {
			this.active = active;
		}
	    
	    /**
	     * Sets the session ID.
	     *
	     * @param sessionId the unique ID of the session
	     */
	    public void setSessionId(int sessionId) {
	        this.sessionId = sessionId;
	    }

	    /**
	     * Sets the subscriber ID associated with this session.
	     *
	     * @param subscriberId the ID of the subscriber
	     */
	    public void setSubscriberId(int subscriberId) {
	        this.subscriberId = subscriberId;
	    }

	    /**
	     * Sets the parking spot ID used during the session.
	     *
	     * @param spotId the ID of the parking spot
	     */
	    public void setSpotId(int spotId) {
	        this.spotId = spotId;
	    }

	    /**
	     * Sets the unique parking code associated with the session.
	     *
	     * @param parkingCode the parking code
	     */
	    public void setParkingCode(int parkingCode) {
			this.parkingCode = parkingCode;
		}

	    /**
	     * Sets the time the vehicle entered the parking spot.
	     *
	     * @param inTime the entry time
	     */
	    public void setInTime(Date inTime) {
	        this.inTime = inTime;
	    }

	    /**
	      * Sets the time the vehicle left the parking spot.
	      *
	      * @param outTime the exit time
	     */
	    public void setOutTime(Date outTime) {
	        this.outTime = outTime;
	    }

	    /**
	     * Sets whether the session was extended.
	     *
	     * @param extended true if the session was extended; false otherwise
	     */
	    public void setExtended(boolean extended) {
	        this.extended = extended;
	    }

	    /**
	      * Sets whether the session ended late.
	      *
	      * @param late true if the session was late; false otherwise
	     */
	    public void setLate(boolean late) {
	        this.late = late;
	    }
}