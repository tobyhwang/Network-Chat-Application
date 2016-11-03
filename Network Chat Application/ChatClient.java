
/**
 * Authors: Daniel Hajnos, Toby Hwang, Lily Lu
 * ChatClient Class
 * Chat client that establishes a user to the chat server.
 * Takes the message and the people the user wants to send the message to
 * and formats it with the correct flags to send to the server.
 * message with the right flags to send to the chat server.
 * It receives messages from the server and gives it to the GUI to process
 */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ChatClient {
    final String machineName = "localhost";
    final int portNum = 4000;
    boolean isConnected;
    
    private Socket echoSocket; // socket the client is connected to  
	private DataOutputStream out; // messages are sent to out
	private DataInputStream in; // read messages from in
    private String userName;
    //ChatUser chatUser; // userName and connected socket
    
    public ChatClient(String _userName){
        isConnected = false;
        userName = _userName;
        doManageConnection();
    }
    
    /**
     * Attempts to send a message to the server with a list of 
     * recipients 
     * @param messageText message this client is sending
     * @param userNames
     * @return true when the message was sent successfully
     *         false when the message failed to sent or the
     *         message was empty and nothing was sent
     */
    public boolean sendTo(String flag, String messageText, ArrayList<String> userNames)
    {
      if(isConnected){
    	  // Special case where we have to send username to server when the client
    	  // first connects or when we want to close the connection with the server.
    	  if(userNames == null) {
    		  try {
    			  if(flag.equals("CLOSE")) {
    				  out.writeUTF("CLOSE:");
    				  closeConnection();
    				  System.exit(0);
    			  }
    			  if(flag.equals("USERNAME")) {
    				  out.writeUTF("USERNAME:"+messageText);
    				  return true;
    			  }
    		  } catch(IOException e) {
    			  e.printStackTrace();
    		  }
    	  }
    	  else {
    	  
          String userList = userNames.toString();
          userList = userList.replaceAll("[,]", ";");
          userList = userList.replaceAll("[^a-zA-Z0-9;]", "");
          messageText = messageText.trim();
          if (messageText == ""){
              return false;
          }
          try {
        	System.out.println("Sending message: "+messageText+ " TO "+userList);
			out.writeUTF("SENDTO:" +userList + ":" + messageText);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
          return true;
        //  String pattern = "SENDTOGROUP:[a-zA-Z0-9]+";
      }
      }
      return false;
      
    }
    
    /**
     * Get the message sent from the server (if any)
     * @return message sent from the server
     */
    public String receiveMessage()  {
        String receive;
        try {
            if ((receive = in.readUTF()) != null){
                return receive;
            }
        }
        catch (EOFException e) {
        	return null;
        }
       catch(SocketException e) {
            	return null;
       }
       catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    
    /**
     * Connect to the server if it not connected
     * Disconnect form the server if it is connected
     * @return string describing the error or success 
     */
    public String doManageConnection(){
        if (isConnected == false)
        {
          try {

              echoSocket = new Socket(machineName, portNum );
              out = new DataOutputStream(echoSocket.getOutputStream());
              in = new DataInputStream(echoSocket.getInputStream());
              isConnected = true;
              out.writeUTF("USERNAME:" + userName);
              return "CONNECTED";
          } catch (NumberFormatException e) {
              return "NO SERVER PORT";
          } catch (UnknownHostException e) {
              return "NO HOST NAME";
          } catch (IOException e) {
              return "COULD NOT CONNECT TO " + machineName;
          }

        }
        else
        {
          try 
          {
            out.close();
            in.close();
            echoSocket.close();
            isConnected = false;
          }
          catch (IOException e) 
          {
              return "FAILED TO CLOSE SOCKET";
          }
        }
        return "";

    }

    
    /**
     * Try to close the socket connection to the server
     */
    public void closeConnection(){
        if(isConnected){
            try 
            {
              out.writeUTF("CLOSE");
              out.close();
              in.close();
              echoSocket.close();
              isConnected = false;
            }
            catch (IOException e) 
            {
                e.printStackTrace();
            }
        }
    }
}
