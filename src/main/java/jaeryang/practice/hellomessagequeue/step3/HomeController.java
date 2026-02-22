package jaeryang.practice.hellomessagequeue.step3;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class HomeController {

	@GetMapping("/home")
	public String home(Model model) {
		model.addAttribute("message", "Hello, RabbitMQ with Spring Boot!");
		return "home";
	}
}
