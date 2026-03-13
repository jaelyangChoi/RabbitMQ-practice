package jaeryang.practice.hellomessagequeue.step7;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderDLQConsumer {

	private final RabbitTemplate rabbitTemplate;

	@RabbitListener(queues = RabbitMQConfig.DLQ)
	public void process(String message) {
		System.out.println("DLQ Message Received: " + message);

		try {
			String fixedMessage = "success";

			rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE,
				"order.completed.shipping",
				fixedMessage
			);
			System.out.println("DLQ Message Sent: " + fixedMessage);
		} catch (Exception e) {
			System.err.println("### [DLQ Consumer Error] " + e.getMessage());
		}
	}
}
