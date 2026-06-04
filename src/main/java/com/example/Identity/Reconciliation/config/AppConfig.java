package com.example.Identity.Reconciliation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

// Custom ThreadPoolTaskExecutor
@Configuration
public class AppConfig{

    @Bean
    public Executor customThreadPoolTaskExecutor(){
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(3);
        threadPoolTaskExecutor.setMaxPoolSize(6);
        threadPoolTaskExecutor.setQueueCapacity(5);
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}