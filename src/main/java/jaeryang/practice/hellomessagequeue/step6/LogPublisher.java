package jaeryang.practice.hellomessagequeue.step6;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LogPublisher {

	private final RabbitTemplate rabbitTemplate;

	public void publish(String routingKey, String message) {
		rabbitTemplate.convertAndSend(RabbitMQConfig.TOPIC_EXCHANGE, routingKey, message);
		System.out.println("[#] message published: " + routingKey + ":" + message);
	}
}
