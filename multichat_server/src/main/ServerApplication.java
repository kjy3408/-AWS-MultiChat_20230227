package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;

public class ServerApplication {

	public static void main(String[] args) {
		JFrame serverFrame = new JFrame("서버");
		serverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//JFrame이 창이닫혔을때 메모리에서 제거하는 용도의 코드
		serverFrame.setSize(300, 300);
		serverFrame.setVisible(true);
		
		
		try {
			ServerSocket serverSocket = new ServerSocket(9090);
			
			while(true) {
				Socket socket = serverSocket.accept(); //client접속 기다림
				ConnectedSocket connectedSocket = new ConnectedSocket(socket); //생성된 스레드를 시작해주기위함
				connectedSocket.start(); //생성된 스레드를 시작해주기위함(run 메소드 시작)										
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
}
