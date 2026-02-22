package jaeryang.practice.hellomessagequeue.step5;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LogPublisher {

	private final RabbitTemplate rabbitTemplate;

	public void publish(String routingKey, String message) {
		rabbitTemplate.convertAndSend(RabbitMQConfig.DIRECT_EXCHANGE, routingKey, message);
		System.out.println("[#] message published: " + routingKey + ":" + message);
	}
}
