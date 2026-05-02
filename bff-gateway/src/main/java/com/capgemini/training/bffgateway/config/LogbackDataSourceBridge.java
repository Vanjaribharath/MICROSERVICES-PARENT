package com.capgemini.training.bffgateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Injects Spring DataSource into DatabaseLogAppender after context starts.
 * BFF Gateway uses the SAME capgemini_db as system-api — only for logging, not business data.
 * JDBC DataSource does NOT conflict with the reactive gateway (WebFlux/Netty).
 * Only spring-boot-starter-web (servlet/Tomcat) conflicts — JDBC is fine.
 */
@Configuration
@RequiredArgsConstructor
public class LogbackDataSourceBridge {

    private final DataSource   dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Bean
    public ApplicationRunner connectLogbackToDatabase() {
        return args -> {
            String svc = DatabaseLogAppender.getServiceName();
            System.out.println("[LogbackBridge:" + svc + "] Connecting DB appender to capgemini_db...");
            try (Connection conn = dataSource.getConnection()) {
                System.out.println("[LogbackBridge:" + svc + "] Connected: " + conn.getMetaData().getURL());
            }
            Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM application_logs", Long.class);
            System.out.println("[LogbackBridge:" + svc + "] application_logs rows=" + count);

            DatabaseLogAppender.setDataSource(dataSource);

            jdbcTemplate.update("""
                INSERT INTO application_logs
                  (timestamp, level, service_name, logger_name, message, thread_name)
                VALUES (NOW(),'INFO',?,
                        'LogbackDataSourceBridge',
                        'DB logging active — capgemini_db — correlationId+traceId+spanId captured',
                        'main')
                """, svc);
            System.out.println("[LogbackBridge:" + svc + "] DB logging ACTIVE. Startup row inserted.");
        };
    }
}
