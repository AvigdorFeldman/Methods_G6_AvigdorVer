package logic;

import java.io.Serializable;

public class FileTransferMessage implements Serializable {
    private String filename;
    private byte[] data;

    public FileTransferMessage(String filename, byte[] data) {
        this.filename = filename;
        this.data = data;
    }

    public String getFilename() {
        return filename;
    }

    public byte[] getData() {
        return data;
    }

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
    
}
