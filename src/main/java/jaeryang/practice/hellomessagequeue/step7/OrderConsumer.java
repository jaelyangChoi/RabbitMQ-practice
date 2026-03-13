package jaeryang.practice.hellomessagequeue.step7;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

@Component
public class OrderConsumer {

	private static final int MAX_RETRIES = 3; // 최대 재시도 횟수
	private int retryCount = 0; // 현재 재시도 횟수

	// containerFactory: 리스너 컨테이너의 설정(ACK 모드, 재시도, prefetch, concurrency 등)을 바꾸기 위해 지정
	// @Header("amqp_deliveryTag"): RabbitMQ가 메시지에 붙여주는 전달 식별자(ack/nack/reject에 필요)를 Spring이 꺼내서 주입해주는 것
	// Channel: RabbitMQ Java Client의 현재 연결/채널(프로토콜 세션) 객체로, basicAck/basicNack/basicReject 같은 저수준 AMQP 명령을 보내는 통로
	@RabbitListener(queues = RabbitMQConfig.ORDER_COMPLETED_QUEUE, containerFactory = "rabbitListenerContainerFactory")
	public void processOrder(String message, Channel channel, @Header("amqp_deliveryTag") long tag) {
		try {
			// 실패 유발
			if ("fail".equalsIgnoreCase(message)) {
				if (retryCount < MAX_RETRIES) {
					System.err.println("#### Fail & Retry: " + message + " (Retry Count: " + retryCount + ")");
					retryCount++;
					throw new RuntimeException(message);
				} else {
					System.err.println("#### 최대 횟수 초과, DLQ로 이동 시킴");
					retryCount = 0;
					// deliveryTag: 메시지 고유식별 태그, multiple: true면 deliveryTag 이하 메시지 전부, requeue: true면 다시 큐로, false면 DLQ or drop
					channel.basicNack(tag, false, false); // 메시지 거부, 재큐잉하지 않음 (DLQ로 이동)
					return;
				}
			}
			// 성공 처리
			System.out.println("# 성공: " + message);
			channel.basicAck(tag, false); // 메시지 수동 ACK
			retryCount = 0;
		} catch (Exception e) {
			System.err.println("# error 발생 : " + e.getMessage());
			try {
				// 실패 시 basicReject를 사용하여 메시지를 재처리 전송
				channel.basicReject(tag, true); //basicReject는 단일 메시지 거부 전용 (간단 버전), basicNack은 확장형 거부 (여러 메시지 가능)
			} catch (IOException ex) {
				System.err.println("# fail & reject message : " + ex.getMessage());
			}
		}
	}
}
