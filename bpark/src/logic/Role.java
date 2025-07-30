package logic;

/**
 * Represents the different roles a user can have in the parking system.
 * 
 * SUBSCRIBER - A regular user who can reserve and put/get their vehicle.
 * ATTENDANT  - A parking lot staff member who manages daily operations such as registertion of subscribers.
 * MANAGER    - An administrative user with higher-level permissions, views graphic reports.
 */
public enum Role {
    SUBSCRIBER,
    ATTENDANT,
    MANAGER
} 