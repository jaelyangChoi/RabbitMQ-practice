# Step6: Log 처리 흐름 (tutorial-step6 기준)

1) 핵심 컴포넌트(파일 경로)
- `src/main/java/jaeryang/practice/hellomessagequeue/step6/RabbitMQConfig.java`
  - `TopicExchange`(`topic_exchange`)과 4개 큐(`error_queue`, `warn_queue`, `info_queue`, `all_log_queue`) 및 바인딩(`log.error`, `log.warn`, `log.info`, `log.*`) 정의
- `src/main/java/jaeryang/practice/hellomessagequeue/step6/LogPublisher.java`
  - `rabbitTemplate.convertAndSend(RabbitMQConfig.TOPIC_EXCHANGE, routingKey, message)`로 메시지 발행
- `src/main/java/jaeryang/practice/hellomessagequeue/step6/LogConsumer.java`
  - 각 큐(`error_queue`, `warn_queue`, `info_queue`, `all_log_queue`)에 대해 `@RabbitListener`로 메시지 수신 후 콘솔 출력
- `src/main/java/jaeryang/practice/hellomessagequeue/step6/CustomExceptionHandler.java`
  - 예외를 받아 라우팅키를 결정(예: `IllegalArgumentException` → `log.warn`, 그 외 → `log.error`)하고 `LogPublisher.publish(...)` 호출
  - 일반 메시지는 `log.info`로 발행
- `src/main/java/jaeryang/practice/hellomessagequeue/step6/LogController.java`
  - 테스트용 엔드포인트(`/api/logs/error`, `/api/logs/warn`, `/api/logs/info`)를 제공

2) 클라이언트/호출자 관점 (엔드포인트 및 입력)
- `GET /api/logs/error`
  - `LogController`에서 NPE를 일부러 발생시켜 예외를 캐치하고 `CustomExceptionHandler.handleException(e)` 호출
- `GET /api/logs/warn`
  - `LogController`에서 `IllegalArgumentException`을 던지고 캐치하여 `CustomExceptionHandler.handleException(e)` 호출
- `POST /api/logs/info` (RequestBody: 문자열 메시지)
  - 요청 본문 메시지를 `CustomExceptionHandler.handleMessage(message)`로 전달하여 `log.info` 라우팅키로 발행

3) 시퀀스(에러 흐름: 예외 → RabbitMQ → 소비자)
- 1) 클라이언트가 `GET /api/logs/error` 호출
- 2) `LogController`에서 NPE 발생 → catch → `exceptionHandler.handleException(e)` 호출
- 3) `CustomExceptionHandler.handleException(Exception e)`
  - 라우팅키 결정: `e instanceof IllegalArgumentException ? "log.warn" : "log.error"`
  - `logPublisher.publish(routingKey, "Exception Log: " + e.getMessage());`
- 4) `LogPublisher.publish(routingKey, message)`
  - `rabbitTemplate.convertAndSend(RabbitMQConfig.TOPIC_EXCHANGE, routingKey, message);`
- 5) RabbitMQ
  - `topic_exchange`에서 라우팅키(`log.error`)에 매칭되는 바인딩을 찾아 `error_queue`로 메시지를 전달
  - `all_log_queue`는 `log.*` 바인딩을 통해 모든 `log.<level>` 메시지를 수신
- 6) `LogConsumer.consumeError(String message)`가 `error_queue` 메시지를 수신하고 콘솔에 출력

결과: 예외 발생 시 라우팅키에 따라 적절한 큐와(또는 와일드카드로 모든 로그 큐)로 전달되며, 대응 소비자가 메시지를 처리(출력)합니다.

4) 시퀀스(정보/경고 흐름)
- WARN 경로 (`GET /api/logs/warn`)
  - Controller가 `IllegalArgumentException`을 던짐 → `CustomExceptionHandler`는 `log.warn` 라우팅키로 발행 → `warn_queue`에 전달 → `LogConsumer.consumeWarn(...)` 처리
- INFO 경로 (`POST /api/logs/info`)
  - 요청 본문 메시지를 `CustomExceptionHandler.handleMessage(message)`가 `log.info` 라우팅키로 발행 → `info_queue` → `LogConsumer.consumeInfo(...)`
- 모든 로그 받기
  - `all_log_queue`는 `log.*` 바인딩을 가지고 있으므로 모든 `log.<level>` 메시지를 수신합니다. 운영에서 전체 로그 파이프라인(집계/색인)에 유용합니다.

5) 테스트
  ```
  curl -X GET "http://localhost:8080/api/logs/error" 
  curl -X GET "http://localhost:8080/api/logs/warn" 
  curl -X POST "http://localhost:8080/api/logs/info" \
   -H "Content-Type: application/json" \
   -d "\"System initialized successfully.\""
  ```
