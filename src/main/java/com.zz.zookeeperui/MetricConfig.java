package com.zz.zookeeperui;

import com.codahale.metrics.*;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

/**
 * @author Mr.Yang
 * @since 2017-05-24
 */

//@Configuration
public class MetricConfig {

    @Bean
    public MetricRegistry metrics() {
        return new MetricRegistry();
    }

    @Bean
    public ConsoleReporter consoleReporter(MetricRegistry registry) {
        return ConsoleReporter.forRegistry(registry)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
    }

    @Bean
    public Slf4jReporter slf4jReporter(MetricRegistry registry) {
        Slf4jReporter slf4jReporter = Slf4jReporter.forRegistry(registry)
                .outputTo(LoggerFactory.getLogger("metric"))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();

        slf4jReporter.start(10, 20, TimeUnit.SECONDS);
        return slf4jReporter;
    }

    //@Bean
    public Meter listRequestMeter(MetricRegistry metrics) {
        return metrics.meter("list_requestMeter");
    }

    /**
     * example
     *   Timer.Context context = listTimer.time();
         try {
            process();
         } finally {
            context.stop();
         }
     */
    @Bean
    public Timer listTimer(MetricRegistry metrics) {
        return metrics.timer("executeTimer");
    }

}
