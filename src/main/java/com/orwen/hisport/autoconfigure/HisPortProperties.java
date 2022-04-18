package com.orwen.hisport.autoconfigure;

import com.hikvision.artemis.sdk.config.ArtemisConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "hisport")
public class HisPortProperties {
    private HikVisionArtemisConfig artemis = new HikVisionArtemisConfig();
    private HxHisPatientPullConfig pull = new HxHisPatientPullConfig();

    @Getter
    @Setter
    @ToString
    public static class HxHisPatientPullConfig {
        private String endpoint = "http://172.22.252.46/csp/huaxi/Huaxi.InvokeMessage.BS.InvokeService.CLS?WSDL";
        private Integer weight = 2;
        private Duration range = Duration.ofMinutes(10);
        private Integer rateInSecond = 8;
        private String latestAt = "2020-01-01";
        private String maxPullAt;
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
