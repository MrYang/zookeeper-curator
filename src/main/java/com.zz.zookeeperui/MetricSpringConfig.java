package com.zz.zookeeperui;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author Mr.Yang
 * @since 2017-05-24
 */

@EnableMetrics
@Configuration
public class MetricSpringConfig extends MetricsConfigurerAdapter {

    @Override
    public void configureReporters(MetricRegistry metricRegistry) {
        Slf4jReporter.forRegistry(metricRegistry)
                .outputTo(LoggerFactory.getLogger("metric"))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build()
                .start(10, 30, TimeUnit.SECONDS);
    }
}
