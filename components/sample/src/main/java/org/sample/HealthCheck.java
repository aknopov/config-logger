package org.sample;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Returns health status on "/health" endpoint
 */
// See config "management.endpoints.web.base-path"
// Test with `curl http://localhost:8080/health`
@Component
public class HealthCheck implements HealthIndicator {
    private final String version;

    public HealthCheck(@Value("${APP_VERSION:UNKNOWN}") String version) {
        this.version = version;
    }

    @Override
    public Health health() {
        return Health.up()
                .withDetail("version", version)
                .build();
    }
}
