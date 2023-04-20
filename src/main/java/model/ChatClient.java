package src.main.java.model;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import src.main.java.controller.*;
import java.util.*;

public class ChatClient {
    private static final String EXIT_COMMAND = ".";

    private Controller clientHandler;

    private String username;
    private Date timestamp = new Date();
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String response;
    private volatile Boolean isRunning = true;
    private volatile Boolean isLoggingIn = true;

    public ChatClient(Controller clientController) {
        this.clientHandler = clientController;
    }

    public void run() {
        this.clientHandler.toggleLoginVisible();

        try {
            while (this.isLoggingIn) {
                // Wait for login from GUI
            }

            this.clientHandler.closeLogin();
            this.clientHandler.toggleClientVisible();

            // Start listener thread
            ClientListener clientListener = new ClientListener();
            Thread clientListenerThread = new Thread(clientListener);
            clientListenerThread.start();

            while (this.isRunning) {
                /**
                 * Server will stay running until user inputs "."
                 * and ClientListener recieves sign off confirmation "BYE"
                 */
            }

            this.clientHandler.closeClient();

            // Close socket and streams
            this.socket.close();
            this.in.close();
            this.out.close();

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }

        System.exit(0);
    }

    public void tryLoginInfo(String username, String ip, Integer port) throws Exception {
        try {
            this.socket = new Socket(ip, port);
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Error 1");
            socket.close();
            in.close();
            out.close();
            throw e;
        }

        if (this.socket == null || this.socket.isClosed() || !this.socket.isConnected() || !this.socket.isBound()
                || this.socket.isInputShutdown() || this.socket.isOutputShutdown()) {
            System.out.println("Error 2");
            return;
        }

        // Send sign-in message to server
        this.out.println("SIGNIN " + username);

        // Wait for acknowledgement message from server
        try {
            this.response = in.readLine();
        } catch (IOException e) {
            System.out.println("Error 3");
            socket.close();
            in.close();
            out.close();
            throw e;
        }

        if (!this.response.equals("ACK")) {
            System.out.println("Error 4");
            socket.close();
            in.close();
            out.close();
            throw new IOException("Did not receive ACK from server");
        }

        this.username = username;
        isLoggingIn = false;
    }

    public void sendMessage(String message) {
        if (message.equals(EXIT_COMMAND)) {
            out.println("SIGNOFF " + this.username);
        } else {
            timestamp = new Date();
            out.println("MESSAGE " + message + " [" + timestamp + "]");
        }
    }

    private class ClientListener implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    response = in.readLine();
                    String parts[] = response.split(" ", 2);

                    if (response == null || parts[0].equals("BYE")) { // This User Signoff
                        isRunning = false;
                        break;
                    } else if (parts[0].equals("MESSAGE")) { // message
                        clientHandler.handleMessageReceived(parts[1]);
                    } else if (parts[0].equals("SIGNIN")) { // Signin
                        clientHandler.handleMessageReceived(parts[1]);
                    } else if (parts[0].equals("SIGNOFF")) { // Other User Signoff
                        clientHandler.handleMessageReceived(parts[1]);
                    }

                }
            } catch (IOException e) {
                // Socket closed
                isRunning = false;
            }
        }
    }
}
