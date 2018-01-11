package wardaChat.server;



import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import wardaChat.FileEvent;

public class ClientConnection implements Runnable{

	public Socket csocket;
	private Thread getAndFwdFile;
	private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;
	private FileEvent fileEvent;
	
	
	public ClientConnection(Socket csocket) {
		this.csocket = csocket;
	}
	
	public void run() {
			getAndForwardFile();
	}
	
	private void getAndForwardFile() {
		getAndFwdFile = new Thread("Get and Forward File") {
			public void run() {
				
				// get inputStream of socket
				try {
					inputStream = new ObjectInputStream(csocket.getInputStream());
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				// read file event object from the inputStream - store into the fileEvent object
				try {
					fileEvent = (FileEvent) inputStream.readObject();
					} catch (ClassNotFoundException e2) {e2.printStackTrace();} catch (IOException e2) {e2.printStackTrace();}
				
				if (fileEvent.getStatus().equalsIgnoreCase("Error")) {
					System.out.println("Error occurred");
					return;
				}
				
				String senderName = fileEvent.getSenderName();
				
				// forward fileEvent object  to all users (get each ones socket outputstream and write fileEVnt obj to it)
					for(int i = 0; i < Server.clients.size(); i++)
					{	
						if(!Server.clients.get(i).name.trim().equals(senderName)) {
							try {
								outputStream = new ObjectOutputStream(Server.clients.get(i).csocket.getOutputStream());
							} catch (IOException e) {
								e.printStackTrace();
							}
							try {
								outputStream.writeObject(fileEvent);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
					
					for(int i = 0; i < Server.clients.size(); i++) 
					{
						if(!Server.clients.get(i).name.trim().equals(senderName)) {
							Server.send(("/m/~~~SERVER~~~: "+senderName+ " sent a file " + fileEvent.getFilename()).getBytes(),
									Server.clients.get(i).address, Server.clients.get(i).port);
						}
						else
						{
							Server.send(("/m/~~~SERVER~~~: Your file "+ fileEvent.getFilename() + " was sent").getBytes(),
									Server.clients.get(i).address, Server.clients.get(i).port);
						}
					}
					
					// close the TCP socket used for file sending
					try {
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println("FTP socket for "+ senderName +" closed");
				
			}
		};
		getAndFwdFile.start();
	}
	

}
