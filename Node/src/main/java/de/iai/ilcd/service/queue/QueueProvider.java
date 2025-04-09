package de.iai.ilcd.service.queue;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.annotation.ApplicationScope;

/**
 * Provides a ThreadPoolTaskExecutor with a single Thread (a queue) that can be autowired where needed.
 */
@Configuration
public class QueueProvider {

    @Bean(name = "globalQueue")
    @ApplicationScope
    public ThreadPoolTaskExecutor getGlobalQueue() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(1);
        executor.setThreadNamePrefix("globalQueue");
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.setAwaitTerminationSeconds(60);
        return executor;
    }

}
