package com.example.Identity.Reconciliation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class IdentityReconciliationApplication {

	public static void main(String[] args) {
		SpringApplication.run(IdentityReconciliationApplication.class, args);
	}

}
