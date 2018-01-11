package wardaChat;



import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client {

	private static final long serialVersionUID = 1L;
	
	// Networks variables and threads and stuff...
	private String name, address;
	private int port;
	private InetAddress ip;
	private DatagramSocket socket;
	private Thread send;
	
	/*
	 * 
	 */
	public Socket TCPsocket;
	public Socket FTPsocket;
	private Thread  sendFiles;
	
	public String downloadDirectory;
	
	private FileEvent fileEvent = null;
	private FileEvent fileEventd;
	private ObjectOutputStream outputStream = null;
	private ObjectInputStream inputStream = null;
	private File dstFile = null;
	private FileOutputStream fileOutputStream = null;
	
	/*
	 * 
	 */
	
	private int ID = -1;
	
	public Client(String name, String address, int port) {
		this.name = name;
		this.address =  address;
		dbtest.ip = address;
		this.port = port;
	}
	
	public String getName() {
		return name;
	}
	
	public String getAddress() {
		return address;
	}
	
	public int getPort() {
		return port;
	}
	
	public boolean openConnection(String address) {
		try {
			socket = new DatagramSocket();
			ip = InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (SocketException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public DatagramPacket receive() {
		byte[] data = new byte[1024]; // 1 KB of data is the max it can receive at a time
		DatagramPacket packet = new DatagramPacket(data, data.length); // empty packet to be filled with the rcvd data
																// the packet also has IP and PORT of the sender in its header
		try {
			socket.receive(packet); // this will busy wait until it receives something...
		} catch (IOException e) {
			e.printStackTrace();
		}
		return packet;
	}
	
	public void send(final byte[] data){
		send = new Thread("Send") {
			public void run() {
				DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
				try {
					socket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} // end run()
		};
		
		// start the send thread
		send.start();
	}
	
	public void close() {
		// lock access to the socket object by using synchronized
		new Thread() {
			public void run() {
				synchronized (socket) { 
					socket.close();
					try {
						TCPsocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}}.start();
	}

	public void setID(int ID) {
		this.ID = ID;
	}
	
	public int getID() {
		return ID;
	}
	
	/*
	 * 
	 */
	public void createTCPSocket() {
		try {
			TCPsocket = new Socket(address, port);
		} catch (IOException e) {
			System.err.println("Cannot connect to the server, try again later.");
			e.printStackTrace();
		}
	}
	
	public void createTCPSocketAndSendFile(String clientSourceFilePath) {
		try {
			FTPsocket = new Socket(address, 5717);
		} catch (IOException e) {
			System.err.println("Cannot connect to the server, try again later.");
			e.printStackTrace();
		}
		
		sendFile(FTPsocket, clientSourceFilePath, name);
		
	}
	
	
	public void downloadFile()
	{
		Thread dload = new Thread("DOWNLOAD FILE") {
			
			public void run() {
				while(true) {
				// get the Inputstream of socket
				try {
					inputStream = new ObjectInputStream(TCPsocket.getInputStream());
				} catch (IOException e) {
					e.printStackTrace();
				}
				// read fileEvent obj from the inputstream
				try {
					fileEventd = (FileEvent) inputStream.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				if (fileEventd.getStatus().equalsIgnoreCase("Error")){
					System.out.println("Error occurred");
					return;
				}
				
				String outputFile = downloadDirectory + fileEventd.getFilename();
				
				if (!new File(downloadDirectory).exists()) {
					new File(downloadDirectory).mkdirs();
				}
				
				dstFile = new File(outputFile);
				try {
					fileOutputStream = new FileOutputStream(dstFile);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				try {
					fileOutputStream.write(fileEventd.getFileData());
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					fileOutputStream.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			
				System.out.println("Output file : " + outputFile + " is successfully saved ");
			}//end while
			}//end run
		};
		dload.start();
	}
	
	public void sendFile(final Socket FTPSocket, final String clientSourceFilePath, final String senderName) {
		sendFiles = new Thread("Client Send File") {
			public void run() {
				
				// Get the output stream of TCP socket to write to it
				try {
					outputStream = new ObjectOutputStream(FTPSocket.getOutputStream());
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				// create a new File event object
				fileEvent = new FileEvent();
				
				// get the file name and directory
				String fileName = clientSourceFilePath.substring(clientSourceFilePath.lastIndexOf("/") + 1, clientSourceFilePath.length());
				
				fileEvent.setFilename(fileName);
				fileEvent.setSourceDirectory(clientSourceFilePath);
				fileEvent.setSenderName(senderName);
				
				File file = new File(clientSourceFilePath);
				
				if (file.isFile()) {
					try {
						DataInputStream diStream = new DataInputStream(new FileInputStream(file));
						long len = (int) file.length();
						byte[] fileBytes = new byte[(int) len];
						int read = 0;
						int numRead = 0;
						while (read < fileBytes.length && (numRead = diStream.read(fileBytes, read, fileBytes.length - read)) >= 0) {
							read = read + numRead;
						}
						fileEvent.setFileSize(len);
						fileEvent.setFileData(fileBytes);
						fileEvent.setStatus("Success");
					} catch (Exception e) {
						e.printStackTrace();
						fileEvent.setStatus("Error");
					}
				} else {
					System.out.println("path specified is not pointing to a file");
					fileEvent.setStatus("Error");
				}
			
				// Now writing the FileEvent object to socket
				try {
					outputStream.writeObject(fileEvent);
					System.out.println("File written to the clients socket on client side");
				} catch (IOException e) {
					e.printStackTrace();
				}
											
			}
		};
		sendFiles.start();
	}
	
	/*
	 * 
	 */

	}
