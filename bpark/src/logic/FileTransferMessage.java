package logic;

import java.io.Serializable;

/**
 * {@code FileTransferMessage} is a class that represents a message containing a file for transfer.
 * This class encapsulates the filename and the file data as a byte array, making it suitable for 
 * serialization and network communication.
 * 
 * The class implements {@link Serializable} to allow instances of it to be serialized and transmitted 
 * over a network or written to disk.
 */
@SuppressWarnings("serial")
public class FileTransferMessage implements Serializable {
    private String filename;
    private byte[] data;

    /**
     * Constructs a new {@code FileTransferMessage} with the specified filename and file data.
     * 
     * @param filename The name of the file being transferred.
     * @param data The byte array containing the file data.
     */
    public FileTransferMessage(String filename, byte[] data) {
        this.filename = filename;
        this.data = data;
    }

    /**
     * Returns the filename of the file being transferred.
     *
     * @return The filename of the file.
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Returns the byte array representing the file data.
     *
     * @return The byte array containing the file data.
     */
    public byte[] getData() {
        return data;
    }

	/**
     * Sets the filename of the file being transferred.
     *
     * @param filename The new filename of the file.
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
     * Sets the byte array representing the file data.
     *
     * @param data The new byte array containing the file data.
	 */
	public void setData(byte[] data) {
		this.data = data;
	}
    
}
