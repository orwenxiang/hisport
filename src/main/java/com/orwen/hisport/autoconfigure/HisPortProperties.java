package com.orwen.hisport.autoconfigure;

import com.hikvision.artemis.sdk.config.ArtemisConfig;
import com.orwen.hisport.artemis.enums.ArtemisRole;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "hisport")
public class HisPortProperties {
    private String onJobCode = "001001001001001";
    private boolean processMdmCallback = true;
    private boolean storeRecords = true;
    private boolean cleanRecords = true;
    private Duration cleanRecordIn = Duration.ofDays(60);
    private HikVisionArtemisConfig artemis = new HikVisionArtemisConfig();
    private HxHisPatientPullConfig pull = new HxHisPatientPullConfig();

    @Getter
    @Setter
    @ToString
    public static class HxHisPatientPullConfig {
        private boolean enabled = true;
        private String endpoint = "http://172.22.252.46/csp/huaxi/Huaxi.InvokeMessage.BS.InvokeService.CLS?WSDL";
        private Integer weight = 2;
        private Duration range = Duration.ofMinutes(10);
        private Duration extendIn = Duration.ofSeconds(10);
        private Integer rateInSecond = 8;
        private String latestAt = "2020-01-01";
        private String maxPullAt;
    }


    @ToString
    public static class HikVisionArtemisConfig extends ArtemisConfig {
        private String host;
        private String appKey;
        private String appSecret;
        @Getter
        @Setter
        private boolean enabled = true;
        @Getter
        @Setter
        private String schema = "https://";
        @Getter
        @Setter
        private String hisMsPrefix = "/artemis/api/v1/hisms";
        @Getter
        @Setter
        private RabbitProperties.Retry retry;
        @Getter
        @Setter
        private Map<ArtemisRole, List<String>> roleCodes;
        @Getter
        @Setter
        private boolean asyncNotify = false;

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
