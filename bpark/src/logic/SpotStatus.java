package logic;

/**
 * Represents the status of a parking spot.
 * 
 * FREE     - The parking spot is currently available for use.
 * OCCUPIED - The parking spot is currently taken by a vehicle.
 * RESERVED - The parking spot is reserved and cannot be used by others in the same time as the reservation.
 */
public enum SpotStatus {
    FREE,
    OCCUPIED,
    RESERVED
} 