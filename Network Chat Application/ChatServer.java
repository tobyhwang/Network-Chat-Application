/**
 * Authors: Daniel Hajnos, Toby Hwang, Lily Lu
 * ChatServer Class
 * Multithreaded chat server that listens for chat clients to connect to it.
 * Then using a helper class spawn off new threads to handle the incoming 
 * connections.
 */
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;


/**
 * The main server for the chat program. 
 */
public class ChatServer {
	
	private ServerSocket serverSocket;
	private int portNumber;
	private int connectedUsers;
	private Socket connectionSocket;
	private Vector<ChatUser> connectionList;
	private Vector<ChatServerService> chatServerServiceList;
	
	public static void main(String [] args) {
		if(args.length < 2) {
			System.out.println("No port given, defaulting to port 4000");
			ChatServer chatServer = new ChatServer(4000);
		}
		else {
			ChatServer c = new ChatServer(Integer.parseInt(args[1]));
		}
	}
	
	/** 
	 * Constructor for the chat server. Listens for new clients
	 * wanting to connect. Once the server receives a new client,
	 * it will create a new thread for handling that client.
	 * @param portNumber: The port that the server binds
	 * and listens on.
	 */
	ChatServer(int portNumber) {
		connectionList = new Vector<ChatUser>();
		chatServerServiceList = new Vector<ChatServerService>();
		this.portNumber = portNumber;
		connectedUsers = 0;
		
		boolean isUsernameTaken = false;
		try {
			// Bind the server onto the specified port and start to 
			// listen for connections.
			serverSocket = new ServerSocket(portNumber); 
			System.out.println("Server Created on port " + portNumber);
			
			// Main server loop. Listens for new connections and adds the
			// client to the list of clients that a separate thread runs.
			while(true) {
				// Accepts an incoming connection from a client. Will pause here 
				// until a new client tries to connect.
				connectionSocket = serverSocket.accept();
				
				// Receive a user name for the connection client
				String userName = recv(connectionSocket.getInputStream());
				System.out.println(userName);
				if(userName == null) {
					System.out.println("Username is null");
					connectionSocket = null;
					continue;
				}
				
				// Parses the message to make sure it is a username being sent.
				String userNameSplit[] = userName.split(":");
				if(userNameSplit[0].equals("USERNAME")) {
					
					// Checks to see if the user name is already in use.
					for(ChatUser chatUser : connectionList) {
						if(chatUser.getUserName().equals(userNameSplit[1])) {
							connectionSocket.close();
							isUsernameTaken = true;
							break;
						}
					}
					
					// If the username is already taken, then close the connection
					// with the client.
					if(isUsernameTaken) {
						System.out.println("user name already in use");
						isUsernameTaken = false;
						connectionSocket.close();
						continue;
					}
					
					ChatUser cu = new ChatUser(connectionSocket, userNameSplit[1]);
					connectionList.add(cu);
					System.out.println("Client connected with server");
					
					ChatServerService css = new ChatServerService(connectionSocket, userNameSplit[1], connectionList);
					chatServerServiceList.add(css);
					Thread serverServiceThread = new Thread(css);
					serverServiceThread.start();
					
					connectedUsers++;
					
					for(ChatServerService css2 : chatServerServiceList) {
						css2.updateUserConnections(connectionList);
					}

				}
				else {
					System.out.println("ERROR: User connecting to server");
					try {
						connectionSocket.close();
					} catch(IOException e) {
						System.out.println("ERROR: Closing socket on server");
						e.printStackTrace();
					}
				}
			}
		} 
		// Error out and close the program if something goes wrong.
		catch (IOException e) {
			System.out.println("ERROR: Server creation on port "+portNumber);
			System.exit(1);
		}
	}
	
	/**
	 * Wrapper to send data to a socket.
	 * @param message: The literal string message that we want to send.
	 * @param sendBuffer: The OutputStream from the socket that we want
	 * to send data too.
	 */
	public void send(String message, OutputStream sendBuffer) {
		try {
			DataOutputStream out = new DataOutputStream(sendBuffer);
			out.writeUTF(message);
		} 
		catch (IOException e) {
			System.out.println("ERROR: Server send method");
			System.exit(1);
		}
	}

	/**
	 * Wrapper to receive data from clients.
	 * @return: Returns the message that was sent by the client.
	 */
	public String recv(InputStream recvBuffer) {
		try {
			DataInputStream in = new DataInputStream(recvBuffer); 
			return in.readUTF();
		} 
		catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}
}