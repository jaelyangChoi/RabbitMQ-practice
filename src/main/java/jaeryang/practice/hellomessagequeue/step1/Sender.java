package jaeryang.practice.hellomessagequeue.step1;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class Sender {

	private final RabbitTemplate rabbitTemplate;

	public void send(String message) {
		rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, message);
		System.out.println("[#] Sent: " + message);
	}
}
