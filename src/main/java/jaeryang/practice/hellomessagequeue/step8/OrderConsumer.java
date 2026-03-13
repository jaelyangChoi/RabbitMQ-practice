package jaeryang.practice.hellomessagequeue.step8;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderConsumer {
	private int retryCount;

	@RabbitListener(queues = RabbitMQConfig.ORDER_COMPLETED_QUEUE)
	public void consume(String message) {
		System.out.println("Received message: " + message + "count: " + retryCount++);
		if ("fail".equals(message)) {
			throw new RuntimeException("Processing failed. Retry: " + message);
		}
		System.out.println("Message processed successfully: " + message);
	}
}
