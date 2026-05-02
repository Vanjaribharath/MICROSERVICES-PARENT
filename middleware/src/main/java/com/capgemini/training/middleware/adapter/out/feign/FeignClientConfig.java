package com.capgemini.training.middleware.adapter.out.feign;
import feign.Logger; import feign.Request; import feign.codec.ErrorDecoder;
import java.util.NoSuchElementException;
import org.springframework.context.annotation.Bean;
import java.util.concurrent.TimeUnit;
public class FeignClientConfig {
    @Bean public Logger.Level logLevel() { return Logger.Level.FULL; }
    @Bean public Request.Options options() { return new Request.Options(3, TimeUnit.SECONDS, 5, TimeUnit.SECONDS, true); }
    @Bean public ErrorDecoder errorDecoder() {
        return (key, resp) -> switch (resp.status()) {
            case 400 -> new IllegalArgumentException("Bad request [" + key + "]");
            case 404 -> new NoSuchElementException("Not found [" + key + "]");
            case 503 -> new feign.RetryableException(resp.status(), "system-api unavailable",
                feign.Request.HttpMethod.GET, (java.util.Date) null, null);
            default  -> new RuntimeException("system-api error " + resp.status());
        };
    }
}
