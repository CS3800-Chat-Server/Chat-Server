package src.main.java.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class ChatBot implements Runnable {
    private Socket socket;
    private volatile boolean isRunning = true;
    private int id;
    private PrintWriter writer;
    
    public ChatBot(String ip, int port, int id) {
        try {
            this.socket = new Socket(ip, port);
            this.id = id;
            this.writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // We can use a printwriter to write messages to the client.
            // No buffered reader is necessary beacuse we do not need to display messages to
            // these bots.
            String username = "Bot" + id;
            writer.println("SIGNIN " + username);
            // Server is expecting initial signin message of "SIGNIN <username>"

            while (isRunning) {
                // Send some message
                // Sleep for a little to space messages out
                writer.println("MESSAGE Test message from " + username);
                Thread.sleep(2000); // 2 second sleep time
            }

            // Server is expecting ending signoff message of "SIGNOFF <username>"
            writer.println("SIGNOFF " + username);

            // Close socket and printwriter
            writer.close();
            socket.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println(e.getStackTrace());
        }

    }

    public static void main(String[] args) {
        int numClients;
        int port;
        String ip;

        if (args.length != 3) {
            System.err.println("Usage: <ip-address> <port> <number of clients>");
            return;
        }

        // Read in each command line argument for ip, port, and number of bot clients to
        // create
        ip = args[0]; // ip address
        port = Integer.parseInt(args[1]); // port number
        numClients = Integer.parseInt(args[2]); // number of bots/clients

        // We can save the ChatBots in this array so we can access them later and stop
        // them
        ArrayList<ChatBot> userArray = new ArrayList<>();

        /*
         * For the number of clients:
         * 1. instantiate and add each chatBot to the array
         * 2. Create a[nd] start a new thread with the ChatBot class
         */
        for (int i = 0; i < numClients; i++) {
            ChatBot bot = new ChatBot(ip, port, i);
            userArray.add(bot);
            new Thread(bot).start();
        }

        // To stop the bots we can input a keyword or just wait for any input
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        /*
         * For the number of clients:
         * 1. set each chatBot in the array's isRunning variable to false, so it will
         * break out of the loop outputting text
         */
        for (ChatBot bot : userArray) {
            bot.isRunning = false;
        }

        scanner.close();
    }
}
