package clientControllers;

import java.util.List;

import ocsf.client.*;
import logic.*;

/**
 * Controller responsible for handling the retrieval and management of parking session
 * history data for a specific subscriber in the client-side of the parking management system.
 * This class communicates with the server to request and receive parking history data,
 * stores the retrieved sessions, and provides access to them upon request.
 */
public class HistoryController {
    private List<Parkingsession> sessions;
    private BParkClient client;
    private subscriber subscriber;
    private boolean responseReceived;

    /**
     * Initializes the client and subscriber associated with this controller.
     *
     * @param client The client instance used for server communication.
     * @param subscriber The current subscriber whose parking history is being handled.
     */
    public void setClient(BParkClient client, subscriber subscriber) {
        this.client = client;
        this.subscriber = subscriber;
    }

    /**
     * Sets the list of parking sessions internally.
     *
     * @param sessions The list of parking sessions to store.
     */
    private void setSessions(List<Parkingsession> sessions) {
        this.sessions = sessions;
    }

    /**
     * Processes messages received from the server.
     * If the message contains a list of parking sessions, it updates the local list accordingly.
     *
     * @param message The object received from the server.
     */
    public void handleServerMessage(Object message) {
        if (message instanceof SendObject<?>) {
            SendObject<?> sendObject = (SendObject<?>) message;

            if (sendObject.getObjectMessage().equals("Parkingsession list of subscriber")
                    && sendObject.getObj() instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<Parkingsession> list = (List<Parkingsession>) sendObject.getObj();
                setSessions(list);
            }
            responseReceived = true;
        }
    }

    /**
     * Sends a request to the server to retrieve the subscriber's parking history,
     * and waits for the server response.
     *
     * @return A list of {@link Parkingsession} objects for the current subscriber.
     * @throws Exception If the server does not respond within the timeout period.
     */
    public List<Parkingsession> getSessions() throws Exception {
        responseReceived = false;
        client.sendToServerSafely(new SendObject<Integer>("Get history", subscriber.getId()));
        Util.waitForServerResponse(15000, () -> this.responseReceived);
        return sessions;
    }
}
