/**
 * Authors: Daniel Hajnos, Toby Hwang, Lily Lu
 * Main GUI class. Creates a GUI for the client side. Handles reading
 * input, receiving data, and sending data.
 */
import java.awt.*;
import java.awt.event.*;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("serial")
public class GUI extends Thread implements ActionListener {
	//toggle for enter press
	//between login window and chat window
	private boolean toggleWindow = true;
	
	private int connectedUsers;
	
	//Chat client backend
	private ChatClient chatClientBackend;
	
	private Timer timer;
	
	//Set up Login Screen
	private JFrame loginScreen;
	private JLabel welcome;
	private JLabel lblUsername;
	private JButton loginButton;
	private JTextField enterUsername;
	private JPanel loginPanel;
	private JScrollPane onlineScroller;
	
	//set up the Chat Client Window
	private JFrame chatScreen;
	private JButton sendMessage;
	private JTextArea messageDisplay;
	private JTextField messageInput;
	@SuppressWarnings("rawtypes")
	private JList online;
	private JPanel panel;
	private JLabel usersOnline;
	private JLabel messageExchange;
	private JLabel userInput;
	private DefaultListModel <String> listModel;
	private String messageHistory;
	private String username;
	private JLabel user;
	ArrayList<String> Users = new ArrayList<String>();
	ArrayList<String> Selected = new ArrayList<String>();
	
	public static void main(String[] args) {
		GUI gui = new GUI(550, 550);

	}
	
	//Display the chat client window
	public GUI(int width, int height) {
		connectedUsers = 0;
		chatScreen = new JFrame("Network Messaging Application");
		chatScreen.setDefaultCloseOperation ( JFrame.EXIT_ON_CLOSE );
		
		//function to set up the login page
		loginPage(400, 100);
		
		//set message history to an empty string
		messageHistory = "";
		
		//set the JPanel in the JFrame
		panel = new JPanel(new GridBagLayout());
		chatScreen.add(panel);
		
		listModel = new DefaultListModel<String>();

		//set up JList for the available users
		online = new JList<String>(listModel);
		online.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		online.setLayoutOrientation(JList.VERTICAL);
		onlineScroller = new JScrollPane(online);
		onlineScroller.setPreferredSize(new Dimension(100, 390));
		onlineScroller.setMinimumSize(new Dimension(100, 390));
		panel.add(onlineScroller, parameters(0, 0, 1.0, 1.0, new Insets(0,10,0,0), GridBagConstraints.WEST));
	
		
		//Set up the message exchange between two users
		messageDisplay = new JTextArea();
		messageDisplay.setLineWrap(true);
		messageDisplay.setWrapStyleWord(true);
		messageDisplay.setEnabled(false);
		JScrollPane messageScroller = new JScrollPane(messageDisplay);
		messageScroller.setPreferredSize(new Dimension(400, 390));
		messageScroller.setMinimumSize(new Dimension(400, 390));
		panel.add(messageScroller, parameters(0, 0, 1.0, 1.0, new Insets(0,0,0,15), GridBagConstraints.EAST));
		
		//Set up the message input field
		messageInput = new JTextField();
		messageInput.setPreferredSize(new Dimension(480,30));
		messageInput.setMinimumSize(new Dimension(480,30));
		messageInput.addActionListener(this);
		panel.add(messageInput, parameters(0, 0, 1.0, 1.0, new Insets(0,0,0,70),GridBagConstraints.SOUTH));
		
		//Add the send Button the JPanel
		sendMessage = new JButton("Send");
		sendMessage.addActionListener(this);
		panel.add(sendMessage, parameters(0, 0, 1.0, 1.0, new Insets(0,0,0,0),GridBagConstraints.SOUTHEAST));
		
		//Add the JLabel for the users online
		usersOnline = new JLabel("Users Online");
		panel.add(usersOnline, parameters(0, 0, 1.0, 1.0, new Insets(0,12,412,0),GridBagConstraints.WEST));
		
		//Add the JLabel for the message exchange
		messageExchange = new JLabel("Message Window");
		panel.add(messageExchange, parameters(0, 0, 1.0, 1.0, new Insets(0,0,412,300),GridBagConstraints.EAST));
		
		//Add the JLabel for the users to enter their message
		userInput = new JLabel("Enter Message");
		panel.add(userInput, parameters(0, 0, 1.0, 1.0, new Insets(0,5,27,0),GridBagConstraints.SOUTHWEST));
		
		//set the color of the JPanel
		panel.setBackground(new Color(176, 196, 222));
		//set the size of the JFrame
		chatScreen.setSize(width, height);
		//lock from resizing
		chatScreen.setResizable(false);
		//make it visible for the user
		chatScreen.setVisible(false);
		
		//set the Cursor in the message box
		messageInput.requestFocusInWindow();
		
	    chatScreen.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                System.out.println("Closed");
                e.getWindow().dispose();
                chatClientBackend.sendTo("CLOSE:", null, null);
            }
        });
	}
	
	//function to handle moving around the JFRAME components
	public GridBagConstraints parameters ( int gx, int gy, double wx, double wy, Insets i, int d ) {
		// Initialize grid constraint instance
		GridBagConstraints constraint = new GridBagConstraints ();
		// Set options
		constraint.weightx = wx;
		constraint.weighty = wy;
		constraint.gridy = gy;
		constraint.gridx = gx;
		constraint.insets = i;
		constraint.anchor = d;

		return constraint;
	}
	
	//action performed function

	public void actionPerformed(ActionEvent e) {
		//if either the login button is pressed or the enter button on the keyboard is pressed
		if(e.getActionCommand().equals("Login") || toggleWindow == true)
		{
			username = enterUsername.getText().trim().toString();
			loginScreen.setVisible(false);
			chatScreen.setVisible(true);
			toggleWindow = false;
			
			//Add the Jlabel for the specific user
			user = new JLabel("username: " + username);
			panel.add(user, parameters(0, 0, 1.0, 1.0, new Insets(0,0,412,20),GridBagConstraints.EAST));
			chatClientBackend = new ChatClient(username);
			//Start the backend for the client
			start();
			
			// Need to wait half a second or else Users Online
			// will not populate.
		/*	try {
				sleep(500);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// Adds the new list of users to the gui.
			for(String names : Users) {
				listModel.addElement(names);
			}*/
			
		}
		else
		{
			Selected.clear();
	    	//Stores the selected name after the message is sent
			for(Object names: online.getSelectedValuesList().toArray()) {
				System.out.println("Adding "+(String)names+" to selected");
				Selected.add((String) names);
			}
			
			listModel.clear();
			// Adds the new list of users to the gui.
			for(String names : Users) {
				listModel.addElement(names);
			}
			
			for(String names : Selected) {
				System.out.println(names);
			}
			
			String message = messageInput.getText();
			
			
			// Resets the message input box, clears the selection, and puts focus back 
			// onto the mbessage box.
			messageInput.setText("");
			messageInput.requestFocusInWindow();
			
			
			if(Selected.size() == 0) {
				JOptionPane.showMessageDialog(chatScreen, "Please Select a User");
			}
			else if(Selected.size() == Users.size()) {
				chatClientBackend.sendTo("SENDTO", message, Users);
			}
			else {
			// Sends the message to all the users selected.
				ArrayList<String> nameList = new ArrayList<String>();
				for(String user : Selected) {
					nameList.add(user);
				}
				messageHistory += ("[" + timeStamp() + "] PRIVATE: "+nameList+": "+ message +"\n");
				messageDisplay.setText(messageHistory);
				chatClientBackend.sendTo("SENDTO", message, nameList);
			}
			
			online.clearSelection();
		}
	}
	
	//set up the Jframe login page
	private void loginPage(int width, int height){
		loginScreen = new JFrame("Login Page");
		welcome = new JLabel("Welcome to the 342 Chat App");
		lblUsername = new JLabel("Enter Username");
		loginButton = new JButton("Login");
		enterUsername = new JTextField();
		loginPanel = new JPanel();
		
		//set the JPanel in the JFrame
		loginPanel = new JPanel(new GridBagLayout());
		loginScreen.add(loginPanel);
		
		//Set up the username input field
		enterUsername.setPreferredSize(new Dimension(300,30));
		enterUsername.addActionListener(this);
		loginPanel.add(enterUsername, this.parameters(0, 0, 1.0, 1.0, new Insets(0,0,0,50),GridBagConstraints.SOUTH));
		
		//SetUp the Login Button
		loginButton.addActionListener(this);
		loginPanel.add(loginButton, this.parameters(0, 0, 1.0, 1.0, new Insets(0,0,0,0),GridBagConstraints.SOUTHEAST));
		
		//Set up the JLabel TItle
		welcome.setFont(new Font("Cambria", Font.BOLD, 12));
		loginPanel.add(welcome, this.parameters(0, 0, 1.0, 1.0, new Insets(0,0,0,0),GridBagConstraints.NORTH));
		
		//Set up the JLabel Username
		loginPanel.add(lblUsername, this.parameters(0, 0, 1.0, 1.0, new Insets(0,0,30,245),GridBagConstraints.SOUTH));
		
		//set the color of the JPanel
		loginPanel.setBackground(new Color(176, 196, 222));
		
		//set the size of the JFrame
		loginScreen.setSize(width, height);
		//lock from resizing
		loginScreen.setResizable(false);
		//make it visible for the user
		loginScreen.setVisible(true);
		
	}
	
	//Create the time stamp for the message
	private String timeStamp(){
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		String time = sdf.format(cal.getTime()); 
		return time;
	}
	
	/**
	 * Thread
	 */
	public void run() {
		while(true) {
			
			if(chatClientBackend == null) { 
				continue;
			}
			
			String receiveMessage = chatClientBackend.receiveMessage();
			
			
			// If nothing was sent, then don't do anything.
			if(receiveMessage == null) {
				break;
			}
			
			System.out.println(receiveMessage);
			// Split the message with flag:message/userlist: message/nothing
			String splitMessage[] = receiveMessage.split(":");
			
			
			// If a new user message comes in, then create a new list of currently
			// connected users.
			if(splitMessage[0].equals("USERLIST")) {
				System.out.println("CLIENT USERSLIST COND");
				// Clear the current list of users.
				Users.clear();
				listModel.clear();
				
				// Create the new list of users.
				for(String names : splitMessage[1].split(";")) {
					if(names.equals(username))
						continue;
					Users.add(names);
					listModel.addElement(names);
				}
				connectedUsers = Users.size();
			}
			
			
			if(splitMessage[0].equals("PUBLIC")) {
				String fromUserName = splitMessage[1];
				messageHistory += ("[" + timeStamp() + "] (" + fromUserName + "): " + splitMessage[2] + "\n");
				messageDisplay.setText(messageHistory);
			}
			
			if(splitMessage[0].equals("PRIVATE")) {
				String fromUserName = splitMessage[1];
				messageHistory += ("[" + timeStamp() + "] PRIVATE (" + fromUserName + "): " + splitMessage[3] + "\n");
				messageDisplay.setText(messageHistory);
				System.out.println("GOT PRIVATE MESSAGE");
			}
			
		}
	}
}
