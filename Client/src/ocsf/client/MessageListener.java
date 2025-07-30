package ocsf.client;

@FunctionalInterface
public interface MessageListener {
    void onMessage(Object message);
}
