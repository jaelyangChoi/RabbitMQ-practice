package jaeryang.practice.hellomessagequeue.step3;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
	// 큐 네임 설정
	public static final String QUEUE_NAME = "notificationQueue";
	public static final String FANOUT_EXCHANGE = "notificationExchange";

	//Spring이 시작될 때 RabbitMQ 서버에 “이 큐를 생성하라”라고 선언하기 위해
	@Bean
	public Queue queue() {
		//QUEUE_NAME은 메시지가 쌓이고 처리될 큐의 이름을 정의
		return new Queue(QUEUE_NAME, false); //영속화 여부
	}

	@Bean
	public FanoutExchange fanoutExchange() {
		//메시지를 수신하면 모든 큐로 브로드캐스트
		return new FanoutExchange(FANOUT_EXCHANGE);
	}

	@Bean
	public Binding bindingNotification(Queue notificationQueue, FanoutExchange fanoutExchange) {
		//큐와 익스체인지를 연결
		return BindingBuilder.bind(notificationQueue).to(fanoutExchange);
	}
}
