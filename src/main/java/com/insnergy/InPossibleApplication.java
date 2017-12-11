package com.insnergy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * http://127.0.0.1:8008 <br>
 * http://127.0.0.1:8008/application/liquibase <br>
 * http://127.0.0.1:8008/h2-console
 * python-server-url: http://ntuesoe.com:8002
 */
@SpringBootApplication
public class InPossibleApplication {
  
  public static void main(String[] args) {
    SpringApplication.run(InPossibleApplication.class, args);
  }
  
}
