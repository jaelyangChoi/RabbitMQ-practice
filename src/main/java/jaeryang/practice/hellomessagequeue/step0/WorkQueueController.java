package jaeryang.practice.hellomessagequeue.step0;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WorkQueueController {

	private final WorkQueueProducer workQueueProducer;

	@PostMapping("/workQueue")
	public String workQueue(@RequestParam String message, @RequestParam int duration) {
		workQueueProducer.sendWorkQueue(message, duration);
		return "[#] Work Queue sent = " + message + ", (" + duration + ")";
	}
}
