/**
 * Authors: Daniel Hajnos, Toby Hwang, Lily Lu
 * ChatServerService class
 * The ChatServerService class is a multithreaded application that
 * serves as an endpoint for the client. It receives messages from the
 * client that it looks over, but also sends messages to other clients
 * that are connected to the same server.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatServerService extends Thread {
	private ChatUser serverToClientConnection;
	private OutputStream sendBuffer;
	private InputStream recvBuffer;
	
	private Vector<ChatUser> connectionList;
	
	private boolean isConnected;

	/**
	 * Constructor for the class. 
	 * @param socket
	 */
	ChatServerService(Socket socket, String userName, Vector<ChatUser> cu) {
		try {
			// Set up the connections
			serverToClientConnection = new ChatUser(socket, userName);
			connectionList = new Vector<ChatUser>(cu);
			
			// Set up the send and receive buffers for the socket.
			sendBuffer = serverToClientConnection.getUserSocket().getOutputStream();
			recvBuffer = serverToClientConnection.getUserSocket().getInputStream();
			
			isConnected = true;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	/**
	 * Updates the server and clients current connection list.
	 * @param cu: ArrayList of ChatUsers.
	 */
	public void updateUserConnections(Vector<ChatUser> cu) {
		// Remove the old list and add the new one.
		connectionList.clear();
		connectionList.addAll(cu);
		
		// Construct the USERLIST message.
		ArrayList<String> userList = new ArrayList<String>();
		String userListMessage = "";
		for(ChatUser user : connectionList) {
			userList.add(user.getUserName());
		}
		
		for(String user : userList) {
			userListMessage += user+";";
		}
		
		// Send the USERLIST message to the client, so that they can
		// also update their currently connected users.
		send("USERLIST:"+userListMessage);
	}
	
	/**
	 * Checks to see if the client is connected with the server.
	 * @return True if connected with client, false if we disconnected.
	 */
	public boolean isClientConnected() {
		return isConnected;
	}

	/**
	 * Gets the information about the client from this
	 * worker thread.
	 * @return The ChatUser object of this worker thread.
	 */
	public ChatUser getChatUser() {
		return serverToClientConnection;
	}
	
	/**
	 * Wrapper to send data to clients.
	 * @param message: Message to be sent.
	 */
	public void send(String message) {
		try {
			DataOutputStream out = new DataOutputStream(sendBuffer);
			out.writeUTF(message);
		} 
		catch (SocketException e) {
			isConnected = false;
			return;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Wrapper to receive data from clients.
	 * @return: Returns the message that was sent by the client.
	 */
	public String recv() {
		try {
			DataInputStream in = new DataInputStream(recvBuffer);
			return in.readUTF();
		} catch(SocketException e) {
			isConnected = false;
			return null;
		}
		catch (IOException e) {
			//e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Wrapper to send data to a socket with a specified output stream.
	 * @param message: The literal string message that we want to send.
	 * @param sendBuffer: The OutputStream from the socket that we want
	 * to send data too.
	 */
	public void send(String message, OutputStream sendBuffer) {
		try {
			DataOutputStream out = new DataOutputStream(sendBuffer);
			out.writeUTF(message);
		} catch(SocketException e) {
			isConnected = false;
			return;
		}
		catch (IOException e) {
			System.out.println("ERROR: Server send method");
		}
	}

	/**
	 * Wrapper to receive data from clients with a specified input stream.
	 * @return: Returns the message that was sent by the client.
	 */
	public String recv(InputStream recvBuffer) {
		try {
			DataInputStream in = new DataInputStream(recvBuffer);
			return in.readUTF();
		} catch(SocketException e) {
			isConnected = false;
			return null;
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Closes the input and output streams to the client. Also, closes the
	 * connection to the clients socket. Kills the thread
	 */
	public void closeConnection() {
		try {
			connectionList.remove(serverToClientConnection);
			updateUserConnections(connectionList);
			sendBuffer.close();
			recvBuffer.close();
			serverToClientConnection.getUserSocket().close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {	
		while(isConnected) {
			try {
				// Receive the message from the client associated with this user.
				String message = "";
				try {
					message = recv(serverToClientConnection.getUserSocket().getInputStream());
				} catch(SocketException e) {
					isConnected = false;
					break;
				}
				
				// If null, then we know no message was sent to the server,
				// so we kill this server's thread.
				if(message == null) {
					System.out.println("Connection Broken");
					isConnected = false;
					break;
				}

				// Split the message from the client to see what flag was sent,
				// how many users we need to send too, and the message to send 
				// to send to everyone.
				String messageSplit[] = message.split(":");
				
				// If we receive a CLOSE flag in our message from the client,
				// then close that connection.
				if(messageSplit[0].equals("CLOSE")) {
					// Gets the name of the user that disconnected.
					System.out.println("User " + serverToClientConnection.getUserName() +" has disconnected");
					isConnected = false;
					break;
				}		

				// If we receive a SENDTO flag in our message from the client,
				// then see which users we need to send to and what message.
				if(messageSplit[0].equals("SENDTO")) {
					// Gets the amount of users to send to 
					ArrayList<String> usersToSend = new ArrayList<String>();
					for(String user : messageSplit[1].split(";")) {
						System.out.println(user);
						usersToSend.add(user);
					}
						
					// A public message to everyone.
					if(connectionList.size()-1 == usersToSend.size()) {
						for(ChatUser user : connectionList) {
							try {
								// Build the message for public viewing PUBLIC:USERNAME:MESSAGE
								send("PUBLIC:"+serverToClientConnection.getUserName()+":"+messageSplit[2],
										user.getUserSocket().getOutputStream());
							} catch(SocketException e) {
								continue;
							}
							catch(IndexOutOfBoundsException e) {
								continue;
							}
						}
					}
					
					// Send a private message to a person or a group of people.
					else {
						// Runs through the list of users we need to send to.
						for(String privateUser : usersToSend) {
							// Finds that one user in the list. and sends the message to them.
							for(ChatUser privateChatUser : connectionList) {
								if(privateChatUser.getUserName().equals(privateUser)) {
									send("PRIVATE:"+serverToClientConnection.getUserName()+":"+privateChatUser.getUserName()+":"+messageSplit[2],
										privateChatUser.getUserSocket().getOutputStream());
									break;
								}
							}
						}
					}
				}
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
		}
		closeConnection();
	}
}
