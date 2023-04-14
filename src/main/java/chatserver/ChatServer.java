package src.main.java.chatserver;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private int PORT;
    private ServerSocket serverSocket;

    private Map<String, PrintWriter> clients = new HashMap<>();

    ChatServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        this.PORT = port;
    }

    public void run() {
        try {
            // Start the server socket
            System.out.println("Chat server started on port " + PORT);

            // Listen for incoming connections
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientThread(socket).start();
            }

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }
    }

    private synchronized void broadcast(String message, PrintWriter excludeClient) {
        for (PrintWriter client : clients.values()) {
            if (client != excludeClient) {
                client.println(message);
            }
        }
    }

    private synchronized void addClient(String username, PrintWriter out) {
        clients.put(username, out);
    }

    private synchronized void removeClient(String username) {
        clients.remove(username);
    }

    public static void main(String[] args) throws IOException {
        ChatServer server = new ChatServer(1234);
        server.run();
    }

    class ClientThread extends Thread {
        private Socket socket;
        private String username;
        private BufferedReader in;
        private PrintWriter out;

        public ClientThread(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Wait for sign-in message from client
                String request = in.readLine();

                String[] tokens = request.split(" ");
                if (tokens.length == 2 && tokens[0].equals("SIGNIN")) {
                    username = tokens[1];
                    broadcast(username + " joined the chat", out);
                    addClient(username, out);

                    // Send acknowledgement message to client
                    out.println("ACK");
                } else {
                    System.err.println("Invalid sign-in request from client");
                    return;
                }

                // Listen for incoming messages from client
                while (true) {
                    request = in.readLine();
                    if (request == null) {
                        break;
                    }

                    tokens = request.split(" ");
                    if (tokens.length >= 2 && tokens[0].equals("MESSAGE")) {
                        String message = username + ": "
                                + String.join(" ", Arrays.copyOfRange(tokens, 1, tokens.length));
                        broadcast(message, out);
                    } else if (tokens.length == 2 && tokens[0].equals("SIGNOFF")) {
                        String signoffMessage = username + " left the chat";
                        removeClient(username);
                        broadcast(signoffMessage, null);

                        // Send confirmation message to client
                        out.println("BYE");
                        break;
                    } else {
                        System.err.println("Invalid request from client: " + request);
                    }
                }

                // Close socket and streams
                socket.close();
                in.close();
                out.close();
            } catch (IOException e) {
                System.err.println("IOException: " + e.getMessage());
            }
        }
    }
}
