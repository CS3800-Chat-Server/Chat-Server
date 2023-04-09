package main.java.chat;

import main.java.chat.server.ChatServer;

public class ChatServerApp {
    public static void main(String[] args) throws Exception{
        int port = 725;

        ChatServer server = new ChatServer(port);

        System.out.println("Server Listening on : " + server.getHostAddress() + ":" + port + ".");
    
        server.runServer();
    }
}
