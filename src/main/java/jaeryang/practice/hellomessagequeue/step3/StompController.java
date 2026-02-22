package jaeryang.practice.hellomessagequeue.step3;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

/**
 * STOMP 클라이언트가 /app/send로 보낸 메시지를 @MessageMapping이 받아
 * SimpMessagingTemplate.convertAndSend("/topic/notifications", ...)로 브로드캐스트
 */
@Controller
@RequiredArgsConstructor
public class StompController {

	private final SimpMessagingTemplate simpMessagingTemplate;

	@MessageMapping("/send")
	public void sendMessage(NotificationMessage notificationMessage) {
		// 수신된 메시지를 브로드캐스팅
		String message = notificationMessage.message();
		System.out.println("[#] message = " + message);

		// 클라이언트에 메시지 브로드캐스트
		simpMessagingTemplate.convertAndSend("/topic/notifications", message);
	}
}
