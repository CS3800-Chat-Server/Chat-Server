package src.main.java.controller;

public interface IClientHandler {
    void handleMessageReceived(String message);

    void handleMessageSent(String message);

    void addUser(String userList);
}
