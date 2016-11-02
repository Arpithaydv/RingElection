/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ringelection;
import java.io.*;
import java.net.*;
import java.util.*;
/**
 *
 * @author funda
 */
public class clientThread extends Thread implements Runnable { //Doubt - should it implement runnable
    
    // declare all the variables 
	public int processNumber;
	public int portNumber;
	public BufferedReader readBuffer;
	public PrintWriter printNext;
	public RingElection gui;  //doubt
	public ServerSocket servSocket;
	public Socket client;	
	public int lastPortNumber=0;
	public String processString;
	public int clientPortNumber;
	public int defaultPort = 8001;
	public int coordinatorProcess = 0;
	static String host ="localhost"; // declare the local host name
	
        // thread initializing
	public void init(int portNumber, int processNumber ,RingElection gui, ServerSocket servSocket){
		
		this.portNumber = portNumber; // assign the port number
		this.processNumber = processNumber; // assign the process number 
		this.gui = gui; // assign the frame
		this.servSocket = servSocket;	 // assign the socket
		gui.jButton4.append("\n Started Thread");  //Doubt
	}
	// thread to update the reset variables
	public void informReset(int portNumber, int processNumber ,ServerSocket servSoc) {
		this.portNumber = portNumber; // assign the port number
		this.processNumber = processNumber; // assign the token 
		this.servSocket = servSocket;	 // assign the socket
	}
    	// run method of the thread
	public void run(){
		
		while(true) {
		
			try {
				
				client = servSocket.accept(); // Accept the incoming socket
				readBuffer = new BufferedReader(new InputStreamReader(client.getInputStream()));		//Open input reader
				
				//if(readBuff.ready()) {
					
		            String token = readBuffer.readLine(); // Read the received token
		            
		            Thread.sleep(2000);
		            
		            // String Tokenize the received token
		            StringTokenizer stringTokenizer = new StringTokenizer(token);					//Break the token into strings 
		            switch(stringTokenizer.nextToken()) {											//iterate through string's first word
		                case "ELECTION_MESSAGE":  // action to be performed when it is an election message
		                	
		                	// Print the received token
		                	gui.jButton4.append("\n Token In:  "+token); //doubt
		                	gui.isIdle = false;
		                	
		                    try {
		                    	// if the election message reached the initiated process, find the eligible coordinator
		                        if(Integer.parseInt(stringTokenizer.nextToken()) == processNumber) {
		                            int[] processes = new int[5];
		                            processes[0] = processNumber;
		                            int counter = 1;
		                            while(stringTokenizer.hasMoreTokens()) {
		                            	processes[counter] = Integer.parseInt(stringTokenizer.nextToken());
		                                counter++;
		                            }
		                            findNewCoordinator(processes);  // find the coordinator
		                            if(coordinatorProcess != 0) {
		                            	// inform the coordinator to other processes
		                            	sendCoordinatorMessage(coordinatorProcess, processNumber, processNumber);
		                            }
		                        }
		                        else {
		                        	// if not the initiated process, just add the process number to the token and pass the token to next process
		                        	Thread.sleep(2000);
		                        	gui.jButton4.append("\n Token Out:  "+token+" "+processNumber); //doubt
		                        	sendTokenToNextProcess(token+" "+processNumber, processNumber+1);
		                        }
		                    } catch(Exception ex) {
		                        ex.printStackTrace();
		                    }
		                    break;
		                case "COORDINATOR_MESSAGE":  // action to be performed when it is an coordinator message
		                	String token2 = stringTokenizer.nextToken();  // coordinator number
		                	String token3 = stringTokenizer.nextToken();   // identified process
		                    try {
		                    	Thread.sleep(2000);
		                        if(Integer.parseInt(token2) == processNumber) {  // if the message is passed to the coordinator
		                            gui.isCoordinator = true;
		                            gui.jbutton4.append("\n This is the new Coordinator..!!! Elected by the Process " + token3); //Doubt
		                        }
		                        else { // informing the previous coordinator about the new coordinator
		                            if(gui.isCoordinator == true) {
		                            	gui.jButton4.append("\n" + processNumber + " is not coordinator anymore"); //Doubt
		                            	gui.isCoordinator = false;
		                            }
		                        }
		                        // if the coordinator message is circled backed to the elector
		                        if(Integer.parseInt(token3) == processNumber) {
		                        	gui.jButton4.append("\n New Coordinator is elected ---> " + token2 + " and informed all other process"); //Doubt
		                        	gui.btnElect.setEnabled(true); // Enable the Manual Election button //Doubt
		                        	gui.btnRefresh.setEnabled(true); // Enable the refresh button //Doubt
		                        	if(gui.isCrashed == false){ // Enable the Crash button if not crashed
		                        		gui.btnCrash.setEnabled(true); //Doubt
		                        	}
		                        	gui.isIdle = true;
		                        	// Start sending the Alive token in order to keep the ring process in sync
		                        	if (processNumber == coordinatorProcess){ // if the elector is the coordinator
		                        		nextAliveProcess(processNumber+1); // then start the alive message from the next available process
		                        	} else { // or else start the alive message from the current process
		                        		String tokenAlive = "ALIVE " + coordinatorProcess + " " + processNumber;
		                        		verifyAliveProcess(gui.coordinatorProcess, processNumber+1, processNumber+1, tokenAlive);
		                        	}
	                            }
		                        else { // if the coordinator message is received by other process
		                        	if(!gui.isCoordinator) {  // Print the new coordinator info
		                        		gui.jButton4.append("\n New Coordinator is --> " + token2 + "  Elected By " + token3); //Doubt
		                        	}
		                        	gui.cNo.setText(token2); // update the GUI  //Doubt
		                        	// update the variables
		                        	gui.coordinatorProcess = Integer.parseInt(token2);
		                            coordinatorProcess = Integer.parseInt(token2);
		                        	sendToNextProcess(token, processNumber+1); // send the message to next process
		                        	gui.isIdle = true;
		                        	gui.btnCrash.setEnabled(true); // Crash button is enabled
		                        	gui.btnRefresh.setEnabled(true); // Refresh button is enabled
		                        	if(gui.currentAliveProcess == processNumber - 1)
		                        	{
		                        		// initiate the alive message when the last alive message was stopped here
		                        		if (processNumber == coordinatorProcess){
			                        		nextAliveProcess(processNumber+1);
			                        	} else {
			                        		String tokenAlive = "ALIVE_NODE " + coordinatorProcess + " " + processNumber;
			                        		verifyAliveProcess(gui.coordinatorProcess, processNumber+1, processNumber, tokenAlive);
			                        	}
		                        	}
		                        }
		                    } catch(Exception ex) {
		                        ex.printStackTrace();
		                    }
		                    break;
		                case "ALIVE_NODE": 
		                	String tokenCNo = stringTokenizer.nextToken(); // coordinator number
		                	String tokenPNo = stringTokenizer.nextToken();  // process number
		                	if(gui.isIdle == true) {
		                		try {
		                			// if the ALIVE_NODE message is circled back to the initiator
			                        if(Integer.parseInt(tokenPNo) == processNumber) {
			                        	// check the status
			                        	if(stringTokenizer.hasMoreTokens()) {
				                            if(stringTokenizer.nextToken().equals("OK")){
				                            	// if the status is OK update the GUI
				                            	gui.jButton4.append("\n [" + tokenPNo + "] --> Coordinator Alive"); //Doubt
				                            	if (processNumber+1 == coordinatorProcess){
					                        		nextAliveProcess(processNumber+2);
					                        	} else {
					                        		nextAliveProcess(processNumber+1);
					                        	}
				                            }
			                        	} else {  // else initiate the elction process
			                        		gui.jButton4.append("\n Coordinator not responding... \n Initiating new election..."); //Doubt
			                        		gui.jButton4.append("\n Token Out: " + "ELECTION_MESSAGE " + processNumber); //Doubt
			            					String currentToken = "ELECTION_MESSAGE " + processNumber;
			            					gui.startElection(currentToken, processNumber+1);
			                        	}
			                        	
			                        }
			                        else if(Integer.parseInt(tokenCNo) == processNumber) { // if the alive message is received by the coordinator
			                            	gui.jButton4.append("\n [" + tokenPNo + "] --> Coordinator Validation Passed"); //Doubt
			                            	// append OK status and send it back to the initiator
			                            	token = token + " OK";
			                            	sendToNextProcess(token, processNumber+1);
			                        }
			                        else { // else forward it to the next processor
			                        	sendToNextProcess(token, processNumber+1);
			                        }
			                        
			                    } catch(Exception ex) {
			                        ex.printStackTrace();
			                    }
		                	}
		                    break;
		                case "EXIT": // if it is EXIT message //Modify it for Disconnect button
		                	// Initiate the election process since the next process was removed from the ring
		                	String token4 = stringTokenizer.nextToken();
		                	gui.isIdle = false;
		                	gui.jButton4.append("\n Process " + token4 + " is removed from the Ring, Initiating Election.. "); //Doubt
		                	String jButton4 = "ELECTION_MESSAGE " + processNumber;      //Doubt
		                	gui.jButton4.append("\n Token Out: " + electedToken); //Doubt
		                	sendToNextProcess(electedToken, processNumber+1);
		                    break;
		                case "ALIVE_NEIGHBOUR": // if it is ALIVE_NEIGHBOUR message
		                	// send the alive message from this processor since the previous one was a coordinator
		                	int targetNo = Integer.parseInt(stringTokenizer.nextToken());
		                	if(targetNo == processNumber) {
		                		if(targetNo == coordinatorProcess) {
		                			nextAliveProcess(1);
		                		} else {
			                		gui.currentAliveProcess = targetNo - 1;
			                		String tokenNextAlive = "ALIVE_NEIGHBOUR " + coordinatorProcess + " " + processNumber;
			                		verifyAliveProcess(gui.coordinatorProcess, processNumber+1, processNumber, tokenNextAlive);
		                		}
		                	}
		            }
		            
		     
	            
			} catch (IOException e) {
				
				System.out.println("Exception : " + e);
			} catch (NullPointerException e)
	        {
	        	
	        	System.out.println("Exception : " + e);
	        } catch (InterruptedException e) {
				// TODO Auto-generated catch block
	        	System.out.println("Exception : " + e);
			} finally {
	            try {
	            	readBuffer.close();
	            } catch (IOException ex) {
	            	System.out.println("Exception : " + ex);
	            }
	        }
		}
	
        }
        
       
    private void sendToNextProcess(String token, int processID) {
        if(processID>9) {
        	processID = processID - 9;
        }
        try {
            Socket socket = new Socket(host, defaultPort+processID);
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            out.println(token);
            out.flush();
            out.close();
            socket.close();
        } catch(Exception ex) {
        	sendToNextProcess(token, processID+1); // send it to next next if the next one was unavailable
        }
    
    
    }
        
    
    private void findCoordinatorProcess(int[] processes) {
        int newCoordinatorProcess = processes[0];
     
        for(int i = 1; i<processes.length; i++) {
            if(processes[i]>newCoordinatorProcess) {					
            	newCoordinatorProcess = processes[i];
            }
        }  
        
        // assign the variables with the new coordinator value
        gui.coordinatorProcess = newCoordinatorProcess;
        coordinatorProcess = newCoordinatorProcess;
        gui.cNo.setText(Integer.toString(newCoordinatorProcess));  //Doubt
        
    }

 
    private void sendCoordinatorMessage(int coordNbr, int processNbr, int electedNbr) {
    	int tempProcess;
    	if(coordNbr == processNbr) {
    		tempProcess = 1;
        } else {
        	tempProcess = processNbr + 1;
        }
    	// initiate the coordinator token and pass it to all the active processes
    	String token = "COORDINATOR_MESSAGE " + coordNbr + " " + electedNbr;
        try {
            Socket socket = new Socket(host, defaultPort+tempProcess);
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            out.println(token);
            out.flush();
            out.close();
            socket.close();
        } catch(Exception ex) {
        	sendCoordinatorMessage(coordNbr, tempProcess, electedNbr); // inform to next next if the next one was unavailable
        }
    }


    private void verifyAliveProcess(int coordinatorID, int processID, int electedID, String token) {
    	if(gui.isIdle == true) {
    		long time = 1500 * electedID;
    		try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		if(processID>5) {
            	processID = processID - 5;
            }
	        try {
	            Socket socket = new Socket(host, defaultPort+processID);
	            PrintWriter out = new PrintWriter(socket.getOutputStream());
	            out.println(token);
	            out.flush();
	            out.close();
	            socket.close();
	            gui.currentAliveProcess = electedID;
	        } catch(Exception ex) {
	        	verifyAliveProcess(coordinatorID, processID+1, electedID, token); // pass to next next if next was unavailable
	        }
    	}
    }
 
    public void nextAliveProcess(int nextProcess) {
    	if(nextProcess>9) {
    		nextProcess = nextProcess - 9;
        }
    	String token = "ALIVE_NODE " + nextProcess;
        try {
            Socket socket = new Socket(host, defaultPort+nextProcess);
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            out.println(token);
            out.flush();
            out.close();
            socket.close();
        } catch(Exception ex) {
        	// forward to next next if the next one was unavailable
        	if(nextProcess+1 == coordinatorProcess) {
        		nextAliveProcess(nextProcess+2);
        	} else {
        		nextAliveProcess(nextProcess+1);
        	}
        }
    }













    
}
