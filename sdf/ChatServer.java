package sdf;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.HashMap;

public class ChatServer {
	public static void main(String[] args) throws IOException, SQLException {
		ServerSocket serverS = null;
		boolean end = true;
		try{
			System.out.println(InetAddress.getLocalHost().getHostAddress());
			serverS = new ServerSocket(42000);
		} catch(IOException e){
			System.out.println("�|�[�g�ԍ��ɃA�N�Z�X�ł��܂���B");
			System.exit(1);
		}
		
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:q6.sqlite3");
			c.setAutoCommit(true);
			System.out.println("Opened database successfully");
			while(end){
				new ChatMThread(serverS.accept(), c).start();
			}
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.out.println("aaaaaaaaaaaa");
			System.exit(1);
		}
		serverS.close(); c.close();
	}
}
