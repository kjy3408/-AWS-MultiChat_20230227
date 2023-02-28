package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import dto.request.RequestDto;
import dto.response.ResponseDto;
import entity.Room;
import lombok.Getter;

@Getter
public class ConnectedSocket extends Thread{ //ConnectedSocket하나가 유저 하나

	//connectedSocketList => 전체 클라이언트
	private static List<ConnectedSocket> connectedSocketList = new ArrayList<>(); //연결된 소켓들을 담을 리스트
	private static List<Room> roomList = new ArrayList<>();
	private static int index = 0;
	private Socket socket;
	private String username;

	private Gson gson;
	
	public ConnectedSocket(Socket socket) {
		this.socket = socket;
		gson = new Gson();
		Room room = new Room("TestRoom" + index, "TestUser" + index);
		index++;
		roomList.add(room);
	}
	
	@Override
	public void run() {
		while(true) {
			BufferedReader bufferedReader;
			try {
				bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String requestJson = bufferedReader.readLine();
				
				System.out.println("요청: " + requestJson);
				requestMapping(requestJson);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void requestMapping(String requestJson) {
		RequestDto<?> requestDto = gson.fromJson(requestJson, RequestDto.class); //requestJson을 RequestDto로 변환해라
		
		switch(requestDto.getResource()) {
			case "usernameCheck":
				checkUsername((String)requestDto.getBody()); //제네릭(object)타입으로 받았으니 String으로 downCating해줘야함.
				break;
			
			case "createRoom":
				Room room = new Room((String)requestDto.getBody(), username); //요청보낸 녀석이 방장이기에 username 
				room.getUsers().add(this); //방을 만들었으니 자기 자신이 list에 담겨야함.(같은 방에 있는 사람끼리 대화하기위함)
				roomList.add(room); //static list에 생성된 방을 추가해줌.
				ResponseDto<String> responseDto = new ResponseDto<>("createRoomSuccessfully", null);
				sendToMe(responseDto);
				refreshUsernameList(username); //-> 자기자신 넣어줌.(방장 및 유저 -> refreshUsernameList() 속 로직에 방장과 유저를 구분하는 로직있음 
				sendToAll(refreshRoomList(), connectedSocketList);
				break;
		
		}
	}
	
	private void checkUsername(String username) {
		if(username.isBlank()) {
			sendToMe(new ResponseDto<String>("usernameCheckIsBlank", "사용자 이름은 공백일 수 없습니다."));
			return;
		}
		
		for(ConnectedSocket connectedSocket : connectedSocketList) {
			if(connectedSocket.getUsername().equals(username)) {
				sendToMe(new ResponseDto<String>("usernameCheckIsDuplicate", "이미 사용중인 이름입니다."));
				return;
			}
		}
		
		this.username = username;
		connectedSocketList.add(this); //위 조건에 걸리지 않을때 로그인되며 리스트에 소켓이 담김.(usernameCheck)
		sendToMe(new ResponseDto<String>("usernameCheckSuccessfully", null));
		sendToMe(refreshRoomList());
	}
	
	private ResponseDto<List<Map<String, String>>> refreshRoomList() {
		List<Map<String, String>> roomNameList = new ArrayList<>();
		
		for(Room room : roomList) {
			Map<String, String> roomInfo = new HashMap<>();
			roomInfo.put("roomName", room.getRoomName());
			roomInfo.put("owenr", room.getOwner());
			roomNameList.add(roomInfo);
		}
		ResponseDto<List<Map<String, String>>> responseDto = new ResponseDto<List<Map<String, String>>>("refreshRoomList", roomNameList);
		return responseDto;
	}
	
	private Room findConnectedRoom(String username) {
		for(Room r : roomList) {
			for(ConnectedSocket cs : r.getUsers()) {
				if(cs.getUsername().equals(username)) {
					return r;
				}
			}
		}
		return null;
	}
	
	private void refreshUsernameList(String username){
		Room room = findConnectedRoom(username);
		List<String> usernameList = new ArrayList<>();
		usernameList.add("방제목: " + room.getRoomName());
		for(ConnectedSocket connectedSocket : room.getUsers()) {
			if(connectedSocket.getUsername().equals(room.getOwner())) {
				usernameList.add(connectedSocket.getUsername() + "(방장)");
				continue;
			}
			usernameList.add(connectedSocket.getUsername());
		}
		ResponseDto<List<String>> responseDto = new ResponseDto<List<String>>("refreshUsernameList", usernameList);
		sendToAll(responseDto, room.getUsers());//room.getUsers -> 방에 들어있는 사람들 한테만 방에있는 유저리스트가 refresh해야하기때문.
		
	}

	private void sendToMe(ResponseDto<?> responseDto) {
		try {
			OutputStream outputStream = socket.getOutputStream();
			PrintWriter printWriter = new PrintWriter(outputStream, true);
			
			String responseJson = gson.toJson(responseDto);
			printWriter.println(responseJson);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendToAll(ResponseDto<?> responseDto, List<ConnectedSocket> connectedSockets) {
		for(ConnectedSocket connectedSocket : connectedSockets) { //전체에게 뿌려주기위해 connectedSocket 반복돌림
			try {
				OutputStream outputStream = connectedSocket.getSocket().getOutputStream(); //connectedSocket이 가지고있는 socket에게 뿌려주기위해 connectedSocket을 참조해서 가져와야함.
				PrintWriter printWriter = new PrintWriter(outputStream, true);
				
				String responseJson = gson.toJson(responseDto);
				printWriter.println(responseJson);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
