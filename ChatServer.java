import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class ChatServer extends Application 
{
	private static ArrayList<ObjectOutputStream> outputToUsers = new ArrayList<ObjectOutputStream>(5);
	private TextArea taServer = new TextArea();
	private static final int MAX_USERS = 2;
	private static ServerSocket serverSocket = null;
	private boolean acceptingNewUsers = true;
	private int connectedUsers = 0;
	private static final int port = 9733; 
	
	public static void main(String[] args) 
	{
		launch(args);
	}
	
	@Override  
	public void start(Stage primaryStage) 
	{ 
		initializePrmaryStage(primaryStage);

	    new Thread( () -> 
	    { 
	      try 
	      { 
	        // Create a server socket 
	        serverSocket = new ServerSocket(port);
	        serverSocket.setReuseAddress(true);
	        Platform.runLater(() -> taServer.appendText(new Date() + ": Server started at socket: " + port + '\n'));  
	        acceptUsers();
	        
	        while (true) 
	        { 
		          // Listen for a new connection request 
		          Socket socket = serverSocket.accept();
		        	
		          // Create and start a new thread for the connection 
		          new Thread(new AddUserToChat(socket)).start(); 
	        } 
	      } 
	      catch(IOException ex) 
	      { 
	        System.err.println(ex); 
	      } 
	    }).start(); 
	 } 

	private void initializePrmaryStage(Stage stage)
	{
		taServer.setMinHeight(450);
		
		 // Create a scene and place it in the stage 
	    Scene scene = new Scene(new ScrollPane(taServer), 450, 400); 
	    stage.setTitle("ChatServer"); // Set the stage title 
	    stage.setScene(scene); // Place the scene in the stage 
	    stage.show(); // Display the stage 
	}
	
	private void acceptUsers()
	{
		acceptingNewUsers = true;
		Platform.runLater(() -> taServer.appendText(new Date() + ": " + "Accepting users" + '\n'));
		
	}
	
	private void refuseNewUsers()
	{
		acceptingNewUsers = false;
		Platform.runLater(() -> taServer.appendText("Maximum user capacity reached." + '\n'));
	}
	
	private void writeToAll(String s)
	{
		try 
		{
			for (int x = 0; x < outputToUsers.size(); x++)
			{
				outputToUsers.get(x).writeUTF(s);
				outputToUsers.get(x).flush();
			}
		} catch (IOException ex) 
		{
			ex.printStackTrace();
		} 
	}
	
	private void writeToLog(String s)
	{
		Platform.runLater(() -> 
	    { 
	        taServer.appendText(s + '\n'); 
	    }); 
	}
	
	private class AddUserToChat implements Runnable 
	{
		 private ObjectInputStream fromUser;
		 private ObjectOutputStream toUser;
		 private String username;
		 private Socket userSocket;

		public AddUserToChat(Socket userSocket) 
		 {
			 this.userSocket = userSocket;
			 connectedUsers++;
		 }

		@Override
	    public void run() 
		{
 			 try 
			 {
 				if (acceptingNewUsers)
 				{
 	 				establishUserConnection();
 	 				readMessagesFromUser();
 				}
 				else
 					serverFull();
			 } 
			 catch (Exception e) 
			 {
				 removeUser();
			 }
		}
		 /*
		 * Connects user to server
		 * @throws IOException if {@link ObjectInputStream#readUTF()} encounters an error
		 */
		 private void establishUserConnection() throws IOException 
		 {
			 // Get input and output streams from socket
			 toUser = new ObjectOutputStream(userSocket.getOutputStream());
			 fromUser = new ObjectInputStream(userSocket.getInputStream());
	
			 // Read and save username and save OOS to user in outputToUsers in ChatServer class
			 username = fromUser.readUTF();

  			 outputToUsers.add(toUser);
  			 
			 writeToLog(new Date() + ": " + username + " joined the chat.");
			 writeToAll(username + " joined the chat.");
	         
			 if (connectedUsers >= MAX_USERS)
		        refuseNewUsers();
		 }
		 
		 private void serverFull() throws IOException
		 {
			// Get input and output streams from socket
			 toUser = new ObjectOutputStream(userSocket.getOutputStream());
			 fromUser = new ObjectInputStream(userSocket.getInputStream());
			 
			 username = fromUser.readUTF();
			 outputToUsers.add(toUser);
			 
			 toUser.writeUTF("Server is full, please come back at a later time!");
			 toUser.flush();
			 
			 connectedUsers--;
			 outputToUsers.remove(toUser);
			
			 Thread.currentThread().interrupt();
		 }

		 /*
		 * Removes user from server
		 */
		 private void removeUser() 
		 {
			 // Decrease user counter and remove OOS to user
			connectedUsers--;
			outputToUsers.remove(toUser);
	
			 writeToLog(new Date() + ": " + username + " left the chat.");
			 writeToAll(username + " left the chat.");
	
			 // If server doesn't accept new users, start accepting them again
			 if (!acceptingNewUsers) acceptUsers();
		 }

		 /**
		 * Continually read messages from user
		 *
		 * @throws IOException if {@link ObjectInputStream#readUTF()} encounters an error
		 */
		 private void readMessagesFromUser() throws IOException 
		 {
			 while (true)
				 writeToAll(String.format("%s wrote: %s", username, fromUser.readUTF()));
		 }
	}
}

