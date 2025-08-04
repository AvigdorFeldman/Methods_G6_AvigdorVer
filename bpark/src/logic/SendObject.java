package logic;
import java.io.Serializable;
/**
 * 
 * A generic container class used for encapsulating objects and associated messages 
 * for transmission between client and server in a JavaFX-based parking management system.
 * This class facilitates the serialization of data objects along with a message string,
 * enabling structured communication in a distributed environment.
 *
 * @param <T> the type of the object to be sent, which must implement {@link Serializable}
 */
@SuppressWarnings("serial")
public class SendObject<T extends Serializable> implements Serializable {
    private T obj;
    private String objectMessage;
    
    /**
     * @param objectMessage
     * @param obj
     * Constructor of the class
     */
    public SendObject(String objectMessage, T obj) {
        super();
        this.obj = obj;
        this.objectMessage = objectMessage;
    }  
    
    /**
     * Getter of obj field
     * @return obj
     */
    public T getObj() {
        return obj;
    }
    
    /**
     * Setter for obj field
     * @param obj
     */
    public void setObj(T obj) {
        this.obj = obj;
    }
    
    /**
     * Getter of objectMessage field
     * @return objectMessage
     */
    public String getObjectMessage() {
        return objectMessage;
    }
    
    /**
     * Setter for objectMessage field
     * @param objectMessage
     */
    public void setObjectMessage(String objectMessage) {
        this.objectMessage = objectMessage;
    }
}