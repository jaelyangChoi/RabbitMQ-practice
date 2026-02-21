package jaeryang.practice.hellomessagequeue.step0;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WorkQueueProducer {

	private final RabbitTemplate rabbitTemplate;

	public void sendWorkQueue(String workQueueMessage, int duration) {
		String message = workQueueMessage + "|" + duration;
		//convert는 “메시지 변환(직렬화)”을 해준다는 의미
		rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, message);
		System.out.println("[#] Sent workQueue: " + message);
	}
}
