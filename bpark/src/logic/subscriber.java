package logic;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a subscriber user in the system, extending the base user class.
 * A subscriber has a parking session history, a uniqe tag for identification, and a unique subscriber code for identification.
 */
public class subscriber  extends user implements Serializable{
	    private List<Parkingsession> history;
	    private String tag;
	    private int code; // Subscriber number - in the login
 
	    /**
	      * Constructs a new subscriber with the specified user details, parking history, tag, and code.
	      *
	      * @param id      the user's ID
	      * @param name    the user's full name
	      * @param phone   the user's phone number
	      * @param email   the user's email address
	      * @param role    the user's role (should be SUBSCRIBER)
	      * @param history the list of past parking sessions associated with the subscriber
	      * @param tag     the subscriber's unique
	      * @param code    the subscriber's unique login code
	     */
	    public subscriber(int id, String name, String phone, String email, Role role,
	                      List<Parkingsession> history,String tag, int code){
	    	super(id, name, phone, email, role);
	        this.history = history;
	        this.tag = tag;
	        this.code = code;
	    }

	    // Getters
	    
	    /**
	     * Returns the subscriber's parking session history.
	     *
	     * @return list of past parking sessions
	     */
	    public List<Parkingsession> getHistory() { return history; }
	    
	    /**
	      * Returns the subscriber's unique code used for login.
	      *
	      * @return the subscriber code
	     */
	    public int getCode() { return code; }
	    
	    /**
	      * Returns the subscriber's tag
	      *
	      * @return the tag string
	     */
	    public String getTag() {
			return tag;
		}

	    // Setters
	    
		/**
		  * Sets the subscriber's uniqe tag.
		  *
		  * @param tag the new tag
		 */
		public void setTag(String tag) {
			this.tag = tag;
		}
		
	    /**
	      * Sets the subscriber's parking session history.
	      *
	      * @param history the new list of parking sessions
	     */
	    public void setHistory(List<Parkingsession> history) { this.history = history; }
	    
	    /**
	      * Sets the subscriber's unique code used for login.
	      *
	      * @param code the new subscriber code
	     */
	    public void setCode(int code) { this.code = code; }
	
}