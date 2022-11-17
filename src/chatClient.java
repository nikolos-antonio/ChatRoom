import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class chatClient extends Application {
    private DataOutputStream toServer;
    private DataInputStream fromServer;
    private String hostname;
    private Socket client;
    private boolean done;

    public chatClient(String hostname) {
        this.hostname = hostname;
    }

    public chatClient() {

    }

    @Override
    public void start(Stage primaryStage) {
        // Panel p to hold the label and text field
        BorderPane paneForTextField = new BorderPane();
        paneForTextField.setPadding(new Insets(5, 5, 5, 5));
        paneForTextField.setStyle("-fx-border-color: green");

        TextField tf = new TextField();
        tf.setAlignment(Pos.BOTTOM_RIGHT);
        paneForTextField.setCenter(tf);

        Button btn = new Button();
        btn.setText("Send");
        paneForTextField.setRight(btn);

        BorderPane mainPane = new BorderPane();
        mainPane.setStyle("-fx-background-color: lightgreen;");
        // Text area to display contents
        TextArea ta = new TextArea();
        mainPane.setCenter(new ScrollPane(ta));
        mainPane.setBottom(paneForTextField);

        btn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                try {
                    String message = tf.getText();
                    if (message.equals("/quit")) {
                        shutdown();
                    } else {
                        toServer.writeUTF(message);
                        toServer.flush();
                    }
                    tf.clear();

                } catch (IOException ex) {
                    System.err.println(ex);
                }
            }
        });

        // Create a scene and place it in the stage
        Scene scene = new Scene(mainPane, 450, 200);
        primaryStage.setTitle("Client"); // Set the stage title
        primaryStage.setScene(scene); // Place the scene in the stage
        primaryStage.show(); // Display the stage

        new Thread(() -> {
            try {
                // Create a socket to connect to the server
                Socket socket = new Socket(hostname, 8000);
                fromServer = new DataInputStream(socket.getInputStream());
                toServer = new DataOutputStream(socket.getOutputStream());

                InetAddress inetAddress = socket.getInetAddress();
                Platform.runLater(() -> {
                            System.out.println("Client's host name is " + inetAddress.getHostName());
                            System.out.println("Client's IP Address is " + inetAddress.getHostAddress());
                });

                String inMessage;
                while ((inMessage = fromServer.readUTF().trim()) != null) {
                    ta.appendText(inMessage + "\n");
                }
            } catch (IOException ex) {
                ta.appendText(ex.toString() + '\n');
                shutdown();
            }
        }).start();
    }


    public void shutdown() {
        done = true;
        try {
            fromServer.close();
            toServer.close();
            if (!client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {

        }
    }

    public static void main(String[] args) {
        launch(args);
    }


}