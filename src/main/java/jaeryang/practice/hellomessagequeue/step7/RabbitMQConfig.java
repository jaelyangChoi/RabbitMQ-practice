package jaeryang.practice.hellomessagequeue.step7;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
	public static final String ORDER_COMPLETED_QUEUE = "order_completed_queue";
	public static final String ORDER_EXCHANGE = "order_completed_exchange";
	public static final String DLQ = "deadLetterQueue";
	public static final String DLX = "deadLetterExchange";

	@Bean
	public TopicExchange orderExchange() {
		return new TopicExchange(ORDER_EXCHANGE);
	}

	@Bean
	public TopicExchange deadLetterExchange() {
		return new TopicExchange(DLX);
	}

	// 메시지가 처리되지 못했을 경우 자동으로 Dead Letter Queue로 이동하도록 설정
	@Bean
	public Queue orderqueue() {
		// return new Queue(ORDER_COMPLETED_QUEUE, false);
		return QueueBuilder.durable(ORDER_COMPLETED_QUEUE)
			.withArgument("x-dead-letter-exchange", DLX) // Dead Letter Exchange 설정
			.withArgument("x-dead-letter-routing-key", DLQ) // Dead Letter Routing Key 설정
			.ttl(5000)
			.build();
	}

	@Bean
	public Queue deadLetterQueue() {
		return new Queue(DLQ);
	}

	@Bean
	public Binding orderComplededBinding() {
		return BindingBuilder.bind(orderqueue()).to(orderExchange()).with("order.completed.#"); //bindingKey를 넣는건데, Spring API에서는 routingKey 파라미터
	}
	// Binding도 routing key를 기준으로 정의되기 때문에, AMQP에는 “binding key”라는 독립된 개념이 없다.
	//우리가 흔히 말하는 “binding key”는 AMQP 공식 용어라기보다는 설명 편의를 위한 표현에 가깝다.


	@Bean
	public Binding deadLetterBinding() {
		return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(DLQ);
	}
}
