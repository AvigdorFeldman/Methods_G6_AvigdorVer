package ocsf.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import serverControllers.*;
import jdbc.*;
import logic.*;

/**
 * The class implements the Server side
 */
public class BparkServer extends AbstractServer {

	final public static int DEFAULT_PORT = 5555;
	private DataBaseQuery con; // Will be used any time an SQL Query is needed
	private List<ConnectionToClient> clientConnections = new ArrayList<>(); // Current connections
	private List<List<String>> requiredList = new ArrayList<>(); // Log of current and former connections
	private ServerController serverController;

	/**
	 * Constructor for the class
	 * @param port
	 * @param controller 
	 */
	public BparkServer(ServerController controller) {
		super(DEFAULT_PORT);
		this.serverController = controller;
		con = new DataBaseQuery();
	}

	/**
	 * Handles objects that are sent to the server
	 * @param msg
	 * @param client
	 */
	public void handleMessageFromClient(Object msg, ConnectionToClient client) {

		if (msg instanceof String) {
			String msgString = (String) msg;
			System.out.println(msgString);
			if (msgString.equals("Client disconnected"))
				clientDisconnected(client);
		}
		if (msg instanceof SendObject<?>) {
			SendObject<?> obj = (SendObject<?>) msg;
			try {
				// Call the handler, which could return any type
				Object result = SendObjectHandler.sendObjectHandle(obj, con);
				// If the result is a SendObject, you can send it directly
				if (result instanceof SendObject<?>) {
					SendObject<?> sendObjectResult = (SendObject<?>) result;
					if(sendObjectResult.getObj() instanceof String) {
						String str = (String)sendObjectResult.getObj();
						Object resultToAll;
						if(str.equals("Updated")||str.equals("updated successfully")||str.contains("Created with code")||str.equals("Created")||sendObjectResult.getObjectMessage().equals("Subscriber created")) {						
							resultToAll = SendObjectHandler.sendObjectHandle(new SendObject<String>("Get","all reservations"), con);
							sendToAllClients(resultToAll);
							resultToAll = SendObjectHandler.sendObjectHandle(new SendObject<String>("Get","all subscribers"), con);
							sendToAllClients(resultToAll);
							resultToAll = SendObjectHandler.sendObjectHandle(new SendObject<String>("Get","all parking sessions"), con);
							sendToAllClients(resultToAll);
							resultToAll = SendObjectHandler.sendObjectHandle(new SendObject<String>("Get","all parking spots"), con);
							sendToAllClients(resultToAll);
						}				
					}			
					sendToSingleClient(sendObjectResult, client);
				}
			} catch (Exception e) {
				System.out.println("sendObjectHandle error");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Sends a object msg to a client
	 * @param msg
	 * @param client 
	 * @return Object
	 */
	public void sendToSingleClient(Object msg, ConnectionToClient client) {
		try {
			if (msg instanceof String) // Sends a String
				client.sendToClient(msg);
			else if (msg instanceof SendObject<?>) {
				SendObject<?> sendObject = (SendObject<?>) msg;
				
				client.sendToClient(sendObject);
			} else if (msg instanceof ArrayList<?>) {
				ArrayList<?> list = (ArrayList<?>) msg;
				client.sendToClient(list);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
	}

	/**
	 * Prints to console that the server started
	 */
	protected void serverStarted() {
		System.out.println(("Server listening for connections on port " + getPort()));
	}

	protected void serverStopped() {
		// Honestly never used it...
		System.out.println("Server has stopped listening for connections.");
	}

	/**
	 * @param client 
	 * Add the client to the list of connected clients Each client
	 * has: id, IP, hostName, status{"Connected","Disconnected"} (all strings)
	 */
	@Override
	public void clientConnected(ConnectionToClient client) {
		if (!clientConnections.contains(client)) {
			clientConnections.add(client);
			List<String> clientInfo = new ArrayList<>();
			clientInfo.add(Long.toString(client.getId()));
			clientInfo.add(client.getInetAddress().getHostAddress());
			clientInfo.add(client.getInetAddress().getCanonicalHostName());
			clientInfo.add("Connected");
			requiredList.add(clientInfo);
			serverController.recievedServerUpdate(requiredList);
			System.out.println(String.format("Client:%s IP:%s HostName:%s %s", clientInfo.get(0), clientInfo.get(1),
					clientInfo.get(2), clientInfo.get(3)));

		} else { // In case the same client tries to reconnect, may never be used
			try {
				clientSetStatus(client, "Connected");
			} catch (Exception e) {
				System.out.println("Connect failed!");
				e.printStackTrace();
			}
		}

	}

	/**
	 * This method is called whenever a client disconnects Remove the
	 * client from the list when they disconnect
	 * @param client 
	 */
	@Override
	public void clientDisconnected(ConnectionToClient client) {
		try {
			clientSetStatus(client, "Disconnected");

		} catch (Exception e) {
			System.out.println("Disconnect failed!");
			e.printStackTrace();
		}
	}

	/**
	 * Updates the status of a client to either "Connected"/"Disconnected"
	 * @param client
	 * @param status
	 * @throws Exception
	 */
	private void clientSetStatus(ConnectionToClient client, String status) throws Exception {
		for (List<String> string : requiredList) {
			if (string.get(0).equals(Long.toString(client.getId()))) {
				string.set(3, status);
				requiredList.set(requiredList.indexOf(string), string);
				if (status.equals("Disconnected"))
					clientConnections.remove(client);
				else if (status.equals("Connected"))
					clientConnections.add(client);
				else {
					throw new Exception();
				}
				serverController.recievedServerUpdate(requiredList);
				// Log the disconnection
				System.out.println(String.format("Client:%s IP:%s HostName:%s %s", string.get(0), string.get(1),
						string.get(2), string.get(3)));
				break;
			}
		}
	}

}
