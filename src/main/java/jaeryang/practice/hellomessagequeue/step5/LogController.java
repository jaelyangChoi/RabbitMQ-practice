package jaeryang.practice.hellomessagequeue.step5;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

	private final CustomExceptionHandler exceptionHandler;

	@GetMapping("/error")
	public ResponseEntity<String> errorAPI() {
		try {
			String value = null;
			value.getBytes(); //null pointer exception 발생시킴
		} catch (Exception e) {
			exceptionHandler.handleException(e);
		}
		return ResponseEntity.ok("Controller NullPointerException 처리");
	}

	@GetMapping("/warn")
	public ResponseEntity<String> warnAPI() {
		try {
			throw new IllegalArgumentException("invalid argument provided");
		} catch (Exception e) {
			exceptionHandler.handleException(e);
		}
		return ResponseEntity.ok("Controller IllegalArgumentException 처리");
	}

	@PostMapping("/info")
	public ResponseEntity<String> infoAPI(@RequestBody String message) {
		exceptionHandler.handleMessage(message);
		return ResponseEntity.ok("Controller Info Log 발송 처리: " + message);
	}
}
