package wardaChat;


import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.Color;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Login extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private JTextField txtName;
	private JPasswordField txtPassWord;
	private JTextField txtAddress;
	private JLabel lblIpAddress;
	private JTextField txtPort;
	private JLabel lblPort;
	
	int port;

	
	public Login() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		setResizable(false);
		setTitle("Login");
		setBackground(new Color(245, 245, 245));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(300, 450);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(102, 0, 153));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		txtName = new JTextField();
		txtName.setBounds(50, 23, 200, 50);
		contentPane.add(txtName);
		txtName.setColumns(10);
		
		JLabel lblName = new JLabel("Name");
		lblName.setForeground(new Color(255, 255, 255));
		lblName.setBounds(129, 6, 41, 16);
		contentPane.add(lblName);
		
		txtAddress = new JTextField();
		txtAddress.setText("localhost");
		txtAddress.setBounds(50, 190, 200, 50);
		contentPane.add(txtAddress);
		txtAddress.setColumns(10);
		
		lblIpAddress = new JLabel("IP Address");
		lblIpAddress.setForeground(new Color(255, 255, 255));
		lblIpAddress.setBounds(114, 172, 73, 16);
		contentPane.add(lblIpAddress);
		
		txtPort = new JTextField();
		txtPort.setText("8192");
		txtPort.setBounds(50, 269, 200, 50);
		contentPane.add(txtPort);
		txtPort.setColumns(10);
		
		lblPort = new JLabel("Port");
		lblPort.setForeground(new Color(255, 255, 255));
		lblPort.setBounds(129, 252, 41, 16);
		contentPane.add(lblPort);
		
		// set action listener to Login button click action
		JButton btnLogin = new JButton("Login");
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String name = txtName.getText();
				String password = txtPassWord.getText();
				
				String address = txtAddress.getText();
				dbtest.ip = address;
				
				int present = 0;
				boolean correctPort = true;
				
				try {
					present = dbtest.searchDB(name,password);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
				try{
					port = Integer.parseInt(txtPort.getText());
				}
				catch(NumberFormatException nfe)
				{
					correctPort = false;
					System.out.println("Port number must be an integer");
					JOptionPane.showMessageDialog(null, "Port number must be an integer", "Log in error", JOptionPane.WARNING_MESSAGE);
					present = 0;
				}
				
				
				if(correctPort) {
				if (present == 1)
				{
					login(name, address, port);
				}
				else
				{
					JOptionPane.showMessageDialog(null, "Wrong username and/or password", "Log in error", JOptionPane.WARNING_MESSAGE);
				}
				}
			}
		});
		btnLogin.setBounds(91, 345, 117, 29);
		contentPane.add(btnLogin);
		
		txtPassWord = new JPasswordField();
		txtPassWord.setBounds(50, 115, 200, 26);
		contentPane.add(txtPassWord);
		txtPassWord.setColumns(10);
		
		JLabel lblPassword = new JLabel("Password");
		lblPassword.setForeground(Color.WHITE);
		lblPassword.setBounds(109, 97, 61, 16);
		contentPane.add(lblPassword);
		
		setVisible(true);
	}
	
	// log in here (result of login btn clicked.. opens a new client window)
	private void login(String name, String address, int port){
		//close the login window
		dispose();
		// open the client
		new ClientWindow(name, address, port);
	}
}
