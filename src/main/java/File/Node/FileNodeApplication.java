package File.Node;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.imageio.ImageIO;

@SpringBootApplication
@EnableAsync
public class FileNodeApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileNodeApplication.class, args);
	}

	@PostConstruct
	public void initImageIO() {
		ImageIO.scanForPlugins();
		System.out.println(" ImageIO plugins loaded");
	}
}
