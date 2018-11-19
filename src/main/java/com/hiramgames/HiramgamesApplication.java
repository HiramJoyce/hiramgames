package com.hiramgames;

import com.hiramgames.component.HiramGamesWebSocket;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@MapperScan("com.hiramgames.dao")
public class HiramgamesApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext applicationContext = SpringApplication.run(HiramgamesApplication.class, args);
		HiramGamesWebSocket.setApplicationContext(applicationContext);
	}
}
