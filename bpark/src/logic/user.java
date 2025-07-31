package logic;

import java.io.Serializable;

/**
 * Represents a user in the system with basic profile and role information.
 */
public class user implements Serializable{
	private int id;
    private String name;
    private String phone;
    private String email;
    private Role role;
    private boolean loggedIn = false;
 
    /**
     * Constructs a new user with the specified information.
     *
     * @param id    the unique ID of the user
     * @param name  the full name of the user
     * @param phone the user's phone number
     * @param email the user's email address
     * @param role  the role assigned to the user (SUBSCRIBER, ATTENDANT, MANAGER)
     */
    public user(int id, String name, String phone, String email, Role role) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.role = role;
        this.loggedIn = loggedIn;
    }

    // Getters
    
    /**
     * Returns the user's unique ID.
     *
     * @return the user ID
     */
    public int getId() { return id; }
    
    /**
     * Returns the user's name.
     *
     * @return the full name of the user
     */
    public String getName() { return name; }
    
    /**
     * Returns the user's phone number.
     *
     * @return the phone number
     */
    public String getPhone() { return phone; }
    
    /**
     * Returns the user's email address.
     *
     * @return the email address
     */
    public String getEmail() { return email; }
    
    /**
     * Returns the user's role.
     *
     * @return the role of the user
     */
    public Role getRole() { return role; }

    /**
     * @return true if the subscriber is logged in, false otherwise
     */
    public boolean getLoggedIn() { return loggedIn; }
    
    // Setters
    
    /**
     * Sets the user's ID.
     *
     * @param id the new user ID
     */
    public void setId(int id) { this.id = id; }
    
    /**
     * Sets the user's name.
     *
     * @param name the new full name
     */
    public void setName(String name) { this.name = name; }
    
    /**
     * Sets the user's phone number.
     *
     * @param phone the new phone number
     */
    public void setPhone(String phone) { this.phone = phone; }
    
    /**
     * Sets the user's email address.
     *
     * @param email the new email address
     */
    public void setEmail(String email) { this.email = email; }
    
    /**
     * Sets the user's role.
     *
     * @param role the new role
     */
    public void setRole(Role role) { this.role = role; }
    
    /**
     * Sets the user's login.
     *
     * @param change the value of loggedIn
     */
    public void setLoggedIn(boolean loggedIn) { this.loggedIn = loggedIn; }
}
