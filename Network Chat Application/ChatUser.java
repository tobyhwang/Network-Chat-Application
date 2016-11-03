/**
 * Authors: Daniel Hajnos, Toby Hwang, Lily Lu
 * ChatUser Class
 * Stores information for the users connected to the server.
 */

import java.net.*;

public class ChatUser {
	private Socket userSocket;
	private String userName;

	/** 
	 * Base constructor for the class. Assigns null for the  user name and socket.
	 */
	ChatUser() {
		userSocket = null;
		userName = null;
	}
	
	/**
	 * Constructor for the class. Takes in a socket that the client is connected 
	 * to on the server and a specified user name from the gui.
	 * @param socket: The socket that the user is using to contact
	 * the server.
	 * @param name: The name of the user in the chat program.
	 */
	ChatUser(Socket socket, String name) {
		userSocket = socket;
		userName = name;
	}
	
	/**
	 * Gets the socket that the client is connected to on the server. 
	 * @return The users socket on the server.
	 */
	public Socket getUserSocket() {
		return userSocket;
	}
	
	/**
	 * Gets the Scoket 
	 * @return The user name of the client.
	 */
	public String getUserName() {
		return userName;
	}
}
