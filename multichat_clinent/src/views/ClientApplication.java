package views;

import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.google.gson.Gson;

import dto.request.RequestDto;

public class ClientApplication extends JFrame {

	
	private static final long serialVersionUID = -4753767777928836759L;
	
	private Gson gson;
	private Socket socket;
	
	private JPanel mainPanel;
	private CardLayout mainCard;
	
	private JTextField usernameField;
	
	private JTextField sendMessageField;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientApplication frame = new ClientApplication();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}

	
	
	public ClientApplication() {
		/*========<<  init >>========*/
		gson = new Gson();
		try {
			socket = new Socket("127.0.0.1", 9090);
			ClientRecive clientRecive = new ClientRecive(socket); //클라이언트 접속 됐을 때 ClientRecive도 같이 연결함.
			clientRecive.start();
			
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (ConnectException e1) {
			JOptionPane.showMessageDialog(this, "서버에 접속할 수 없습니다.", "접속오류", JOptionPane.ERROR_MESSAGE); //this를 null로 바꿔보기
			System.exit(0); //프로그램종료
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		
		
		/*========<< frame set >>========*/
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(600, 150, 480, 800);
		
		
		
		/*========<< panels >>========*/
		mainPanel = new JPanel();
		JPanel loginPanel = new JPanel();
		JPanel roomListPanel = new JPanel();
		JPanel roomPanel = new JPanel();
		
		
		
		/*========<< layout >>========*/
		mainCard = new CardLayout();
		
		mainPanel.setLayout(mainCard);
		loginPanel.setLayout(null);
		roomListPanel.setLayout(null);
		roomPanel.setLayout(null);
		
		
		
		/*========<< panel set >>========*/
		setContentPane(mainPanel);
		mainPanel.add(loginPanel, "loginPanel");
		mainPanel.add(roomListPanel, "roomListPanel");
		mainPanel.add(roomPanel, "roomPanel");
		
		
		
		/*========<< login panel >>========*/
		JButton enterButton = new JButton("접속하기");

		usernameField = new JTextField();
		usernameField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					enterButton.doClick(); //엔터 시 클릭해라! 라는 뜻 (클릭한것과 같으므로 mouseClicked를 복붙하지 않아도됨)
				}
			}
		});
		
		usernameField.setBounds(73, 394, 308, 49);
		loginPanel.add(usernameField);
		usernameField.setColumns(10);
		
		enterButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				RequestDto<String> usernameCheckReqDto = new RequestDto<String>("usernameCheck", usernameField.getText());
				sendRequest(usernameCheckReqDto);
			}
		});
		enterButton.setBounds(73, 453, 308, 49);
		loginPanel.add(enterButton);

		
		
		/*========<< roomList panel >>======== */
		JScrollPane roomListScroll = new JScrollPane();
		roomListScroll.setBounds(118, 0, 336, 751);
		roomListPanel.add(roomListScroll);
		
		JList roomList = new JList();
		roomListScroll.setViewportView(roomList);

		JButton createRoomButton = new JButton("방생성");
		createRoomButton.setBounds(12, 0, 94, 94);
		roomListPanel.add(createRoomButton);
		
		
		
		/*========<< room panel >>========*/
		JScrollPane joinUserListScroll = new JScrollPane();
		joinUserListScroll.setBounds(0, 10, 345, 98);
		roomPanel.add(joinUserListScroll);
		
		JList joinUserList = new JList();
		joinUserListScroll.setViewportView(joinUserList);
		
		JButton roomExitButton = new JButton("나가기");
		roomExitButton.setBounds(345, 10, 109, 98);
		roomPanel.add(roomExitButton);
		
		JScrollPane chattingContentScroll = new JScrollPane();
		chattingContentScroll.setBounds(0, 108, 454, 569);
		roomPanel.add(chattingContentScroll);
		
		JTextArea chattingContent = new JTextArea();
		chattingContentScroll.setViewportView(chattingContent);
		
		sendMessageField = new JTextField();
		sendMessageField.setBounds(0, 687, 367, 64);
		roomPanel.add(sendMessageField);
		sendMessageField.setColumns(10);
		
		JButton sendButton = new JButton("New button");
		sendButton.setBounds(368, 684, 86, 67);
		roomPanel.add(sendButton);
		
		}
	
	
	private void sendRequest(RequestDto<?> requestDto) { //요청보내는 메소드
		String reqJson = gson.toJson(requestDto); 
		OutputStream outputStream = null;
		PrintWriter printWriter = null;
		try {
			outputStream = socket.getOutputStream();
			printWriter = new PrintWriter(outputStream, true);
			printWriter.println(reqJson);
			System.out.println("클라이언트 -> 서버: " + reqJson);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}
}
