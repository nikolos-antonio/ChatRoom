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
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class chatServer extends Application {

    private ArrayList<HandleSession> connections;
    private ServerSocket serverSocket;
    private boolean done;
    private ExecutorService pool;
    TextArea taLog = new TextArea();

    public chatServer() {
        connections = new ArrayList<>();
        done = false;
    }


    @Override
    public void start(Stage primaryStage) throws IOException {

        Scene scene = new Scene(new ScrollPane(taLog), 450, 200);
        primaryStage.setTitle("ChatServer Server"); // Set the stage title
        primaryStage.setScene(scene); // Place the scene in the stage
        primaryStage.show();// Display the stage

        new Thread( () -> {
            try {
                serverSocket = new ServerSocket(8000);
                pool = Executors.newCachedThreadPool();
                Platform.runLater(() -> taLog.appendText(new Date() + ": Server started at socket 8000" + "\n"));
                // Ready to create chat room for the two clients
                while (!done) {
                    // Connect to client
                    Socket client = serverSocket.accept();
                    HandleSession handle = new HandleSession(client);
                    connections.add(handle);
                    pool.execute(handle);

                    Platform.runLater(() -> {
                        taLog.appendText(new Date() + ": client has joined chat room " + '\n');
                        taLog.appendText("client IP address " + client.getInetAddress().getHostAddress() + '\n');
                    });

                }
            } catch (Exception ex) {
                ex.printStackTrace();
                try {
                    shutdown();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void broadcast(String message)
            throws IOException {
        for (HandleSession ch : connections) {
            if (ch != null) {
                ch.sendMessage(message);
            }
        }
    }

    public void shutdown() throws IOException {
        done = true;
        pool.shutdown();
        if (serverSocket.isClosed()){
            serverSocket.close();
        }
        for(HandleSession ch: connections) {
            ch.shutdown();
        }
    }

    //threadclass for chatroom
    class HandleSession implements Runnable {
        private Socket client;
        private DataInputStream fromClient;
        private DataOutputStream toClient;
        private String nickname;

        public HandleSession(Socket client) {
            this.client = client;
        }
        public void run() {
            try {
                // Create data input and output streams
                fromClient = new DataInputStream(client.getInputStream());
                toClient = new DataOutputStream(client.getOutputStream());
                toClient.writeUTF("Please enter a nickname: ");
                nickname = fromClient.readUTF().trim();

                System.out.println(nickname + " connected");
                broadcast(nickname + " joined the chat!\n");

                // Receive Message from the client
                String message;
                while ((message = fromClient.readUTF().trim()) != null) {
                    if(message.startsWith("/quit")) {
                        broadcast(nickname + " left the chat!\n");
                        shutdown();
                    } else {
                        broadcast(nickname + ": " + message + "\n");
                        taLog.appendText("Message received from client " + nickname + ": " + message + "\n");
                    }
                }
            }
            catch(IOException ex) {
                ex.printStackTrace();
                try {
                    shutdown();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void shutdown() throws IOException {
            fromClient.close();
            toClient.close();
            if (!client.isClosed()) {
                client.close();
            }
        }

        public void sendMessage(String message) throws IOException {
            toClient.writeUTF(message);
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}