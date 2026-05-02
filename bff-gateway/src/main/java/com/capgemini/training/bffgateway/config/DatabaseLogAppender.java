package com.capgemini.training.bffgateway.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Logback Appender → writes all logs to shared application_logs table in capgemini_db.
 * BFF Gateway is reactive (WebFlux) but this appender uses raw JDBC — no conflict
 * because Logback appenders run synchronously on the logging thread, not the reactive pipeline.
 * serviceName is set from logback-spring.xml: <serviceName>bff-gateway</serviceName>
 */
public class DatabaseLogAppender extends AppenderBase<ILoggingEvent> {

    private static final int BUFFER_MAX = 500;
    private static volatile DataSource dataSource;
    private static final List<ILoggingEvent> buffer = new CopyOnWriteArrayList<>();
    private static volatile boolean firstWriteDone = false;
    private static volatile String serviceName = "unknown";

    public void setServiceName(String name) { serviceName = name; }
    public static String getServiceName() { return serviceName; }

    public static void setDataSource(DataSource ds) {
        dataSource = ds;
        List<ILoggingEvent> copy = new ArrayList<>(buffer);
        buffer.clear();
        copy.forEach(DatabaseLogAppender::writeToDb);
        System.out.println("[DatabaseLogAppender:" + serviceName + "] Flushed " + copy.size() + " buffered events to DB.");
    }

    @Override
    protected void append(ILoggingEvent event) {
        event.prepareForDeferredProcessing();
        if (dataSource == null) {
            if (buffer.size() < BUFFER_MAX) buffer.add(event);
            return;
        }
        writeToDb(event);
    }

    private static void writeToDb(ILoggingEvent event) {
        if (dataSource == null) return;
        Map<String, String> mdc = event.getMDCPropertyMap();
        String sql = """
            INSERT INTO application_logs
              (timestamp, level, service_name, logger_name, message,
               correlation_id, trace_id, span_id, thread_name, exception_detail)
            VALUES (?,?,?,?,?,?,?,?,?,?)
            """;
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.from(Instant.ofEpochMilli(event.getTimeStamp())));
            ps.setString(2,  event.getLevel().toString());
            ps.setString(3,  serviceName);
            ps.setString(4,  cap(event.getLoggerName(), 200));
            ps.setString(5,  cap(event.getFormattedMessage(), 2000));
            ps.setString(6,  mdc.getOrDefault("correlationId", null));
            ps.setString(7,  mdc.getOrDefault("traceId",       null));
            ps.setString(8,  mdc.getOrDefault("spanId",        null));
            ps.setString(9,  cap(event.getThreadName(), 100));
            ps.setString(10, extractEx(event.getThrowableProxy()));
            ps.executeUpdate();
            if (!firstWriteDone) {
                firstWriteDone = true;
                System.out.println("[DatabaseLogAppender:" + serviceName + "] FIRST WRITE SUCCESS — correlationId="
                    + mdc.getOrDefault("correlationId","null")
                    + " traceId=" + mdc.getOrDefault("traceId","null"));
            }
        } catch (Exception ex) {
            System.err.println("[DatabaseLogAppender:" + serviceName + "] INSERT FAILED: " + ex.getMessage());
        }
    }

    private static String extractEx(IThrowableProxy p) {
        if (p == null) return null;
        StringBuilder sb = new StringBuilder(p.getClassName()).append(": ").append(p.getMessage());
        if (p.getStackTraceElementProxyArray() != null && p.getStackTraceElementProxyArray().length > 0)
            sb.append("\n\tat ").append(p.getStackTraceElementProxyArray()[0].getSTEAsString());
        return cap(sb.toString(), 1000);
    }

    private static String cap(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max-3) + "..." : s;
    }
}
