# Notification / News 처리 흐름 정리

주요 포인트 요약
- STOMP 애플리케이션 엔드포인트: `/app/subscribe` (서버의 `@MessageMapping("/subscribe")`)
- WebSocket 엔드포인트: `/ws` (SockJS)
- SimpleBroker 접두사(구독): `/topic/*` (예: `/topic/java`, `/topic/spring`, `/topic/vue`)
- RabbitMQ 교환: `newsExchange`(FanoutExchange). 세 개의 큐(`javaQueue`, `springQueue`, `vueQueue`)가 바인딩되어 있음
- 전달 경로(두 가지)
  1. STOMP → 애플리케이션(`NewsController`) → RabbitMQ(`NewsPublisher`) → 큐 → `NewsSubscriber` → WebSocket(`/topic/...`) 브로드캐스트
  2. HTTP REST → `NewsPublisher` → (위와 동일)

핵심 설정 요약 (`WebSocketConfig`)
- registry.enableSimpleBroker("/topic")
- registry.setApplicationDestinationPrefixes("/app")
- registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();

시퀀스 A — STOMP → 서버 → RabbitMQ → WebSocket 브로드캐스트 (구체)
1. 클라이언트가 STOMP로 서버에 메시지 전송
   - destination: `/app/subscribe`
   - 헤더: `newsType` (예: `java`, `spring`, `vue`) — `NewsController`에서 `@Header("newsType") String newsType`으로 수신
2. 서버: `NewsController.handleSubscribe(newsType)` 실행
   - 코드 위치: `src/main/java/jaeryang/practice/hellomessagequeue/step4/NewsController.java`
   - 메서드 시그니처:
     ```java
     @MessageMapping("/subscribe")
     public void handleSubscribe(@Header("newsType") String newsType)
     ```
   - 동작: `String newsMessage = newsPublisher.publish(newsType);` 호출
3. `NewsPublisher.publish(newsType)`
   - 위치: `src/main/java/jaeryang/practice/hellomessagequeue/step4/NewsPublisher.java`
   - 내부: `rabbitTemplate.convertAndSend(RabbitMQConfig.FANOUT_EXCHANGE_FOR_NEWS, news, message);`
   - 반환값: 발행된 `message` 문자열
4. RabbitMQ
   - 교환: `newsExchange` (FanoutExchange)
   - 바인딩된 큐: `javaQueue`, `springQueue`, `vueQueue`
   - Fanout 특성: 라우팅 키를 무시하고 교환에 바인딩된 모든 큐로 메시지를 전송함
5. `NewsSubscriber`가 각 큐에서 메시지 수신
   - 위치: `src/main/java/jaeryang/practice/hellomessagequeue/step4/NewsSubscriber.java`
   - 메서드 예:
     ```java
     @RabbitListener(queues = RabbitMQConfig.JAVA_QUEUE)
     public void javaNews(String message) {
         simpMessagingTemplate.convertAndSend("/topic/java", message);
     }
     // springNews -> /topic/spring, vueNews -> /topic/vue
     ```
6. SimpleBroker가 `/topic/java`, `/topic/spring`, `/topic/vue`을 구독한 브라우저 클라이언트에게 메시지 전송

결과: STOMP로 `/app/subscribe`을 호출하면 내부적으로 RabbitMQ에 발행되고, FanoutExchange의 특성 때문에 세 개의 큐에 동일 메시지가 들어가며 결국 세 개 토픽 모두에 브로드캐스트됩니다.
다만, 클라이언트는 구독한 토픽에 대한 메시지만 수신합니다.<br>
(사실 해당 예제는 Topic Exchange가 적합하나, 아직 학습 전이라 Fanout Exchange로 구현되어 있음)

시퀀스 B — HTTP REST → RabbitMQ → WebSocket 브로드캐스트
1. 클라이언트(또는 외부)가 HTTP POST 호출
   - 엔드포인트: `POST /news/api/publish?newsType=java`
   - 컨트롤러: `NewsRestController.publishNews(String newsType)`
   - 이 메서드는 `newsPublisher.publishAPI(newsType)` 호출
2. 이후의 흐름은 시퀀스 A의 RabbitMQ → Subscriber → WebSocket 브로드캐스트와 동일
