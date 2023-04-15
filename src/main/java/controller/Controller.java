package src.main.java.controller;

import src.main.java.model.ChatClient;
import src.main.java.view.ClientGUI;

public class Controller implements IClientHandler {
    private ChatClient model;
    private ClientGUI view;

    public Controller() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                view = ClientGUI(this);
                view.setVisible(true);
            }
        });
        this.model = new ChatClient(this);
        this.model.run();
    }

    protected ClientGUI ClientGUI(Runnable runnable) {
        return null;
    }

    @Override
    public void handleMessageReceived(String message) {
        view.addMessage(message);
    }

    @Override
    public void handleMessageSent(String message) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleMessageSent'");
    }

    @Override
    public void addUser(String userName) {
        view.addUserName(userName);
    }

    public static void main(String[] args) {
        new Controller();
    }

}
