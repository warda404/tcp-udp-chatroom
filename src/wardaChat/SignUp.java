package wardaChat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;

public class SignUp extends JFrame {
	
	public static boolean success = true;
	
	public static void setSuccess(boolean x) {
		success = x;
	}

private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private JTextField txtName;
	private JPasswordField txtPassword;
	private JLabel lblPasswrod;
	private JTextField txtAddress;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SignUp frame = new SignUp();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public SignUp() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		setResizable(false);
		setTitle("Sign Up");
		setBackground(new Color(245, 245, 245));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(300, 350);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBackground(Color.DARK_GRAY);
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
		
		txtPassword = new JPasswordField();
		txtPassword.setBounds(50, 102, 200, 50);
		contentPane.add(txtPassword);
		txtPassword.setColumns(10);
		
		lblPasswrod = new JLabel("Password");
		lblPasswrod.setForeground(new Color(255, 255, 255));
		lblPasswrod.setBounds(113, 85, 73, 16);
		contentPane.add(lblPasswrod);
		
		// set action listener to SignUP button click action
		JButton btnSignUp = new JButton("Sign Up");
		btnSignUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String name = txtName.getText();
				String password = txtPassword.getText();
				
				boolean invalid = false;
				if(name.equals("") || password.equals(""))
				{
					JOptionPane.showMessageDialog(null, "Username and/or password cannot be blank", "Sign up error", JOptionPane.WARNING_MESSAGE);
					invalid = true;
				}
				else if(password.length() < 8)
				{
					JOptionPane.showMessageDialog(null, "Password must be atleast 8 characters", "Sign up error", JOptionPane.WARNING_MESSAGE);
					invalid = true;
				}
				if(!invalid)
				{
					store(name, password);
				
					// STORE IN MY SQL successful
					if(success)
					{
						dispose();
						new Login();
					}
				}
			}
		});
		btnSignUp.setBounds(90, 176, 117, 29);
		contentPane.add(btnSignUp);
		
		JButton btnAlreadyHaveAn = new JButton("Already have an account?");
		btnAlreadyHaveAn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
				new Login();
			}
		});
		btnAlreadyHaveAn.setBounds(55, 217, 189, 29);
		contentPane.add(btnAlreadyHaveAn);
		
		txtAddress = new JTextField();
		txtAddress.setBounds(77, 270, 130, 26);
		contentPane.add(txtAddress);
		txtAddress.setColumns(10);
	}
	
	private void store(String name, String password) {
		String address = txtAddress.getText();
		dbtest.ip = address;
		try {
			dbtest.post(name, password);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	
}
