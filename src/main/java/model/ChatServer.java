package src.main.java.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ChatServer {
    private static final int PORT = 277;
    private final int numQueues = 5;
    private ServerSocket serverSocket;

    final private Map<Integer, PrintWriter> clients = new ConcurrentHashMap<Integer, PrintWriter>();
    final private ArrayList<LinkedBlockingQueue<String>> queuelist = new ArrayList<LinkedBlockingQueue<String>>(5);

    public void run() throws IOException {
        try {
            // Start the server socket
            serverSocket = new ServerSocket(PORT);
            System.out.println("Chat server started on port " + PORT);

            // Initialize list of message queues and launch threads
            for (int i = 0; i < numQueues; i++) {
                queuelist.add(new LinkedBlockingQueue<String>());
                QueueThread q = new QueueThread(queuelist.get(i));
                Thread qThread = new Thread(q);
                qThread.start();
            }

            // Starting id for first client
            int clientId = 0;

            // Accept incoming connections, launch ClientThread, and increment Id
            while (true) {
                Socket socket = serverSocket.accept();

                ClientThread ct = new ClientThread(socket, clientId);
                Thread clientThread = new Thread(ct);
                clientThread.start();

                clientId++;
            }
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        } finally {
            serverSocket.close();
        }
    }

    private synchronized void broadcast(String message, int clientId) {
        queuelist.get(clientId % queuelist.size()).add(message);
    }

    private synchronized void addClient(Integer id, PrintWriter out) {
        clients.put(id, out);
    }

    private synchronized void removeClient(Integer id) {
        clients.remove(id);
    }

    public static void main(String[] args) throws IOException {
        ChatServer server = new ChatServer();
        server.run();
    }

    private class ClientThread implements Runnable {
        final private Socket socket;
        final private int id;
        private String username;
        private BufferedReader in;
        private PrintWriter out;

        public ClientThread(Socket socket, int id) {
            this.socket = socket;
            this.id = id;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Wait for sign-in message from client
                String request = in.readLine();

                // Split message into tokens
                String[] tokens = request.split(" ");

                // Process sign in message from client
                if (tokens.length >= 2 && tokens[0].equals("SIGNIN")) {
                    username = String.join(" ", Arrays.copyOfRange(tokens, 1, tokens.length));
                    broadcast(tokens[0] + " " + username + " joined the chat", id);
                    addClient(id, out);

                    // Send acknowledgement message to client
                    out.println("ACK");
                } else {
                    System.err.println("Invalid sign-in request from client");
                    out.println("Invalid sign-in request to server");
                    out.println("BYE");
                    socket.close();
                    in.close();
                    out.close();
                    return;
                }

                // Listen for incoming messages from client
                while (true) {

                    request = in.readLine();
                    if (request == null) {
                        break;
                    }

                    // Split message into tokens
                    tokens = request.split(" ");

                    // Process message from client
                    if (tokens.length >= 2 && tokens[0].equals("MESSAGE")) {
                        String message = tokens[0] + " " + username + ": "
                                + String.join(" ", Arrays.copyOfRange(tokens, 1, tokens.length));
                        broadcast(message, id);
                    } else if (tokens.length >= 2 && tokens[0].equals("SIGNOFF")) {
                        String signoffMessage = tokens[0] + " " + username + " left the chat";
                        removeClient(id);
                        broadcast(signoffMessage, id);

                        // Send closing confirmation acknowledgement to client
                        out.println("BYE");
                        break;
                    } else if (tokens.length == 1) {
                        continue;
                    } else {
                        System.err.println(
                                "Invalid request from client " + username + " (id " + id + ") " + ": " + request);
                    }
                }

            } catch (IOException e) {
                System.err.println("IOException handling user message: " + e.getMessage());
            } finally {
                try {
                    // Close socket and streams
                    socket.close();
                    in.close();
                    out.close();
                } catch (IOException e) {
                    System.err.println("IOException closing sockets: " + e.getMessage());
                }
            }
        }
    }

    private class QueueThread implements Runnable {
        private final LinkedBlockingQueue<String> queue;

        QueueThread(LinkedBlockingQueue<String> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String headMessage = (String) queue.poll();

                    if (!(headMessage == null)) {
                        for (Map.Entry<Integer, PrintWriter> pair : clients.entrySet()) {
                            pair.getValue().println(headMessage);
                        }
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("Error in queue thread : " + e.getMessage());
            }
        }
    }
}
