package jaeryang.practice.hellomessagequeue.step4;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NewsSubscriber {

	// WebSocket으로 메시지를 전달하기 위한 Spring의 템플릿 클래스
	private final SimpMessagingTemplate simpMessagingTemplate;

	// RabbitListener에 의해 QUEUE_NAME을 바라보다가 exchange에 메시지가 도착하면 Queue로 발행되고 이 Queue가 메시지를 수신
	@RabbitListener(queues = RabbitMQConfig.JAVA_QUEUE)
	public void javaNews(String message) {
		// 해당 destination으로 구독한 세션들에게 메시지 push
		simpMessagingTemplate.convertAndSend("/topic/java", message);
	}

	@RabbitListener(queues = RabbitMQConfig.SPRING_QUEUE)
	public void springNews(String message) {
		simpMessagingTemplate.convertAndSend("/topic/spring", message);
	}

	@RabbitListener(queues = RabbitMQConfig.VUE_QUEUE)
	public void vueNews(String message) {
		simpMessagingTemplate.convertAndSend("/topic/vue", message);
	}
}
