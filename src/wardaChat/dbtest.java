package wardaChat;

import javax.swing.JOptionPane;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.util.Date;
import java.text.SimpleDateFormat;

public class dbtest {

	public static String ip;
	
	public static void main(String[] args) throws Exception {
		getConnection();

	}
	
	
	public static int searchDB(String name, String password) throws Exception
	{
		try {
			Connection con = getConnection();
			PreparedStatement getCount = con.prepareStatement("SELECT COUNT(username) as 'present' FROM userinfo WHERE username='"+name+"' and password = '"+password+"'");
			ResultSet result= getCount.executeQuery();
			int u=0;;
			while(result.next())
			{
				u = result.getInt("present");
			}
			return u;
		}
		catch(Exception e) {
			System.out.println(e);
			return 0;
		}
		finally {
			System.out.println("Search user query executed...");
		}
	}
	
	public static void post(String name, String password) throws Exception {
		try {
			SignUp.setSuccess(true);
			Connection con = getConnection();
			PreparedStatement posted = con.prepareStatement("INSERT INTO userInfo (username, password) VALUES('"+name+"', '"+password+"')");
			posted.executeUpdate();
		}
		catch(Exception e)
		{
			System.out.println(e);
			JOptionPane.showMessageDialog(null, "USERNAME ALREADY EXISTS. PICK ANOTHER NAME!", "Sign up error", JOptionPane.WARNING_MESSAGE);
			System.out.println("USERNAME ALREADY EXISTS. PICK ANOTHER NAME!");
			//System.exit(1);
			SignUp.setSuccess(false);
		}
		finally
		{
			System.out.println("Insert Username and Password completed");
		}
	}
	
	
	public static void getLog() throws Exception {
		try {
			Connection con = getConnection();
			PreparedStatement selectMsgs = con.prepareStatement("SELECT * FROM message_log");
			ResultSet result= selectMsgs.executeQuery();
			
			//ArrayList<String> messages = new ArrayList<String>();
			ClientWindow.console1("****************** MESSAGE LOG ***************************");
			while(result.next())
			{
				ClientWindow.console1(result.getString("msgTime") + " "+ result.getString("msg"));
			}
			ClientWindow.console1("**************************************************************");
		}
		catch(Exception e) {
			System.out.println(e);
		}
	}
	
	public static void logMessage(String message) throws Exception {
			//getting current date and time using Date class
	       DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
	       Date dateobj = new Date();
	       String message_time = df.format(dateobj);
	       
	       try {
	    	   Connection con = getConnection();
	    	   PreparedStatement posted = con.prepareStatement("INSERT INTO message_log (msgTime, msg) VALUES('"+message_time+"', '"+message+"')");
	    	   posted.executeUpdate();
	       }
	       catch(Exception e)
	       {
	    	   System.out.println(e);
	       }
	       finally
			{
				System.out.println("Message logged");
			}
	}
	
	public static Connection getConnection() throws Exception{
		  try	{
		   String driver = "com.mysql.jdbc.Driver";
		   String url = "jdbc:mysql://" + ip +  ":3306/users";
		   System.out.println(ip);
		   //String url = "jdbc:mysql://localhost:3306/users";
		   String username = ""; // not provided for security reasons here XD 
		   String password = ""; // same as above
		   Class.forName(driver);
		   
		   Connection conn = DriverManager.getConnection(url,username,password);
		   System.out.println("Connected");
		   return conn;
		  } catch(Exception e)	{
			  System.out.println(e);
		  }
		  
		  return null;
		 }

}
