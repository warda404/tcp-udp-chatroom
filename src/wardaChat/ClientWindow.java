package wardaChat;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
import javax.swing.JFileChooser;
import javax.swing.JLabel;

public class ClientWindow extends JFrame implements Runnable{
	
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtMessage;
	private DefaultCaret caret;
	private JTextField txtShowonline;
	private static JTextPane textPane;
	
	private Thread run, listen;
	private Client client;
	private boolean running  = false;
	
	
	// CONSTRUCTOR
	public ClientWindow(String name, String address, int port) {
		setResizable(false);
		setTitle(name); 
		client = new Client(name, address, port);
		boolean connect = client.openConnection(address); // create UDP socket
		client.createTCPSocket();
		
		if(!connect) {
			System.err.println("Connection failed!");
			console("Connection failed!");
		}
		
		CreateWindow();
		console("Trying to connect to Server " + address + ":" + port + ", User: " + name);
		// SEND CONNECTION PACKET TO SERVER 
		String connection = "/c/" + name;
		client.send(connection.getBytes());

		running = true;
		run = new Thread(this, "Running");
		run.start();
	}
	
	private void CreateWindow() {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(624,550);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		
		Random r = new Random();
		int Low = 0;
		int High = 256;
		int RED = r.nextInt(High-Low) + Low;
		
		Random s = new Random();
		Low = 0;
		High = 256;
		int GREEN = s.nextInt(High-Low) + Low;
		
		Random t = new Random();
		Low = 0;
		High = 256;
		int BLUE = t.nextInt(High-Low) + Low;
		
		contentPane.setBackground(new Color(RED, GREEN, BLUE));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		txtMessage = new JTextField();
		txtMessage.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					if(txtMessage.getText().startsWith("/m/"))
					{
						txtMessage.setText("");
						return;
					}
					try {
						sendd(txtMessage.getText());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		txtMessage.setBounds(0, 490, 429, 26);
		contentPane.add(txtMessage);
		txtMessage.setColumns(10);
		
		// SEND MESSAGE ENTERED IN TEXT BOX
		JButton btnSend = new JButton("Send");
		btnSend.setBounds(429, 490, 75, 29);
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(txtMessage.getText().startsWith("/m/"))
				{
					txtMessage.setText("");
					return;
				}
				sendd(txtMessage.getText());
			}
		});
		contentPane.add(btnSend);
		
		txtShowonline = new JTextField();
		txtShowonline.setEditable(false);
		txtShowonline.setBounds(567, 8, 46, 26);
		contentPane.add(txtShowonline);
		txtShowonline.setColumns(10);
		
		// RETRIEVE THE MESSAGES LOG WHEN CLIENT ASKS FOR IT
		JButton btnMessageLog = new JButton("Message Log");
		btnMessageLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					dbtest.getLog();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		btnMessageLog.setBounds(507, 36, 117, 29);
		contentPane.add(btnMessageLog);
		
		// GUI OF THE CHAT AREA
		textPane = new JTextPane();
		StyledDocument doc = (StyledDocument) textPane.getDocument();
	    Style style = doc.addStyle("StyleName", null);
	    try {
			doc.insertString(doc.getLength(), "Welcome! :)", style);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		textPane.setEditable(false);
		JScrollPane scroll2 = new JScrollPane(textPane);
		caret = (DefaultCaret) textPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		scroll2.setBounds(6, 8, 500, 475);
		contentPane.add(scroll2);
		
		// Upload FILE BUTTON (opens Jfilechooser) 
		JButton btnChooseFile = new JButton("Upload");
		btnChooseFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				JFileChooser chooser = new JFileChooser();
		        int returnName = chooser.showOpenDialog(null);
		        
		        if (returnName == JFileChooser.APPROVE_OPTION) {
		            File f = chooser.getSelectedFile();
		            if (f != null) { // Make sure the user didn't choose a directory.
		                //client.sendFile(f.getAbsolutePath());
		            	client.createTCPSocketAndSendFile(f.getAbsolutePath());
		            }
		        }
		        
			}
		});
		btnChooseFile.setBounds(507, 490, 117, 29);
		contentPane.add(btnChooseFile);
		
		JLabel lblOnline = new JLabel("Online:");
		lblOnline.setForeground(Color.WHITE);
		lblOnline.setBounds(518, 13, 61, 16);
		contentPane.add(lblOnline);
		
		JButton btnChangeDloadDirectory = new JButton("Dload location");
		btnChangeDloadDirectory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// get download directory from user
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		        int returnName = chooser.showOpenDialog(null);
		        
		        if (returnName == JFileChooser.APPROVE_OPTION) {
		            File f = chooser.getSelectedFile();
		                client.downloadDirectory = f.getAbsolutePath() + "/";
		                System.out.println(client.downloadDirectory);
		        }
			}
		});
		btnChangeDloadDirectory.setBounds(507, 454, 117, 29);
		contentPane.add(btnChangeDloadDirectory);
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		// WHEN CLIENT CLOSES THE WINDOW (NORMAL CLOSE BY CLICKING CROSS BUTTON)
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				String disconnect = "/d/" + client.getID(); 
				client.send(disconnect.getBytes()); // remove it from list of clients
				running = false;
				client.close(); // close the client socket
			}
		});
		
		try {
			dbtest.getLog();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		setVisible(true);
		txtMessage.requestFocusInWindow();
		
		
		// get download directory from user
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnName = chooser.showOpenDialog(null);
        
        if (returnName == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
                client.downloadDirectory = f.getAbsolutePath() + "/";
                System.out.println(client.downloadDirectory);
        }
	}

	public void run() {
		listen();
		client.downloadFile();
	}
	
	private  void sendd(String message){
		if(message.equals("")) return;
		message =  client.getName()  + ": " + message;
		message = "/m/"+message;
		client.send(message.getBytes());
		txtMessage.setText("");
	}

	// LISTEN for a confirmation from Server + this is the thread for client receieve anyway
	public void listen() {
		listen = new Thread("Listen") {
			public void run(){ 
				while(running) {
					String message = new String(client.receive().getData());
					if(message.startsWith("/c/")) {
						client.setID(Integer.parseInt(message.split("/c/")[1].trim()));
						console("Successfully connected to server! ID: " + client.getID());
					}
					else if(message.startsWith("/m/")) {
						String text = message.split("/m/")[1].trim();
						console(text);
					}
					else if(message.startsWith("/o/")) {
						String onlineUsers = message.split("/o/")[1].trim();
						txtShowonline.setText(onlineUsers);
					}
					else if (message.startsWith("/CLOSE/"))
					{
						JOptionPane.showMessageDialog(null, "You have been kicked", "Message from Server", JOptionPane.WARNING_MESSAGE);
						dispose(); System.exit(1);
					}
					else if (message.startsWith("/SERVERQUIT/"))
					{
						JOptionPane.showMessageDialog(null, "Server is offline, quitting!", "Message from Server", JOptionPane.WARNING_MESSAGE);
						dispose(); System.exit(1);
					}
					else if(message.startsWith("/i/")) { //ping from server
						client.send(("/i/"+client.getID()).getBytes());
					}
					
				}
			}
		};
		listen.start();
	}

	public static void console(String message){
		DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
	    Date dateobj = new Date();
	    String message_time = df.format(dateobj);
		textPane.setText(textPane.getText() + "\n"+message_time + " "+message);
	}
	
	
	public static void console1(String message){
		textPane.setText(textPane.getText() + "\n"+message);
	}
}
