package ch.treasurekeep;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
        import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan()
@EnableWebMvc
@EnableScheduling
@EnableConfigurationProperties()
public class KeeperOfTheTreasureApplication {
	public static void main(String[] args) {
		System.out.println("There is where the magic happens: http://localhost:8080/swagger-ui/index.html");
		SpringApplication.run(KeeperOfTheTreasureApplication.class, args);
	}
}
