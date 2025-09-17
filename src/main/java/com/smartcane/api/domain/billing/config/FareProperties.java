package com.smartcane.api.domain.billing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "smartcane.fare")
@Data
public class FareProperties {
    private String currency = "KRW";
    private int busFare = 1400;
    private int subwayFare = 1550;

    private Discounts discounts = new Discounts();
    private Transfer transfer = new Transfer();

    @Data
    public static class Discounts {
        private double accessibleUserRate = 0.0;
        private double subscriptionRate = 0.0;
    }

    @Data
    public static class Transfer {
        private boolean enabled = true;
        private int windowMinutes = 30;
        private int maxTransfers = 4;
        private Pricing pricing = new Pricing();

        @Data
        public static class Pricing {
            private Type type = Type.FREE;
            private int flatAmount = 0; // KRW
            private double ratio = 0.0; // 0.5 = 50%

            public enum Type { FREE, FLAT, RATIO }
        }
    }
}
