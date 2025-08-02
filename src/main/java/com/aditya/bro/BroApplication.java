package com.aditya.bro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.aditya.bro.ai", "com.aditya.bro.auth", "com.aditya.bro.dispute", "com.aditya.bro.document", "com.aditya.bro.land", "com.aditya.bro.publicsearch", "com.aditya.bro.transfer", "com.aditya.bro.config", "com.aditya.bro.notification"}, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = com.aditya.bro.land.config.WebConfig.class), @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = com.aditya.bro.userback.service.LandService.class)})
@EnableMongoRepositories(basePackages = {"com.aditya.bro.auth.repository", "com.aditya.bro.dispute.repository", "com.aditya.bro.document.repository", "com.aditya.bro.land.repository", "com.aditya.bro.transfer.repository", "com.aditya.bro.userback.repository", "com.aditya.bro.notification.repository"})
public class BroApplication {

	public static void main(String[] args) {
		SpringApplication.run(BroApplication.class, args);
	}

}
