package jaeryang.practice.hellomessagequeue.step6;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
	// 큐 네임 설정
	public static final String ERROR_QUEUE = "error_queue";
	public static final String WARN_QUEUE = "warn_queue";
	public static final String INFO_QUEUE = "info_queue";
	public static final String ALL_LOG_QUEUE = "all_log_queue";

	public static final String TOPIC_EXCHANGE = "topic_exchange";

	@Bean
	public TopicExchange topicExchange() {
		return new TopicExchange(TOPIC_EXCHANGE);
	}

	//Spring이 시작될 때 RabbitMQ 서버에 “이 큐를 생성하라”라고 선언하기 위해
	@Bean
	public Queue errorQueue() {
		//QUEUE_NAME은 메시지가 쌓이고 처리될 큐의 이름을 정의
		return new Queue(ERROR_QUEUE, false); //영속화 여부
	}

	@Bean
	public Queue warnQueue() {
		return new Queue(WARN_QUEUE, false);
	}

	@Bean
	public Queue infoQueue() {
		return new Queue(INFO_QUEUE, false);
	}

	@Bean
	public Queue allLogQueue() {
		return new Queue(ALL_LOG_QUEUE, false);
	}

	@Bean
	public Binding errorBinding() {
		return BindingBuilder.bind(errorQueue()).to(topicExchange()).with("log.error");
	}

	@Bean
	public Binding warnBinding() {
		return BindingBuilder.bind(warnQueue()).to(topicExchange()).with("log.warn");
	}

	@Bean
	public Binding infoBinding() {
		return BindingBuilder.bind(infoQueue()).to(topicExchange()).with("log.info");
	}

	@Bean
	public Binding allLogBinding() {
		return BindingBuilder.bind(allLogQueue()).to(topicExchange()).with("log.*");
	}
}
