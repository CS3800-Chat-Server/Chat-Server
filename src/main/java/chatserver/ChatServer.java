package src.main.java.chatserver;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class ChatServer {
    private static final int PORT = 1234;
    private ServerSocket serverSocket;

    private Map<String, PrintWriter> clients = new HashMap<>();
    private LinkedBlockingQueue<String> pbq = new LinkedBlockingQueue<String>();

    public void run() throws IOException {
        try {
            // Start the server socket
            serverSocket = new ServerSocket(PORT);
            System.out.println("Chat server started on port " + PORT);

            // Start queue thread
            QueueThread qt = new QueueThread(pbq);
            Thread queueThread = new Thread(qt);
            queueThread.start();

            // Listen for incoming connections
            while (true) {
                Socket socket = serverSocket.accept();

                ClientThread ct = new ClientThread(socket);
                Thread clientThread = new Thread(ct);
                clientThread.start();
            }

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        } finally {
            serverSocket.close();
        }
    }

    private synchronized void broadcast(String message, PrintWriter excludeClient) {
        pbq.add(message);
    }

    private synchronized void addClient(String username, PrintWriter out) {
        clients.put(username, out);
    }

    private synchronized void removeClient(String username) {
        clients.remove(username);
    }

    public static void main(String[] args) throws IOException {
        ChatServer server = new ChatServer();
        server.run();
    }

    private class ClientThread implements Runnable {
        private Socket socket;
        private String username;
        private BufferedReader in;
        private PrintWriter out;

        public ClientThread(Socket socket) {
            this.socket = socket;
        }

        @Override
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

                    // Split messaage and evaluate type
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

    private class QueueThread implements Runnable {
        private LinkedBlockingQueue<String> queue;

        QueueThread(LinkedBlockingQueue<String> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String headMessage = (String) queue.poll();

                    if (headMessage == null) {
                        continue;
                    } else {
                        String excludeClient = headMessage.split(":")[0].trim();
                        excludeClient = excludeClient.split(" ")[0];
                        for (Map.Entry<String, PrintWriter> pair : clients.entrySet()) {
                            if (!pair.getKey().equals(excludeClient)) {
                                pair.getValue().println(headMessage);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("QUEUE THREAD ERROR : " + e.getMessage());
            }
        }
    }
}
