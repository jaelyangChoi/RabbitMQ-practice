package jaeryang.practice.hellomessagequeue.step3;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationSubscriber {

	public static final String CLIENT_URL = "/topic/notifications";

	// WebSocket으로 메시지를 전달하기 위한 Spring의 템플릿 클래스
	private final SimpMessagingTemplate simpMessagingTemplate;

	// RabbitMQ Queue에서 메시지 수신
	// RabbitListener에 의해 QUEUE_NAME을 바라보다가 exchange에 메시지가 도착하면 Queue로 발행되고 이 Queue가 메시지를 수신
	// String message = (String) rabbitTemplate.receiveAndConvert(RabbitMQConfig.QUEUE_NAME); 과 같은 번거로운 코딩 생략
	@RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
	public void subscribe(String message) {
		System.out.println("[#] Received Notification: " + message);
		// WebSocket을 통해 클라이언트로 메시지를 전달
		simpMessagingTemplate.convertAndSend(CLIENT_URL, message); // 클라이언트에 브로드캐스트
	}
}
