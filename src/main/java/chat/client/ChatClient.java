package main.java.chat.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ChatClient implements Runnable{
    private Socket socket;
    private BufferedReader serverIn;
    private BufferedReader userIn;
    private DataOutputStream out;

    public ChatClient (String serverAddress, int serverPort) {
        try {
            this.socket = new Socket(serverAddress, serverPort);

            serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());

            Thread listenThread = new Thread(new ServerListener());
            listenThread.start();

            userIn = new BufferedReader(new InputStreamReader(System.in));

        } catch (Exception e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while(true) {
                String line = userIn.readLine();
                if (line == null)
                    break;
                out.writeBytes(line + '\n');
            }
        } catch (Exception e) {
            //
        }
    }

    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    String line = serverIn.readLine();
                    if (line == null)
                        break;
                    System.out.println(line);
                }
            } catch (Exception e) {
                System.out.println("Error reading from server: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (Exception e) {
                    System.out.println("Error closing socket: " + e.getMessage());
                }
            }
            
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java ChatClient <server_address> <server_port>");
            System.exit(1);
        }

        String serverAddress = args[0];
        int serverPort = Integer.parseInt(args[1]);

        ChatClient client = new ChatClient(serverAddress, serverPort);
        
        Thread clientThread = new Thread(client);

        clientThread.start();
    }
}