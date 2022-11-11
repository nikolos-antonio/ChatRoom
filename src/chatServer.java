import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class chatServer extends Application {
    TextArea taLog = new TextArea();

    @Override
    public void start(Stage primaryStage){


        Scene scene = new Scene(new ScrollPane(taLog), 450, 200);
        primaryStage.setTitle("Chat server"); // Set the stage title
        primaryStage.setScene(scene); // Place the scene in the stage
        primaryStage.show(); // Display the stage

        new Thread( () -> {
            try {
                // Create a server socket
                ServerSocket serverSocket = new ServerSocket(8000);
                Platform.runLater(() -> taLog.appendText(new Date() +
                        ": Server started at socket 8000" +"\n"));

                // Ready to create chat room for the two clients
                while (true) {
                    Platform.runLater(() -> taLog.appendText(new Date() +
                            ": Wait for clients to join session " +  '\n'));

                    // Connect to first client
                    Socket client1 = serverSocket.accept();

                    Platform.runLater(() -> {
                        taLog.appendText(new Date() + ": client has joined chat room "
                                + '\n');
                        taLog.appendText("client1 IP address" +
                                client1.getInetAddress().getHostAddress() + '\n');
                    });

                    // Connect to player 2
                    Socket client2 = serverSocket.accept();

                    Platform.runLater(() -> {
                        taLog.appendText(new Date() +
                                ": Second client has joined chatroom " +  '\n');
                        taLog.appendText("client2 IP address" +
                                client2.getInetAddress().getHostAddress() + '\n');
                    });

                    // Launch a new thread for this session of two players
                    new Thread(new HandleSession(client1, client2)).start();
                }
            }
            catch(IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }
    //threadclass for chatroom
    class HandleSession implements Runnable{
        private Socket client1;
        private Socket client2;

        private DataInputStream fromClient1;
        private DataOutputStream toClient1;
        private DataInputStream fromclient2;
        private DataOutputStream toClient2;

        public HandleSession(Socket client1, Socket client2) {
            this.client1 = client1;
            this.client2 = client2;
        }
        public void run() {
            try {
                // Create data input and output streams
                fromClient1 = new DataInputStream(
                        client1.getInputStream());
                toClient1 = new DataOutputStream(
                        client1.getOutputStream());

                fromclient2 = new DataInputStream(
                        client2.getInputStream());
                toClient2  = new DataOutputStream(
                        client2.getOutputStream());

                // Continuously serve the client
                while (true) {
                    // Receive Message from the client
                    String message1 = fromClient1.readUTF().trim();
                    // Send message client2
                    toClient2.writeUTF(message1);



                    //Receive message from client2
                    String message2 = fromclient2.readUTF().trim();
                    //send Message to client1
                    toClient1.writeUTF(message2);



                    Platform.runLater(() -> {
                        taLog.appendText("Message received from client1: " +
                                message1 + '\n');
                        taLog.appendText("Message received from client2: " + message2 + '\n');
                    });
                }
            }
            catch(IOException ex) {
                ex.printStackTrace();
            }
        }
        private void sendMessage(DataOutputStream out, String sms)
                throws IOException {
            out.writeUTF(sms);
        }


    }
    public static void main(String[] args) {
        launch(args);
    }


}