package jaeryang.practice.hellomessagequeue.step0;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
	// 큐 네임 설정
	public static final String QUEUE_NAME = "WorkQueue";

	//Spring이 시작될 때 RabbitMQ 서버에 “이 큐를 생성하라”라고 선언하기 위해
	@Bean
	public Queue queue() {
		//QUEUE_NAME은 메시지가 쌓이고 처리될 큐의 이름을 정의
		return new Queue(QUEUE_NAME, true); //영속화 여부. true: 서버 셧다운 시 데이터 보관
	}


	//RabbitTemplate: 래빗엠큐로 메시지를 보내는 데 사용되는 템플릿 클래스
	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		return new RabbitTemplate(connectionFactory); //ConnectionFactory: 래빗엠큐의 연결 관리
	}

	//SimpleMessageListenerContainer: 래빗엠큐에서 메시지를 수신하고 처리하는 리스너 컨테이너
	@Bean
	public SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
		MessageListenerAdapter listenerAdapter) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(QUEUE_NAME);
		container.setMessageListener(listenerAdapter);
		container.setAcknowledgeMode(AcknowledgeMode.AUTO); //default이나 명시
		return container;
	}

	//MessageListenerAdapter: 메시지를 처리할 리스너 어댑터
	@Bean
	public MessageListenerAdapter listenerAdapter(WorkQueueConsumer workQueueTask) {
		return new MessageListenerAdapter(workQueueTask, "workQueueTask");
	}
}
