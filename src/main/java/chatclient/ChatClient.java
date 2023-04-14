package src.main.java.chatclient;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatClient {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 1234;
    private static final String EXIT_COMMAND = ".";

    private String username;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner scanner;
    private String response;

    public ChatClient() {
        scanner = new Scanner(System.in);
    }

    public void run() {
        try {
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

            // Start listener thread here
            ClientListener clientListener = new ClientListener();
            Thread clientListenerThread = new Thread(clientListener);
            clientListenerThread.start();

            // Start reading user input and sending messages to server
            System.out.println("Enter your messages (type '.' to quit):");
            String input;
            while ((input = scanner.nextLine()) != null) {
                if (input.equals(EXIT_COMMAND)) {
                    // Send sign-off message to server
                    out.println("SIGNOFF " + username);
                    break;
                }
                // Send message to server
                out.println("MESSAGE " + input);
            }

            // Close socket and streams
            socket.close();
            in.close();
            out.close();

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        client.run();
    }

    private class ClientListener implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    response = in.readLine();
                    if (response == "BYE")
                        break;
                    System.out.println(response);
                }
            } catch (IOException e) {
                System.err.println("Client Listener IOException: " + e.getMessage());
            }
        }
    }
}
