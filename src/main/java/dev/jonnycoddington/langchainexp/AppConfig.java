package dev.jonnycoddington.langchainexp;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
  
  @Bean
  public Dotenv dotenv() {
    return Dotenv.configure()
        .filename(".env")
        .ignoreIfMalformed()
        .ignoreIfMissing()
        .load();
  }
}
