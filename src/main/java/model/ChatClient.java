package src.main.java.model;

import java.io.*;
import java.net.*;
import java.util.*;
import src.main.java.controller.*;

public class ChatClient {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 1234;
    private static final String EXIT_COMMAND = ".";

    enum Status {
        WAIT, LOGIN, SEND
    };

    private Controller clientHandler;

    private String username;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner scanner;
    private String response;
    private boolean running = true;

    private ServerSender serverSender;

    public ChatClient(Controller clientController) {
        this.clientHandler = clientController;
        this.scanner = new Scanner(System.in);
    }

    public void initClient() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("IOException in initClient: " + e.getMessage());
        }
    }

    public void run() {
        try {
            clientHandler.toggleLoginVisible();
            // Recieve username, SERVER_IP, and SERVER_PORT

            // Connect to the server
            socket = new Socket(SERVER_IP, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Prompt for username
            System.out.print("Enter your username: ");
            username = scanner.nextLine();

            // Send sign-in message to server
            out.println("SIGNIN " + username);

            // Wait for acknowledgement message from server
            response = in.readLine();
            if (!response.equals("ACK")) {
                System.err.println("Unexpected server response: " + response);
                return;
            }

            clientHandler.closeLogin();
            clientHandler.toggleClientVisible();

            // Start listener thread here
            ClientListener clientListener = new ClientListener();
            Thread clientListenerThread = new Thread(clientListener);
            clientListenerThread.start();

            serverSender = new ServerSender();
            Thread serverSenderThread = new Thread(serverSender);
            serverSenderThread.start();

            while (running) {
                // Server will stay running until user inputs "."
            }

            // Close socket and streams
            socket.close();
            in.close();
            out.close();
            clientHandler.closeClient();

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }
    }

    public class ServerSender implements Runnable {
        Status stat = Status.WAIT;
        String message = "";

        @Override
        public void run() {
            while (running) {
                if (stat == Status.LOGIN) {
                    signIn();
                    stat = Status.WAIT;
                } else if (stat == Status.SEND) {
                    sendMessage(this.message);
                    stat = Status.WAIT;
                    this.message = "";
                } else {
                    continue;
                }
            }
        }

        public void signIn() {

        }

        public void sendMessage(String message) {
            if (message.equals(EXIT_COMMAND)) {
                out.println("SIGNOFF " + username);
            } else {
                out.println("MESSAGE " + message);
            }
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public ServerSender getServerSender() {
        return this.serverSender;
    }

    private class ClientListener implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    response = in.readLine();
                    if (response == null || response.equals("BYE")) {
                        running = false;
                        break;
                    }
                    clientHandler.handleMessageReceived(response);
                    System.out.println(response);
                }

            } catch (IOException e) {
                // Socket closed
            }
        }
    }
}
