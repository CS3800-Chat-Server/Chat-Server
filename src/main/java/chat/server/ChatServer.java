package main.java.chat.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ChatServer {
    private ArrayList<ClientHandler> clientList;
    private ServerSocket socket;

    public ChatServer(int port) {
        this.clientList = new ArrayList<ClientHandler>();

        try {
            this.socket = new ServerSocket(port);
        } catch (Exception e) {
            System.out.println("Error starting server: " + e.getMessage());
        }
    }
    
    public String getHostAddress() throws UnknownHostException {
        //socket.getInetAddress();
        return InetAddress.getLocalHost().getHostAddress();
    }

    public void runServer() {
        while (true) {
            Socket clientSocket;
            try {
                clientSocket = socket.accept();
                System.out.println("Client Connected.");
                DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
                dos.writeBytes("Enter your name: \n");
            } catch (Exception e) {
                System.out.println("Error accepting client connection: " + e.getMessage());
                continue;
            }

            ClientHandler client = new ClientHandler(clientSocket);
            Thread clientThread = new Thread(client);

            clientList.add(client);

            clientThread.start();
        }
    }

    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader in;
        private DataOutputStream out;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new DataOutputStream(clientSocket.getOutputStream());

                //out.writeBytes("Enter your name: ");
                String name = in.readLine().trim();

                for (ClientHandler client : clientList) {
                    if (client != this) {
                        client.out.writeBytes(name + " has joined the chat room!\n");
                    }
                }

                while (true) {
                    String message = in.readLine();
                    if (message == null) {
                        break;
                    }

                    for (ClientHandler client : clientList) {
                        if (client != this) {
                            client.out.writeBytes(name + ": " + message + '\n');
                        }
                    }
                }
                
            } catch (Exception e) {
                System.out.println("Error handling client : " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (Exception e) {
                    System.out.println("Error closing client socket: " + e.getMessage());
                }
            }
        }
    }
}