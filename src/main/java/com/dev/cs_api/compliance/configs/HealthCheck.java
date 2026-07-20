package com.dev.cs_api.compliance.configs;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class HealthCheck implements HealthIndicator {
    @Override
    public Health getHealth(boolean includeDetails) {
        return HealthIndicator.super.getHealth(includeDetails);
    }

    @Override
    public Health health() {
        try {
            return Health.up()
                    .withDetail("api_status", "Operacional")
                    .withDetail("version", "1.0.0")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("erro", e.getMessage())
                    .build();
        }
    }
}
