package jaeryang.practice.hellomessagequeue.step0;

import org.springframework.stereotype.Component;

//consumer 역할
@Component
public class Receiver {
	public void receiveMessage(String message) {
		System.out.println("[#] Received: " + message);
	}
}
