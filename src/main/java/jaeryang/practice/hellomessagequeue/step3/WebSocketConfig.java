package jaeryang.practice.hellomessagequeue.step3;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
// Spring에서 WebSocket 메시지 브로커를 구성하기 위한 인터페이스
// 웹소켓 연결, 메시지 브로커 설정 및 라우팅 등의 웹 소켓 관련 확장기능을 제공
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// SimpleBroker: Spring이 내부 메모리로 간단한 브로커 역할을 하겠다 (RabbitMQ 아님)
		// 서버 → 클라(구독 대상). 서버가 convertAndSend("/topic/...") 하면 → 그 destination 구독자에게 push(세션 뒤져서)
		registry.enableSimpleBroker("/topic"); // 클라이언트가 구독할 수 있는 경로 설정. SimpleBroker가 /topic/*에 대한 구독을 관리
		// 클라 → 서버(@MessageMapping으로 들어옴). 클라가 SEND /app/... 하면 → 서버의 @MessageMapping("...")로 라우팅
		registry.setApplicationDestinationPrefixes("/app"); // 클라이언트가 메시지를 보낼 때 사용할 접두사 설정
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws") // 클라이언트가 WebSocket 연결을 시도할 때 사용할 엔드포인트 설정
			.setAllowedOriginPatterns("*") // CORS 설정
			.withSockJS(); // SockJS 지원
	}
}
