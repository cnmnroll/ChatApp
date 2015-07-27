package sdf;

import java.text.SimpleDateFormat;
import java.time.format.*;
import java.util.*;
import java.util.Date;
import java.io.*;
import java.net.*;
import java.sql.*;

public class ChatMThread extends Thread{
	Socket socket = null;
	private Connection statment = null;
	PrintWriter out;
	BufferedReader in;
	static List<ChatMThread> menber;
	static List<String> loginUser;
	HashMap<String, String> userTable = new HashMap<String, String>();
	String userName = null;
	long loginTime;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	//DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
	private int chatCount = 0;
	public ChatMThread(Socket s, Connection in){
		super("KaiwaMThread");
		//���O�C�����
		userTable.put("user1", "1"); userTable.put("user2", "2"); userTable.put("user3", "3");
		statment = in;
		socket = s;
		if(menber == null) menber = new ArrayList<ChatMThread>();
		if(loginUser == null) loginUser = new ArrayList<String>();
		menber.add(this);
		try{
			out = new PrintWriter(socket.getOutputStream(), true);
			this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch(IOException e){
			System.out.println("aaaaaaaa");
		}
	}

	public void close(){
		try {
			loginUser.remove(this.userName);
			menber.remove(this);
			
			out.close();
			in.close();
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("close position eroor");
		} 
	}

	public void run(){
		// TODO Auto-generated method stub
		PreparedStatement ps = null; String fromUser;
		try{
			loginTime = new Date().getTime();
			String userInfoLine = in.readLine();
			System.out.println(userInfoLine);
			String[] userInfo = userInfoLine.split(" ");

			if(userTable.containsKey(userInfo[1]) && userTable.get(userInfo[1]).equals(userInfo[3])){
				if(((ArrayList)loginUser).indexOf(userInfo[1]) != -1){
					out.println("101 multiple login");
				} else {
					userName = userInfo[1]; loginUser.add(userName);
					out.println("0 login succeed");
					System.out.println("0 login succeed");
					out.println("���̃T�[�o��NE26-0037K�����@���쐬�������̂ł��B");
					String sql = "INSERT INTO chat_table VALUES (?, ?, ?, ?);";
					try{
						sendMessegeToUser();
						sendMessegeToAllUser("login");
						updateDB("login", sql); //DB���O�C���L�^
						while((fromUser = in.readLine()) != null){
							System.out.println(fromUser);
							updateDB(fromUser, sql);
							sendMessegeToAllUser(fromUser);
							chatCount++;
						}

					} catch(IOException e){
						//System.out.println("run���\�b�h���s����O:" + e);

					} 
					updateDB("logout", sql);
					sendMessegeToAllUser("logout");
					System.out.println(userName +"���񂪏I�����܂����B");
					close();
				}
			} else {
				out.println("100 login invaild");
				System.out.println("100 login invaild");
			}

			close();
		}catch (Exception e) {
			System.out.println("sql");
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
//			close();
		}
	}

	public void sendMessegeToUser(){
		String name = "";
		for(String n : loginUser){
			if(n.equals(this.userName)) continue;
			name += " " + n;
		}
		//���O�C�����[�U���M
		if(name.length() > 0){
			this.out.println("curuser " + (loginUser.size()-1) + name);
		}
		try{
			String sql = "select count(*) as count from chat_table where status  = 1";
			ResultSet rs = serchDB(sql);
			int count = rs.getInt("count");
			int countOffset = serchDB("select count(*) as count from (select id from chat_table"
					+ " where status = 1 limit 10);").getInt("count") - 1; System.out.println(countOffset);
					sql = "select id, username, chat from (select id, username, chat from chat_table"
							+ " where status = 1 order by id desc limit 10) order by id asc;";
					rs = serchDB(sql);

					//����10�̔���
					while(rs.next()){
						this.out.println("oldchat " + rs.getString("username") + " "+ (count - countOffset--)
								+ " " + sdf.format(rs.getLong("id")) + " " + rs.getString("chat"));
					}
				rs.close();
		}catch(SQLException e){
			System.out.println("�ߋ��`���b�g�擾�G���[");
			System.out.println(e.getMessage());
		}
	}

	public void sendMessegeToAllUser(String fromUser){
		System.out.println("sendMessegeToAllUser");
		Iterator<ChatMThread> it = menber.iterator();
		String userChat[] = fromUser.split(" ");
		while(it.hasNext()){
			ChatMThread client = it.next();
			if(userChat[0].equals("login")){
				if(client == this) continue; //�����ȊO�ɑ���
				try{ //���O�C���ʒm
					String sql = "select count(*) as count from chat_table where status = 0"
							+ " and username = '" +userName + "' order by id desc limit 1;";
					ResultSet rs = serchDB(sql);
					int count = rs.getInt("count");
					sql = "select id from chat_table where status = 0 and username = '" 
							+userName + "' and chat = 'login' order by id desc limit 1;";
					rs = serchDB(sql);
					if(count > 0){
						client.out.println("login user " + userName + " " 
								+ sdf.format(new Date(rs.getLong("id"))));
					} else {
						System.out.println("�����O�C��");
						client.out.println("login user " + userName+ " first login");
					}
					rs.close();
				}catch(SQLException e){
					System.out.println(e.getMessage());
				}
			} else if(userChat[0].equals("logout")){ //���O�A�E�g�ʒm
				if(client == this) continue; //�����ȊO�ɑ���
				client.out.println("logout user " + userName + " "+ sdf.format(new Date(loginTime))
						+ " " + chatCount);
			} else { //�`���b�g�ʒm
				client.out.println("chat " + userName + " " + fromUser);
			}
		}
	}

	public void updateDB(String fromUser, String sql) throws SQLException{
		System.out.println("updateDB");
		long time = new Date().getTime(); 
		PreparedStatement ps = statment.prepareStatement(sql);
		ps.setLong(1, time); ps.setString(2, userName);
		String userInfo[] = fromUser.split(" ");
		if(fromUser.equals("")){
			System.out.println("�󕶎�");
			ps.setInt(3, 1); ps.setString(4, "");
		} else {
			System.out.println("��s");
			if(userInfo[0].equals("login")){
				ps.setInt(3, 0); ps.setString(4, "login");
			} else if(userInfo[0].equals("logout")){
				ps.setInt(3, 0); ps.setString(4, "logout");
			} else {
				ps.setInt(3, 1); ps.setString(4, fromUser);
			}
		}
		ps.executeUpdate();
	}

	private ResultSet serchDB(String sql) throws SQLException{
		System.out.println("serchDB");
		PreparedStatement ps = statment.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		return rs;
	}
}
