package jaeryang.practice.hellomessagequeue.step3;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationPublisher {

	private final RabbitTemplate rabbitTemplate;

	public void publish(String message) {
		rabbitTemplate.convertAndSend(RabbitMQConfig.FANOUT_EXCHANGE, "", message);
		System.out.println("[#] Published Notification: " + message);
	}
}
