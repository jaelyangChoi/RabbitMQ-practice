package jaeryang.practice.hellomessagequeue.step4;

import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class NewsController {

	private final NewsPublisher newsPublisher;

	// /app/subscribe
	@MessageMapping("/subscribe")
	public void handleSubscribe(@Header("newsType") String newsType) {
		System.out.println("[#] newsType: " + newsType);

		String newsMessage = newsPublisher.publish(newsType);
		System.out.println("[#] newsMessage: " + newsMessage);

	}
}
