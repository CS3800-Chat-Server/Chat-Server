package main.java.chat.server;

import java.net.ServerSocket;
import java.util.ArrayList;

public class ChatServer {
    private ArrayList<ClientHandler> clientList;
    private ServerSocket socket;

    public ChatServer() {
        // TODO: Constructor
    }

    public void runServer() {
        // TODO: Implement main function for server to listen and accept clients
    }

    private static class ClientHandler implements Runnable {
    
        @Override
        public void run() {
            // TODO Implement ClientHandler run()
            
        }
    }
}