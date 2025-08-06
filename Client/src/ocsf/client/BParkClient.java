package ocsf.client;

import java.io.IOException;

import clientControllers.ShowAlert;
import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;
import logic.*;

/**
 * The class handles clients of BPark
 */
public class BParkClient extends ObservableClient {

	private MessageListener messageListener;

	/**
	 * @param host
	 * @param port 
	 * sets the host and port for the connection
	 */
	public BParkClient(String host, int port) {
		super(host, port); // This sets the host and port to be used in openConnection()
	}

	/**
	 * @param listener 
	 * Sets the listener for incoming messages from the server
	 */
	public void setMessageListener(MessageListener listener) {
		this.messageListener = listener;
	}

	/**
	 * @param msg
	 * Handles messages received from the server
	 */
	@Override
	protected void handleMessageFromServer(Object msg) {
		try {
			if(msg instanceof Exception) {
				Exception e = (Exception)msg;
				if(e.getMessage().equals("Server Shutdown"))
					Platform.runLater(() -> {
						ShowAlert.showAlert("Server Shutdown", "Close your app\nWait for administrator to inform you that the server is up again", AlertType.ERROR);
					});
			}
			else if (messageListener != null) {
				messageListener.onMessage(msg);
			}
		}catch(Exception e) {
			e.printStackTrace();
			
		}
	}

	/**
	 * @param msg 
	 * Safely sends a message to the server with error handling
	 */
	public void sendToServerSafely(Object msg) {
		try {
			sendToServer(msg);
		} catch (IOException e) {
			System.err.println("Failed to send message to server: " + e.getMessage());
		}
	}

	/**
	 * @param subscriber sub
	 * @throws Exception 
	 * start connection getting sub
	 */
	public void start(subscriber sub) throws Exception {
		try {
			openConnection();
			this.sendToServerSafely(new SendObject<subscriber>("connect",sub));
		} catch (IOException e) {
			System.err.println("Failed to open connection: " + e.getMessage());
			throw new Exception();
		}
	}

	/**
	 * Stops the client connection
	 */
	public void stop(subscriber sub) {
		try {
			if(sub!=null) {
				sub.setLoggedIn(false);
				this.sendToServerSafely(new SendObject<subscriber>("loggedoff",sub));
			}
			this.sendToServerSafely("Client disconnected");
			closeConnection();
		} catch (IOException e) {
			System.err.println("Failed to close connection: " + e.getMessage());

		}
	}


}
