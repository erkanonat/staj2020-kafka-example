package com.havelsan.kkmconnector.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class ServiceThreadConfiguration {


    private static final String ALARM_CORE_POOL_SIZE_PROPERTY = "service.alarm.thread.pool.core.size";
    private static final String ALARM_MAX_POOL_SIZE_PROPERTY = "service.alarm.thread.pool.max.size";
    private static final String ALARM_THREAD_NAME_PREFIX_PROPERTY = "service.alarm.thread.pool.prefix";

    private static final String KAFKA_POOL_SIZE_PROPERTY = "service.kafka.thread.pool.size";
    private static final String KAFKA_THREAD_NAME_PREFIX_PROPERTY = "service.kafka.thread.pool.prefix";

    @Autowired
    Environment env;

    @Bean(name = "alarmThreadPoolTaskExecutor")
    public ThreadPoolTaskExecutor alarmThreadPoolTaskExecutor() {
        String corePoolSize = env.getProperty(ALARM_CORE_POOL_SIZE_PROPERTY);
        String maxPoolSize = env.getProperty(ALARM_MAX_POOL_SIZE_PROPERTY);
        String threadNamePrefix = env.getProperty(ALARM_THREAD_NAME_PREFIX_PROPERTY);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Integer.parseInt(corePoolSize));
        executor.setMaxPoolSize(Integer.parseInt(maxPoolSize));
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.initialize();
        return executor;
    }

    @Bean(name = "kafkaThreadPoolTaskScheduler")
    public ThreadPoolTaskScheduler kafkaThreadPoolTaskScheduler() {
        String poolSize = env.getProperty(KAFKA_POOL_SIZE_PROPERTY);
        String threadNamePrefix = env.getProperty(KAFKA_THREAD_NAME_PREFIX_PROPERTY);

        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(Integer.parseInt(poolSize));
        scheduler.setThreadNamePrefix(threadNamePrefix);
        scheduler.initialize();
        return scheduler;
    }


}
