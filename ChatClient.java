import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ChatClient extends Application
{
	  private ObjectOutputStream toServer; 
	  private ObjectInputStream fromServer;
	
	  private GridPane gridpane = new GridPane();
	  private BorderPane mainPane = new BorderPane();
	  
	  private TextField tfUsername = new TextField(); 
	  private TextField tfUserInput = new TextField(); 
	  private TextArea ta = new TextArea();
	  private String username = "";
	  private String userinput = "";
	  private Socket socket;
	  private static final int port = 9733; 
	  
	
	public static void main(String[] args)
	{
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception 
	{
		// Panel p to hold the label and text field 
	    BorderPane paneForTextField = new BorderPane(); 
	    paneForTextField.setPadding(new Insets(5, 5, 5, 5));  
	    paneForTextField.setStyle("-fx-border-color: green"); 
	    
	    gridpane.add(tfUsername, 0, 0);
	    gridpane.add(tfUserInput, 0, 1);
	    
	    initializeUsernameTextField();
	    initializeUserInputTextField();
	    
	    tfUserInput.setPrefWidth(400.0);
	    
	    gridpane.setPrefWidth(450.0);
	    gridpane.setVgap(5.0);
	    gridpane.setHgap(25.0);
	    gridpane.setAlignment(Pos.CENTER);
	    paneForTextField.setBottom(gridpane);
	     
	    ta.setPrefHeight(450);
	    mainPane.setCenter(new ScrollPane(ta)); 
	    mainPane.setBottom(paneForTextField); 
	    
	    initializePrimaryStage(primaryStage);
	    
	    tfUsername.setOnAction(eusername -> 
	    { 
	    	if (!tfUsername.getText().equals("") && username.equals(""))
	    	{
	    		username = tfUsername.getText().trim();
	    		connecToServer();
	    	}
		}); 
	    
	    tfUserInput.setOnAction(euserinput -> 
	    { 
		    if (!username.equals(""))
		    {
			      // Get user input
				String Userinput = tfUserInput.getText().trim();
	
				// Send string
				sendToServer(Userinput);

		        tfUserInput.setText("");
		        Userinput = "";
		    }
	    });
	    	
	}
	
	private void initializeUsernameTextField()
	{
	    tfUsername.setPrefWidth(400.0);
	    tfUsername.promptTextProperty().set("Enter username here...");
	}
	
	private void initializeUserInputTextField()
	{
		tfUserInput.setPrefWidth(400.0);
		tfUserInput.promptTextProperty().set("Enter message here...");
	}
	
	private void initializePrimaryStage(Stage stage)
	{
		// Create a scene and place it in the stage 
	    Scene scene = new Scene(mainPane, 450, 400); 
	    stage.setTitle("Chat Client"); // Set the stage title 
	    stage.setScene(scene); // Place the scene in the stage 
	    stage.show(); // Display the stage 
	}
	
	private BorderPane getBorderPane() {return mainPane;}
	
	private void connecToServer()
	{
		try 
	    { 
		  socket = new Socket("localhost", port);  
	      fromServer = new ObjectInputStream(socket.getInputStream()); 
	      toServer = new ObjectOutputStream(socket.getOutputStream());
	      sendToServer(username);
	      establishServerConnection();
	    } 
	    catch (IOException ex) 
	    { 
	      ta.appendText(ex.toString() + '\n'); 
	    } 

	}
	
	private void establishServerConnection()
	{
    	if (socket.isConnected())
    	{
    		Thread thread1 = new Thread(new Runnable() 
    		{
    		    @Override
    		    public void run()
    		    {
    		    	while(true)
                    {
            			try 
            			{
    						userinput = fromServer.readUTF();
    	        			if(!userinput.equals(""))
    	        				receiveDataFromServer();
    					} catch (IOException e) 
            			{
    						e.printStackTrace();
    					}
                    }
    		    }
    		});
    		thread1.setDaemon(true);
    		thread1.start();
    	}
    	else
    	{
    		Platform.runLater(() -> 
    	    { 
    	    	ta.appendText("Server is currently full! Please try again later.\n");
    	    }); 	
    	}		
	}
	
	private void receiveDataFromServer() throws IOException
	{
		writeToLog(userinput);
	}
	
	private void sendToServer(String s)
	{
        try 
        {
        	toServer.writeUTF(s);
			toServer.flush();
		} 
        catch (IOException ex) 
        {
        	System.err.println(ex); 
		}
        
	}
	
	private void writeToLog(String s)
	{
        ta.appendText(s + '\n');
	}
}

