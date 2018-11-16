package com.hiramgames;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hiramgames.dao")
public class HiramgamesApplication {

	public static void main(String[] args) {
		SpringApplication.run(HiramgamesApplication.class, args);
	}
}
