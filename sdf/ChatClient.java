package sdf;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.Date;

import javax.swing.*;

public class ChatClient extends JFrame implements KeyListener{
	//ChatPanel chatPanel;
	Socket serverSocket = null;
	ChatActionThread cat = null;
	static PrintWriter out;
	static BufferedReader in;
	JTextArea outputChat;
	TextField inputChat, inputPort;
	JScrollPane chatScroll;
	String userName;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	JButton submit;
	public ChatClient(){
		addWindowListener(new myListener());
		JMenuBar menubar = new JMenuBar(); JMenu menu = new JMenu("�I��"); JMenuItem exit = new JMenuItem("�I��");
		submit = new JButton("���M");
		menu.add(exit); menubar.add(menu); this.setJMenuBar(menubar);
		JPanel main = new JPanel(), connection = new JPanel(), userNamePanel = new JPanel(); JPanel submitPanel = new JPanel();
		outputChat = new JTextArea(); chatScroll = new JScrollPane(outputChat);
		inputChat = new TextField(); inputPort = new TextField();

		exit.addActionListener(e ->{
			close();
		});

		outputChat.setLineWrap(true); //�܂�Ԃ�
		outputChat.setEditable(false); //�e�L�X�g�G���A���͕s��
		main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
		chatScroll.setPreferredSize(new Dimension(500, 300));
		connection.setLayout(new BorderLayout());
		inputChat.setPreferredSize(new Dimension(400, 20));
		submit.setEnabled(false);
		inputChat.addKeyListener(this);
		try{
			submit.addActionListener(e -> { //�`���b�g�̑��M
				cat.sendChat(inputChat.getText()); //setting��ʂŐڑ����ꂽ�ꍇ�ɃC���X�^���X�����������B
				JScrollBar scrollBar = chatScroll.getVerticalScrollBar();
				scrollBar.setValue(scrollBar.getMaximum()); //�����X�N���[��
				System.out.println("���M");
				submit.setEnabled(false);
			});
		} catch(java.lang.NullPointerException e1){ 
			JOptionPane.showMessageDialog(this, "�`���b�g���M�G���[");
		}
		submitPanel.add(inputChat); submitPanel.add(submit);
		main.add(connection); main.add(chatScroll); main.add(userNamePanel); main.add(submitPanel);
		add(main);
	}
	
	 public class myListener extends WindowAdapter{
		 public void windowClosing(WindowEvent e) {
			 close();
		 }
	 }
	
	public static void main(String args[]){
		ChatClient cc = new ChatClient();
		
		cc.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		cc.userName = args[2];
		try{
			cc.serverSocket = (new ChatConnection(args[0], Integer.parseInt(args[1].toString()))).getSocket(); //�A�E�^�[�N���X��serverSocket���Q��
			cc.out = new PrintWriter(cc.serverSocket.getOutputStream(), true);
			cc.in = new BufferedReader(new InputStreamReader(cc.serverSocket.getInputStream()));
			cc.out.println("user " + args[2] + " pass " + args[3]);

			String judge = cc.in.readLine();

			if(judge.equals("0 login succeed")){
				cc.cat = cc.new ChatActionThread(cc.serverSocket, args[2]); //������p���ăT�[�o�ɐڑ������������`���b�g���J�n
				cc.cat.start();
			} else if(judge.equals("101 multiple login")){
				System.out.println("���[�U" + args[2] + "�͂��łɃ��O�C�����Ă��܂��B");
				
				System.exit(0);
			} else {
				System.out.println("���[�U" + args[2] + "�̃p�X���[�h�Ԉ���Ă��܂��B");
				System.exit(0);
			}
		} catch(IOException | ArrayIndexOutOfBoundsException e){
			System.out.println("�ڑ��ł��܂���B\nIP�A�h���X�A�|�[�g�ԍ��A���O�A�p�X���[�h�̏��Ő��������͂��Ă��������B");
			System.exit(0);
		}
		cc.pack(); cc.setVisible(true);
	}
	
	
	
	class ChatActionThread extends Thread{ //�`���b�g�̑��M�y�ю�M
		Socket chatServer = null;
		String userName = "����������";

		ChatActionThread(Socket s, String name){ //���[�_�ݒ�
			chatServer = s; userName = name;
		}
		public void run(){ //�`���b�g��M
			String fromServer; 
			try {
				System.out.println("start");
				String spr[];
				while((fromServer = in.readLine()) != null){
					System.out.println(fromServer);
					spr = fromServer.split(" ");
					if(spr[0].equals("login")){
						outputChat.append(spr[2] + "���񂪃��O�C�����܂����B" + "�O��̃��O�C����" + spr[3] + " " + spr[4] + "�ł��B\n");
					} else if(spr[0].equals("chat")){
						outputChat.append(spr[1] + ">"); //�A�E�^�[�N���X��outputChat���Q��
						for(int i = 2; i < spr.length; i++){
							outputChat.append(" " + spr[i]);
						}
						outputChat.append("\n");
						System.out.println("����");
					} else if(spr[0].equals("logout")){
						outputChat.append(spr[2] + "���񂪃��O�A�E�g���܂����B"  + spr[3] + " " + spr[4] + "�Ƀ��O�C�����Ĉȗ��A" + spr[5] + "�񔭌����܂����B\n");
					} else if(spr[0].equals("oldchat")) {
						outputChat.append(spr[2] + "�Ԗ� " + spr[3] + ":"+ spr[4]);
						for(int i = 5; i < spr.length; i++){
							outputChat.append(" " + spr[i]);
						}
						outputChat.append("\n");
					} else if(spr[0].equals("curuser")) {
						outputChat.append("���݃��O�C�����Ă��郆�[�U��" + spr[1] + "�l�ł��B\n");
						for(int i = 2; i < spr.length; i++){
							outputChat.append(spr[i] + " ");
						}
						outputChat.append("\n");
					} else {
						outputChat.append(fromServer +"\n");
					}
					JScrollBar scrollBar = chatScroll.getVerticalScrollBar();
					scrollBar.setValue(scrollBar.getMaximum()); //�����X�N���[��
				}
				System.out.println("�I��");
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				//System.out.println("�`���b�g��M�G���[");
				//e1.printStackTrace();
			}
		}

		public void sendChat(String text){ //�T�[�o�Ƀ`���b�g����
			if(text.equals("")){
				System.out.println("��������͂��Ă�������");
			} else {
				out.println(text);
				inputChat.setText(""); //������
				System.out.println(text + " ���M����");
			}
		}

		public void setUserName(String name){ //���[�U�l�[���̐ݒ�
			this.userName = name;
		}
	}

	public void close(){
		try {
			out.close();
			in.close();
			serverSocket.close();
			System.exit(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("close position eroor");
		} 
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		if(spaceCheck()){
			submit.setEnabled(true);
		}
	}
	private boolean spaceCheck(){
		for(int i = 0; i < inputChat.getText().length(); i++){
			if(inputChat.getText().charAt(i) != ' '){
				return true;
			}
		}
		submit.setEnabled(false);
		return false;
	}
}


class ChatConnection { //�T�[�o�Ƃ̐ڑ��N���X
	Socket chatServer = null;
	public ChatConnection(String IpAddress, int port) throws IOException{
		System.out.println("�ڑ���");
		try{ //�C���X�^���X�������ɐڑ�
			chatServer = new Socket(IpAddress, port);
			chatServer.getInputStream();
			System.out.println("�ڑ�����");
		} catch(UnknownHostException e){
			System.out.println("�z�X�g�ɐڑ��ł��܂���B");
			System.exit(0);
		}
	}
	public Socket getSocket(){ //�T�[�o�Ƃ̃\�P�b�g��Ԃ�
		return chatServer;
	}
}


