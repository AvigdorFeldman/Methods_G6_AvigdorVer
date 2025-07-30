package clientControllers;

import java.io.IOException;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import logic.*;
import ocsf.client.*;

/**
 * Controller class for handling the login screen functionality in the BPark client application.
 * This class manages user authentication input, connects to the server,
 * and navigates the user to the appropriate interface (e.g., Subscriber, Attendant, Admin, or Guest)
 * based on their role or login method.
 * Notes:
 * - Supports two login methods: "Name + Subscriber Code" and "Tag".
 * - Tag-based login is only available when the console checkbox is selected.
 */
public class LoginController {

    // === FXML injected fields ===

    /** Port used for server communication. */
    final int port = 5555;

    @FXML private TextField ipField;
    @FXML private ComboBox<String> loginMethodComboBox;
    @FXML private TextField nameField;
    @FXML private TextField subscriberIdField;
    @FXML private TextField tagField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button exitButton;
    @FXML private Button connectButton;
    @FXML private CheckBox consoleCheckBox;

    /** Client instance responsible for communicating with the server. */
    private BParkClient client;
    private Controller controller;

    /**
     * Initializes the controller after the FXML file has been loaded.
     * Sets up initial UI state and listeners for login method changes.
     */
    @FXML
    public void initialize() {
        loginMethodComboBox.setItems(FXCollections.observableArrayList("Name + Subscriber Code"));
        loginMethodComboBox.getSelectionModel().selectFirst();
        loginMethodComboBox.setOnAction(event -> updateFieldVisibility());
        consoleCheckBox.setOnAction(event -> updateLoginOptions());
    }

    /**
     * Updates the login method options in the combo box based on whether the console checkbox is selected.
     */
    private void updateLoginOptions() {
        boolean isConsoleChecked = consoleCheckBox.isSelected();

        if (isConsoleChecked) {
            if (!loginMethodComboBox.getItems().contains("Tag")) {
                loginMethodComboBox.getItems().add("Tag");
            }
        } else {
            if (loginMethodComboBox.getItems().contains("Tag")) {
                loginMethodComboBox.getItems().remove("Tag");
                if ("Tag".equals(loginMethodComboBox.getValue())) {
                    loginMethodComboBox.setValue("Name + Subscriber Code");
                }
            }
        }

        updateFieldVisibility();
    }

    /**
     * Adjusts the visibility of input fields depending on the selected login method.
     */
    private void updateFieldVisibility() {
        String method = loginMethodComboBox.getValue();
        boolean isTag = "Tag".equals(method);
        boolean isConsoleChecked = consoleCheckBox.isSelected();

        nameField.setVisible(!isTag);
        nameField.setManaged(!isTag);

        subscriberIdField.setVisible(!isTag);
        subscriberIdField.setManaged(!isTag);

        tagField.setVisible(isTag && isConsoleChecked);
        tagField.setManaged(isTag && isConsoleChecked);
    }

    /**
     * Handles the login process when the user clicks the connect button.
     * Validates input and initiates the server connection.
     */
    @FXML
    private void handleLogin() {
        String ipAddress = ipField.getText().trim();
        if (ipAddress.isEmpty()) {
            ShowAlert.showAlert("Error", "Please enter both IP and port", Alert.AlertType.ERROR);
            return;
        }
        subscriber sub1 = createsubfromlogin();
        connectToServer(ipAddress, port, sub1);
    }

    /**
     * Creates a {@link subscriber} object based on the login fields.
     *
     * @return a subscriber object if Name + Subscriber Code method is used, or null if using Tag-based login.
     */
    public subscriber createsubfromlogin() {
        String method = loginMethodComboBox.getValue();
        boolean isTag = "Tag".equals(method);
        boolean isConsoleChecked = consoleCheckBox.isSelected();

        try {
            if (isTag && isConsoleChecked) {
            	String tag = tagField.getText().trim();
                return new subscriber(0, null, null, null, null, null, tag, 0);
                
            } else {
                int passwordInt = Integer.parseInt(subscriberIdField.getText().trim());
                return new subscriber(0, nameField.getText().trim(), null, null, null,
                        null, null, passwordInt);
            }
        } catch (Exception e) {
            ShowAlert.showAlert("Error", "The type of the fields is wrong", Alert.AlertType.ERROR);
            return null;
        }
    }

    /**
     * Establishes a connection to the server and starts the client session.
     *
     * @param ipAddress the IP address of the server
     * @param port the port number for the server
     * @param sub1 the subscriber object for authentication (can be null if Tag is used)
     */
    public void connectToServer(String ipAddress, int port, subscriber sub1) {
        try {
            client = new BParkClient(ipAddress, port);
            exitButton.setDisable(false);
            client.start(sub1);
            client.setMessageListener(this::handleServerMessage);
        } catch (Exception e) {
            ShowAlert.showAlert("Error", "Failed to connect to the server at " + ipAddress + ":" + port, Alert.AlertType.ERROR);
            connectButton.setDisable(false);
        }
    }

    /**
     * Handles incoming messages from the server and navigates the user to the appropriate screen.
     *
     * @param msg the message object received from the server
     */
    private void handleServerMessage(Object msg) {
        Platform.runLater(() -> {
            if (msg instanceof SendObject send) {
                if (send.getObj() instanceof subscriber sub) {
                    System.out.println("[Server] " + msg);
                    switch (sub.getRole()) {
                        case SUBSCRIBER -> connectclient(sub, "MainMenuScreen.fxml", "Main Menu", -1);
                        case ATTENDANT -> connectclient(sub, "AttendantScreen.fxml", "Attendant Menu", -1);
                        case MANAGER -> connectclient(sub, "AdminScreen.fxml", "Admin Menu", -1);
                    }
                } else if (send.getObj() instanceof Double percent) {
                    System.out.println(percent);
                    connectclient(null, "GuestScreenUI.fxml", "Guest Screen", percent);
                } else {
                    client.stop();
                    ShowAlert.showAlert("Error", "Cant login to server wrong account", Alert.AlertType.ERROR);
                }
            }
        });
    }

    /**
     * Loads the appropriate FXML screen and passes context (subscriber/client) to the controller.
     *
     * @param sub the subscriber object, or null for guest access
     * @param fxml the path to the FXML file to load
     * @param title the window title
     * @param percent the guest discount (only used for GuestScreenController)
     */
    private void connectclient(subscriber sub, String fxml, String title, double percent) {
        controller = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientUI/"+fxml));
        Parent tableRoot = null;

        try {
            tableRoot = loader.load();
            controller = loader.getController();
            controller.setClient(client, sub);
            
        } catch (IOException e) {
            System.out.println("cant load screen: " + fxml);
            e.printStackTrace();
        }

        if (percent != -1 && controller instanceof GuestScreenController guestController) {
            guestController.set_percent(percent);
        }

        if ("MainMenuScreen.fxml".equals(fxml) && controller instanceof MainMenuController mainMenuController) {
            boolean useConsole = consoleCheckBox.isSelected();
            mainMenuController.setIsConsole(useConsole);
            mainMenuController.hidebuttons(useConsole);
        }

        Stage stage = (Stage) connectButton.getScene().getWindow();
        Scene scene = new Scene(tableRoot);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.setTitle(title);
        stage.setMaximized(true);

        stage.setOnCloseRequest(new EventHandler<>() {
            @Override
            public void handle(WindowEvent event) {
                System.out.println("Closing application...");
                client.stop();
                System.gc();
                System.exit(0);
            }
        });

        controller.setBackHandler(() -> {
            try {
                client.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("login.fxml"));
                Parent loginRoot = loginLoader.load();
                stage.setScene(new Scene(loginRoot));
                stage.setTitle("Connect to Server");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        client.setMessageListener(msg -> controller.handleServerMessage(msg));
    }

    /**
     * Handles the action when the exit button is clicked.
     * Terminates the JavaFX application gracefully.
     */
    @FXML
    public void handleExit() {
        System.gc();
        Platform.exit();
    }

    /**
     * Opens the guest screen by connecting to the server without authentication.
     * Triggered when a guest user clicks the connect button.
     */
    @FXML
    private void openGuestScreen() {
        String ipAddress = ipField.getText().trim();
        if (ipAddress.isEmpty()) {
            ShowAlert.showAlert("Error", "Please enter IP", Alert.AlertType.ERROR);
            return;
        }
        connectToServer(ipAddress, port, null);
    }
}
