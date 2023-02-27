package dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RequestDto<T> {
	private String resource;
	private T body; //body가 String, 객체, 리스트 등 어떤 형태로든 들어올 수 있다.(제네릭으로 잡았기에)
	
}
