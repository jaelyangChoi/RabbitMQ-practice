package jaeryang.practice.hellomessagequeue.step8;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderProducer {

	private final RabbitTemplate rabbitTemplate;

	public void sendShipping(String message) {
		rabbitTemplate.convertAndSend(
			RabbitMQConfig.ORDER_TOPIC_EXCHANGE,
			"order.completed.shipping",
			message);
		System.out.println("[주문 완료. 배송 지시 메시지 생성 : " + message + "]");
	}
}
