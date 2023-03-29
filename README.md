This project is a simple multi-user chat system in Java. The system allows for multiple clients to chat with each other using a centralized server.

**TODO**: 

1. Chat Client
- Contact server using TCP sockets
- First promtp a welcome message asking for username 
- Prompt for text to be sent to other clients, sending message when users press enter
- Client will send all message enetered by the user to server and recieve and display all messages received from the chat group
- Exit the client by user inputting a line with '.' only. The client should send a sign off message to server, leave the chat group, and wait for confirmation message from the server.

2. Chat Server
- Server must keep track of all users in chat system and should operate on a known port.
- Server should listen and accept user's sign in requests, sending back a  welcome message when a user first joins the chat group to all users.
- Server should wait for a cleint to send a message, and each time a new message is recieved the server adds the user's name to the message and distributes it to the chat group.
- When a server recieved a sign-off message, it should send back the confirmation to the user and inform all other clients that the user has left.
- Design the chat message format, including the sign-in message, sign-off message, and regular messages.
