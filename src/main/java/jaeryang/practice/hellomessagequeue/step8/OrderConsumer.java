package jaeryang.practice.hellomessagequeue.step8;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderConsumer {

	private final RabbitTemplate rabbitTemplate;
	private final RetryTemplate retryTemplate;

	@RabbitListener(queues = RabbitMQConfig.ORDER_COMPLETED_QUEUE)
	public void consume(String message) {
		retryTemplate.execute(context -> {
			try {
				System.out.println("# 리시브 메시지: " + message + " | 시도 횟수: " + (context.getRetryCount() + 1));

				if ("fail".equals(message)) {
					throw new RuntimeException(message);
				}
				System.out.println("# 메시지 처리 성공: " + message);
			} catch (Exception e) {
				if (context.getRetryCount() + 1 >= 3) {
					rabbitTemplate.convertAndSend(
						RabbitMQConfig.ORDER_TOPIC_DLX,
						RabbitMQConfig.DEAD_LETTER_ROUTING_KEY,
						message);
				} else {
					throw e; // RetryTemplate이 재시도를 수행하도록 예외를 다시 던짐
				}
			}
			return null;
		});
	}
}
