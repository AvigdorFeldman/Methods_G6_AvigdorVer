package logic;

import java.io.Serializable;

/**
 * Represents a parking spot in a parking lot system.
 * Each parking spot has a unique ID and a status indicating whether it is free, occupied, or reserved.
 * Implements Serializable to allow object transfer between client and server.
 */
@SuppressWarnings("serial")
public class ParkingSpot implements Serializable{
    private int spotId;
    private SpotStatus status;
    
    /**
     * Constructs a ParkingSpot with the specified ID and status.
     *
     * @param spotId the unique identifier of the parking spot
     * @param status the current status of the parking spot (FREE, OCCUPIED, RESERVED)
     */
    public ParkingSpot(int spotId, SpotStatus status) {
        this.spotId = spotId;
        this.status = status;
    }

    // Getters
    /**
     * Returns the ID of the parking spot.
     *
     * @return the spot ID
     */
    public int getSpotId() {
        return spotId;
    }

    /**
     * Returns the current status of the parking spot.
     *
     * @return the spot status
     */
    public SpotStatus getStatus() {
        return status;
    }
    
    // Setters
    /**
     * Sets the ID of the parking spot.
     *
     * @param spotId the new spot ID
     */
    public void setSpotId(int spotId) {
        this.spotId = spotId;
    }

    /**
     * Sets the status of the parking spot.
     *
     * @param status the new spot status
     */
    public void setStatus(SpotStatus status) {
        this.status = status;
    }

}