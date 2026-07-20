package ucu.retojulio2026.talent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TalentApplication {

	public static void main(String[] args) {
		SpringApplication.run(TalentApplication.class, args);
	}

}
