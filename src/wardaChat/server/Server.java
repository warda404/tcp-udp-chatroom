package wardaChat.server;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import wardaChat.dbtest;

public class Server implements Runnable{
	
	public static List<ServerClient> clients = new ArrayList<ServerClient>();
	private List<Integer> clientResponse = new ArrayList<Integer>();
	
	private int port;
	private static DatagramSocket socket;
	private Thread ServerRun, manage;
	private static Thread send;
	private Thread receive;
	private boolean running = false;
	
	/*
	 * 
	 */
	private ServerSocket serverSocket;
	private ServerSocket ftpServerSocket;
	private Socket TCPsocket;
	private Thread accept, accept2;
	/*
	 * 
	 */
	
	private final int MAX_ATTEMPTS = 5;
	
	public Server(int port) {
		this.port = port;
		// create socket 
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}
		
		/*
		 * 
		 */
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			ftpServerSocket = new ServerSocket(5717);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/*
		 * 
		 */
		
		ServerRun = new Thread(this, "Server");
		ServerRun.start();
	}

	public void run() {
		running = true;
		System.out.println("Server started on port " + port);
		manageClients();
		receive();
		
		/*
		 * 
		 */
		acceptClients();
		acceptClients2();
		/*
		 * 
		 */
		
		Scanner scanner = new Scanner(System.in);
		while(running) {
			String text = scanner.nextLine();
			if(!text.startsWith("/")) {
				broadcast("/m/***SERVER***: "+text);
				continue;
			}
			text = text.substring(1);
			if(text.equals("clients")) {
				System.out.println("Clients:");
				System.out.println("=====================================================================================");
				for (int i = 0; i<clients.size();i++) {
					ServerClient c = clients.get(i);
					System.out.println(c.name + " ("+c.getID()+") @"+c.address+":"+c.port+ " TCP " +c.csocket);
				}
				System.out.println("=====================================================================================");
			}
			else if(text.startsWith("kick") && text.length() > 4) {
				String strid = text.split(" ")[1];
				int id = -1;
				boolean number = true;
				try {
					id = Integer.parseInt(strid);
				}
				catch (NumberFormatException nfe) { 
					number = false;
				}
				
				if(number) {
					boolean exists = false;
					for(int i = 0; i < clients.size(); i++) 
					{
						if(clients.get(i).getID() == id) 
						{
							exists = true;
							String closeChat = "/CLOSE/";
							send(closeChat.getBytes(), clients.get(i).address, clients.get(i).port);
							disconnect(id, true);
							break;
						}
					} 
					if(exists) {}
					else
					{
						System.out.println("Client of this ID " + id +" doesn't exist!");
					}
				}
				else
				{
					System.out.println("Enter the ID of the client to kick him/her");
				}
				
			}
			else if(text.equals("quit"))
			{
				quit();
			}
		}
	}

	// MANAGE CLIENTS THREAD
	private void manageClients() {
		manage = new Thread("Manage") {
			public void run() {
				while(running) {
					// ping all clients
					for(int i = 0; i< clients.size(); i++)	
					{
						ServerClient client = clients.get(i);
						send("/i/server".getBytes(), client.address, client.port);
					}
					
					//send status to all clients
					sendStatus();
					
					// sleep for 2 seconds 
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} 
					// check for replies
					for(int i = 0; i< clients.size(); i++)	
					{
						ServerClient c = clients.get(i);
						if(!clientResponse.contains(c.getID())) 
						{
							if(c.attempt >= MAX_ATTEMPTS)
							{
								disconnect(c.getID(), false);
							}
							else
							{
								c.attempt++;
							}
						}
						else 
						{
							clientResponse.remove(new Integer(c.getID()));
							c.attempt = 0;
						}
					}
				}
			}
		};
		manage.start();
	}
	
	// STATUS
	private void sendStatus() 
	{
		if(clients.size() <= 0) return;
		String onlineUsers = "/o/"+clients.size();
		
		for(int i = 0; i < clients.size(); i++)
		{
			ServerClient client = clients.get(i);
			send(onlineUsers.getBytes(), client.address, client.port);
		}
		
	}
	
	// RECIEVE MESSAGES THREAD
	private void receive() {
		receive = new Thread("Receive") {
			public void run() {
				while(running) {
					
					byte[] data = new byte[1024];
					DatagramPacket packet = new DatagramPacket(data, data.length);
					try {
						socket.receive(packet);
					} catch (IOException e) {
						e.printStackTrace();
					}
					process(packet);
				}
			}
		};
		receive.start();
	}
	
	// BROADCAST
	public static void broadcast(String message) {
		
		// store the message in message log 
		try {
			dbtest.ip = "localhost";
			dbtest.logMessage(message.split("/m/")[1].trim());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// send to all clients (sends to the sending one too so its like a confirmation)
		for(int i = 0; i < clients.size(); i++)
		{
			ServerClient client = clients.get(i);
			send(message.getBytes(), client.address, client.port);
		}
	}
	
	// SEND THREAD
	public static void send(final byte[] data, final InetAddress address, final int port) {
		send = new Thread("Send") {
			public void run() {
				DatagramPacket packet = new DatagramPacket(data,data.length, address, port);
				try {
					socket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		
		send.start();
	}
	
	// PROCESS INCOMING DATA
	private void process(DatagramPacket packet) 
	{
		String string = new String(packet.getData());
		if(string.startsWith("/c/")) 
		{
			// generate ID
			int clientID = UniqueIdentifier.getIdentifier();
			
			// add the client to its list of clients
			clients.add(new ServerClient(string.substring(3,string.length()), packet.getAddress(), packet.getPort(), clientID, acceptClients()));
		
			System.out.println(string.substring(3, string.length())+" ("+clientID+") @"+packet.getAddress()+":"+packet.getPort()+" joined the chat!");
			
			// confirm connection so that client also knows that connection was successful
			String thisIsYourID =   "/c/" + clientID;
			send(thisIsYourID.getBytes(), packet.getAddress(), packet.getPort());
			
		}
		else if(string.startsWith("/d/")) 
		{
			int IDtobeDisconnected = Integer.parseInt(string.split("/d/")[1].trim());
			disconnect(IDtobeDisconnected, true);
		}
		else if (string.startsWith("/m/"))
		{
			System.out.println(string);
			broadcast(string);
		}
		else if (string.startsWith("/i/"))
		{
			clientResponse.add(Integer.parseInt(string.split("/i/")[1].trim()));
		}
		else
		{
			System.out.println(string);
			
		}
	} // end process method
	
	// SERVER QUIT (close UDP + TCP sockets )
	private void quit() {
		while(clients.size()!= 0)
		{
			String closeChat = "/SERVERQUIT/";
			send(closeChat.getBytes(), clients.get(0).address, clients.get(0).port);
			disconnect(clients.get(0).getID(), true);
		}
		running = false;
		socket.close();
		/*
		 * 
		 */
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			ftpServerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		 * 
		 */
	}
	
	// DISCONNECT A CLIENT 
	private void disconnect(int id, boolean status) {
		ServerClient c = null;
		for(int i = 0; i < clients.size(); i++)
		{
			if (clients.get(i).getID() == id)
			{
				c = clients.get(i);
				clients.remove(i);
				break;
			};
		}
		String message = "";
		// normal disconnect
		if(status) { 
			message = "User " + c.name + " (" + c.getID()  + ") @" + c.address.toString() +  ":" + c.port + " disconnected";
			System.out.println(message);
			
			// tell all clients about the user that left
			String m = "/m/"+c.name.trim()+" left!";
			for(int i = 0; i < clients.size(); i++)
			{
				ServerClient client = clients.get(i);
				send(m.getBytes(), client.address, client.port);
			}
		}
		// abnormal disconnect
		else {
			message = "User " + c.name + " (" + c.getID()  + ") @" + c.address.toString() +  ":" + c.port + " timed out";
			System.out.println(message);
			
			// tell all clients about the user that left
			String m = "/m/"+c.name.trim()+" left!";
			for(int i = 0; i < clients.size(); i++)
			{
				ServerClient client = clients.get(i);
				send(m.getBytes(), client.address, client.port);
			}
		}
	}
	
	// ACCEPT CLIENTS THREAD
	/*
	 * 
	 */
	// this one is for storing sockets of all clients that are connected
	private Socket acceptClients() {
		accept = new Thread("Accept") {
			public void run() {
				while(running) {
					try {
						TCPsocket = serverSocket.accept();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};
		accept.start();
		return TCPsocket;
	}
	
	// this one is for creating socket just for sending File (fwd files to all the sockets returned by above accept method
	private void acceptClients2() {
		accept2 = new Thread("Accept") {
			public void run() {
				while(running) {
					try {
						TCPsocket = ftpServerSocket.accept();
						System.out.println("Accepted FTP socket connecton");
						Thread t = new Thread(new ClientConnection(TCPsocket));
						t.start();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};
		accept2.start();
	}
	
	
	
	/*
	 * 
	 */
	
}
