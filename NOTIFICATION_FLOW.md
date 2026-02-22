# Notification 처리 흐름 정리

이 문서는 프로젝트(HelloMessageQueue)의 STOMP/WebSocket + RabbitMQ 기반 알림 처리 흐름을 엔드투엔드로 정리합니다. 클라이언트가 구독/전송할 때 메시지가 어떻게 처리되어 브라우저로 전달되는지, 관련 코드 위치와 핵심 훅(method signatures), Lombok 적용 권장 등을 포함합니다.

요약
- 프로젝트는 두 경로로 클라이언트에 알림을 전달합니다:
  1. STOMP -> 애플리케이션(@MessageMapping) -> SimpleBroker(/topic/...) 브로드캐스트
  2. REST -> RabbitMQ(exchange) 발행 -> @RabbitListener(큐) 수신 -> SimpleBroker(/topic/...) 브로드캐스트
- 현재 토픽 이름은 `/topic/notifications`(복수)로 통일되어 있습니다.

체크리스트
- [x] WebSocket(STOMP) 설정 위치: `src/main/java/jaeryang/practice/hellomessagequeue/step3/WebSocketConfig.java`
- [x] STOMP 핸들러: `src/main/java/jaeryang/practice/hellomessagequeue/step3/StompController.java`
- [x] REST 발행자: `src/main/java/jaeryang/practice/hellomessagequeue/step3/NotificationController.java`
- [x] RabbitMQ 발행자: `src/main/java/jaeryang/practice/hellomessagequeue/step3/NotificationPublisher.java`
- [x] RabbitMQ 설정: `src/main/java/jaeryang/practice/hellomessagequeue/step3/RabbitMQConfig.java`
- [x] RabbitMQ 구독자: `src/main/java/jaeryang/practice/hellomessagequeue/step3/NotificationSubscriber.java`
- [x] 클라이언트(템플릿): `src/main/resources/templates/index.html`

1) 핵심 설정 파일

- `WebSocketConfig` (요약)
  - registry.enableSimpleBroker("/topic")
  - registry.setApplicationDestinationPrefixes("/app")
  - registerStompEndpoints: `/ws` (SockJS)

  파일: `src/main/java/jaeryang/practice/hellomessagequeue/step3/WebSocketConfig.java`

2) 클라이언트(브라우저)에서의 동작(핵심)
- index.html (요약)
  - SockJS로 `/ws` 연결
  - 구독: `stompClient.subscribe('/topic/notifications', callback)`
  - 전송: `stompClient.send('/app/send', {}, JSON.stringify({ message }))`
  - 파일: `src/main/resources/templates/index.html`

3) 시퀀스 A — 클라이언트 STOMP 메시지(/app/send) → 브로드캐스트
- 클라이언트: `stompClient.send('/app/send', {}, JSON.stringify({ message }))`
- 서버 라우팅: `/app` 접두사 때문에 요청이 애플리케이션으로 전달
- 핸들러: `StompController`
  - 파일: `src/main/java/jaeryang/practice/hellomessagequeue/step3/StompController.java`
  - 메서드 시그니처:
    ```java
    @MessageMapping("/send")
    public void sendMessage(NotificationMessage notificationMessage)
    ```
  - 현재 구현: `simpMessagingTemplate.convertAndSend("/topic/notifications", message);`
- 브로드캐스트: SimpleBroker가 `/topic/notifications`을 구독한 클라이언트에게 메시지 전송

4) 시퀀스 B — REST -> RabbitMQ -> Subscriber -> 브로드캐스트
- REST 엔드포인트: `POST /notifications`
  - 컨트롤러: `NotificationController`
  - 메서드 시그니처:
    ```java
    @PostMapping
    public String sendNotification(@RequestBody String message)
    ```
  - 동작: `publisher.publish(message);`
- RabbitMQ Publisher: `NotificationPublisher`
  - 메서드 시그니처:
    ```java
    public void publish(String message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.FANOUT_EXCHANGE, "", message);
    }
    ```
  - exchange 타입: `FanoutExchange` (모든 바인딩된 큐로 브로드캐스트)
- RabbitMQ 구성: `RabbitMQConfig`
  - `QUEUE_NAME = "notificationQueue"`
  - `FANOUT_EXCHANGE = "notificationExchange"`
  - Binding: `BindingBuilder.bind(notificationQueue).to(fanoutExchange)`
- Subscriber: `NotificationSubscriber`
  - 애노테이션: `@RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)`
  - 메서드 시그니처:
    ```java
    public void subscribe(String message)
    ```
  - 동작: `simpMessagingTemplate.convertAndSend(CLIENT_URL, message);` // CLIENT_URL = "/topic/notifications"
- 결과: SimpleBroker가 최종적으로 `/topic/notifications` 구독자들에게 메시지를 전달
