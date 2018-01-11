// stores the information about a client (name, address, port, ID, etc)
package wardaChat.server;

import java.net.InetAddress;
import java.net.Socket;

public class ServerClient {
	
	public String name;
	public InetAddress address;
	public int port;
	private final int ID;
	public Socket csocket;
	public int attempt = 0;
	
	public ServerClient(String name, InetAddress address, int port, final int ID, Socket s) {
		this.name = name;
		this.address = address;
		this.port = port;
		this.ID = ID;
		this.csocket = s;
	}
	
	public int getID() {
		return ID;
	}
}
