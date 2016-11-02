


package ringelection;
import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;


/**
 *
 * @author funda
 */
public class RingElection {  //Sould it implement runnable

    private static int defaultPort = 8001;  // Default port number, which acts as the starting port
    static String host ="localhost"; // declare the local host name
    static clientThread thread = new clientThread(); // declare a client thread
    public static int portNbr = 0, processNbr = 0; // declare the initial port number and process number
    public static boolean portConnStatFlag = false;  // declare port flag to check the port connection status
    static ServerSocket serverSocket; // declare a socket for server
    public static int coordinatorProcess = 0, processAlive = 0, crashedPort = 0; // declare some useful integer variables
    public static boolean isCoordinator = false, isCrashed = false, isIdle = true, crashFlag = false; // declare some useful boolean variables
        
    //Sending the election message to the next process and inform about the election
	    public void startElection(String token, int proccessNbr) {
	    	// if the process number is more than 5, minus it with 5 inorder to bring the number back within 5
	        if(proccessNbr>9) {
	        	proccessNbr = proccessNbr - 9;
	        }
	        try {
	        	// Create a socket based on the process number
	            Socket socket = new Socket(host, defaultPort+proccessNbr);
	            // Create the PrintWriter for the socket
	            PrintWriter out = new PrintWriter(socket.getOutputStream());
	            // send the token into the printwriter for that particular socket
	            out.println(token);
	            out.flush();  // flush the printwriter output
	            out.close();  // close the printwriter
	            socket.close();  // close the socket
	        } catch(Exception ex) {
	        	startElection(token, proccessNbr+1); // inform to next next if the next is unavailable
	        }
	    }  
            // Sending the exit message to the previous process in the ring whenever a process is removed
	    private void sendExitMessage(String token, int procID) {
	    	if(procID == 0 || procID < 0) {
	    		if(coordinatorProcess != 0) {
	    			procID = coordinatorProcess;
	    		}else {
	    			procID = 5;
	    		}
	        }
	        try {
	            Socket socket = new Socket(host, defaultPort+procID);
	            PrintWriter out = new PrintWriter(socket.getOutputStream());
	            out.println(token);
	            out.flush();
	            out.close();
	            socket.close();
	        } catch(Exception ex) {
	        	startElection(token, procID-1);  // inform to previous of previous if previous process is unavailable
	        }
	    }
                
    public static void main(String[] args) {
        // Add section to make jframe visible - change the commented section below
        
		
			/*TokenClient2 gui = new TokenClient2();
			gui.setVisible(true);		// setting the frame visible
			gui.setTitle("Ring Algorithm");*/
			
			// open server sockets starting with port 7081. Limit the number to 5
			// Check the available port number before making the connection
                        
                        RingElection gui = new RingElection();
                       // gui.setVisible(true);		// setting the frame visible
			//gui.setTitle("Ring Algorithm");
                        
			for(int i=1;i<10;i++)
			{
				ServerSocket tempSocket;
				portNbr = defaultPort + i;
				try{
					tempSocket= new ServerSocket(portNbr); // declare a new socket
					processNbr = portNbr - defaultPort; // Process number identification
					String tempProcess = Integer.toString(processNbr);
					pNo.setText(tempProcess); // Inform the GUI // doubt
					tempSocket.close(); // close the socket
					jButton4.append("Connected to the port : " + portNbr); //doubt 
					portConnStatFlag = true;
					break;  // break the for loop whenever a port is available
					
				}catch (IOException e){
					System.out.println("Socket is running at " + portNbr);  				
				}
			}
			
			// Limit the process when it exceeds 5
			if(portConnStatFlag == false) {
				System.out.println("Exceed the total number of alotted ports.....");
				System.exit(1);
			}
			else
			{
				// Connect the identified available port to the server socket
				try {
					serverSocket = new ServerSocket(portNbr);
					thread.start(); // Start the thread           //doubt
					thread.init(portNbr, processNbr, gui, serverSocket); // inform the initial variables  //doubt
				} catch (IOException e) {
					System.out.println("Something went wrong while trying to connect to port " + portNbr);
					System.exit(1);
				}
			}
		
        
    }
    
    
}

