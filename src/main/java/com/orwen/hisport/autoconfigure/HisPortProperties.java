package com.orwen.hisport.autoconfigure;

import com.hikvision.artemis.sdk.config.ArtemisConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Calendar;
import java.util.Date;

@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "hisport")
public class HisPortProperties {
    private HikVisionArtemisConfig artemis = new HikVisionArtemisConfig();
    private HxHisConfig hxHis = new HxHisConfig();

    @Getter
    @Setter
    @ToString
    public static class HxHisConfig {
        private Integer pullWeight = 2;
        private Duration patientPullRate = Duration.ofMinutes(10);
        private Date latestPullAt = new Date(2000 - 1900, Calendar.JANUARY, 1);
    }


    public static class HikVisionArtemisConfig extends ArtemisConfig {
        private String host;
        private String appKey;
        private String appSecret;
        @Getter
        @Setter
        private String schema = "https://";
        @Getter
        @Setter
        private String hisMsPrefix = "/artemis/api/v1/hisms";

        @Override
        public String getHost() {
            return super.getHost();
        }

        @Override
        public void setHost(String host) {
            super.setHost(host);
        }

        @Override
        public String getAppKey() {
            return super.getAppKey();
        }

        @Override
        public void setAppKey(String appKey) {
            super.setAppKey(appKey);
        }

        @Override
        public String getAppSecret() {
            return super.getAppSecret();
        }

        @Override
        public void setAppSecret(String appSecret) {
            super.setAppSecret(appSecret);
        }
    }
}
