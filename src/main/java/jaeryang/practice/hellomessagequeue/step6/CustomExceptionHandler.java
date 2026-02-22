package jaeryang.practice.hellomessagequeue.step6;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomExceptionHandler {

	private final LogPublisher logPublisher;

	//에러 처리
	public void handleException(Exception e) {
		String routingKey = e instanceof IllegalArgumentException ? "log.warn" : "log.error";
		logPublisher.publish(routingKey, "Exception Log: " + e.getMessage());
	}

	// 메시지 처리
	public void handleMessage(String message) {
		String routingKey = "log.info";
		logPublisher.publish(routingKey, "Info Log: " + message);
	}
}
