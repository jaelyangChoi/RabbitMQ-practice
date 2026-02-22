# Step5: Log 처리 흐름 (tutorial-step5 기준)

1) 핵심 컴포넌트(파일 경로)
- `src/main/java/jaeryang/practice/hellomessagequeue/step5/RabbitMQConfig.java`
  - DirectExchange(`direct_exchange`)와 3개 큐(`error_queue`, `warn_queue`, `info_queue`) 및 바인딩(routing keys: `error`, `warn`, `info`)을 정의
- `src/main/java/jaeryang/practice/hellomessagequeue/step5/LogPublisher.java`
  - `rabbitTemplate.convertAndSend(DIRECT_EXCHANGE, routingKey, message)`로 메시지 발행
- `src/main/java/jaeryang/practice/hellomessagequeue/step5/LogConsumer.java`
  - 각 큐(`error_queue`, `warn_queue`, `info_queue`)에 대해 `@RabbitListener`로 메시지 수신 후 콘솔 출력
- `src/main/java/jaeryang/practice/hellomessagequeue/step5/CustomExceptionHandler.java`
  - 예외를 받아 라우팅키를 결정(`IllegalArgumentException` → `warn`, 그 외 → `error`)하고 `LogPublisher.publish(...)` 호출
  - 일반 메시지는 `info` 라우팅키로 발행
- `src/main/java/jaeryang/practice/hellomessagequeue/step5/LogController.java`
  - HTTP 엔드포인트(`/api/logs/error`, `/api/logs/warn`, `/api/logs/info`)로 테스트용 예외/로그 트리거 제공

2) 클라이언트/호출자 관점 (엔드포인트 및 입력)
- `GET /api/logs/error`
  - `LogController` 내부에서 NPE를 일부러 발생시켜 예외를 캐치하고 `CustomExceptionHandler.handleException(e)` 호출
- `GET /api/logs/warn`
  - `LogController` 내부에서 `IllegalArgumentException`을 던지고 캐치하여 `CustomExceptionHandler.handleException(e)` 호출
- `POST /api/logs/info` (RequestBody: 문자열 메시지)
  - 요청 본문 메시지를 `CustomExceptionHandler.handleMessage(message)`로 전달하여 `info` 레벨로 발행

3) 시퀀스(에러 흐름: 예외 발생 → RabbitMQ → 소비자)
- 1) 클라이언트가 `GET /api/logs/error` 호출
- 2) `LogController`에서 NPE 발생 → catch → `exceptionHandler.handleException(e)` 호출
- 3) `CustomExceptionHandler.handleException(Exception e)`
  - 라우팅키 결정: `e instanceof IllegalArgumentException ? "warn" : "error"`
  - `logPublisher.publish(routingKey, "Exception Log: " + e.getMessage());`
- 4) `LogPublisher.publish(routingKey, message)`
  - `rabbitTemplate.convertAndSend(RabbitMQConfig.DIRECT_EXCHANGE, routingKey, message);`
- 5) RabbitMQ
  - `direct_exchange`에서 라우팅키(`error`)에 일치하는 바인딩을 찾아 `error_queue`로 메시지 전달
- 6) `LogConsumer.consumeError(String message)`가 `error_queue`의 메시지를 수신하고 콘솔에 출력

결과: 예외가 발생하면 해당 예외의 성격에 따라 `error` 또는 `warn` 큐로 메시지가 라우팅되고, 대응 Consumer가 메시지를 처리(로그 출력)한다.

4) 시퀀스(정보/경고 흐름)
- WARN 경로 (`GET /api/logs/warn`)
  - Controller가 `IllegalArgumentException`을 던짐 → `CustomExceptionHandler`는 `warn` 라우팅키로 발행 → `warn_queue`에 전달 → `LogConsumer.consumeWarn(...)`가 처리
- INFO 경로 (`POST /api/logs/info`)
  - 요청 본문 메시지를 `CustomExceptionHandler.handleMessage(message)`가 `info` 라우팅키로 발행 → `info_queue` → `LogConsumer.consumeInfo(...)`

5) 테스트
    ```
    curl -X GET "http://localhost:8080/api/logs/error" 
    curl -X GET "http://localhost:8080/api/logs/warn" 
    curl -X POST "http://localhost:8080/api/logs/info" \
     -H "Content-Type: application/json" \
     -d "\"System initialized successfully.\""
    ```
