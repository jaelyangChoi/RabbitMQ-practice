package jaeryang.practice.hellomessagequeue.step3;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * HTTP(REST)로 들어온 알림을 NotificationPublisher가 RabbitMQ Exchange에 publish
 * → NotificationSubscriber가 큐에서 수신
 * → SimpMessagingTemplate.convertAndSend("/topic/notifications", ...)로 브로드캐스트
 *
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationPublisher publisher;

	@PostMapping
	public String sendNotification(@RequestBody String message) {
		publisher.publish(message);
		return "[#] Notification sent: " + message + "\n";
	}
}
