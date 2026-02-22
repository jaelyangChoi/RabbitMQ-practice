package jaeryang.practice.hellomessagequeue.step5;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomExceptionHandler {

	private final LogPublisher logPublisher;

	//에러 처리
	public void handleException(Exception e) {
		String routingKey = e instanceof IllegalArgumentException ? "warn" : "error";
		logPublisher.publish(routingKey, "Exception Log: " + e.getMessage());
	}

	// 메시지 처리
	public void handleMessage(String message) {
		String routingKey = "info";
		logPublisher.publish(routingKey, "Info Log: " + message);
	}
}
