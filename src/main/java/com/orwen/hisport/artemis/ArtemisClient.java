package com.orwen.hisport.artemis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hikvision.artemis.sdk.ArtemisHttpUtil;
import com.orwen.hisport.artemis.model.*;
import com.orwen.hisport.autoconfigure.HisPortProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.lang.Nullable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

@Slf4j
@Component
public class ArtemisClient {
    private static final String CONTENT_TYPE = "application/json";
    private static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

    @Autowired
    private HisPortProperties properties;
    @Autowired
    private Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;
    @Autowired
    private JacksonProperties jacksonProperties;
    @Autowired(required = false)
    @Qualifier("artemisRetryTemplate")
    private RetryTemplate artemisRetryTemplate;

    private HisPortProperties.HikVisionArtemisConfig artemisConfig;
    private ObjectMapper objectMapper;

    @PostConstruct
    @Transactional
    void initialize() {
        artemisConfig = properties.getArtemis();

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        TimeZone timeZone = this.jacksonProperties.getTimeZone();
        if (timeZone == null) {
            timeZone = new ObjectMapper().getSerializationConfig().getTimeZone();
        }
        dateFormat.setTimeZone(timeZone);
        jackson2ObjectMapperBuilder.dateFormat(dateFormat);
        objectMapper = jackson2ObjectMapperBuilder.build();
    }

    public void departChanged(ArtemisDepartDTO artemisDepart) {
        syncDepart(List.of(artemisDepart));
    }

    @SneakyThrows
    protected synchronized void syncDepart(Collection<ArtemisDepartDTO> departs) {
        doWithRetry(() -> {
            ArtemisResponse<Void> response = doRequest("/orgSync", departs, new TypeReference<>() {
            });
            checkResponse(response, departs, "Failed to sync department");
            log.debug("Success access artemis to department sync with department is {}", departs);
        });
    }


    public void staffJoin(ArtemisStaffDTO staffDTO) {
        doWithRetry(() -> {
            ArtemisResponse<Void> response = doRequest("/insiderAdd", staffDTO, new TypeReference<>() {
            });
            checkResponse(response, staffDTO, "Failed to sync staff join");
            log.debug("Success access artemis to staff join with body {}", staffDTO);
        });
    }


    public void staffLeave(ArtemisLeaveDTO leaveHospitalDTO) {
        doWithRetry(() -> {
            ArtemisResponse<Void> response = doRequest("/insiderDel", leaveHospitalDTO, new TypeReference<>() {
            });
            checkResponse(response, leaveHospitalDTO, "Failed to sync staff leave ");
            log.debug("Success access artemis to staff leave with body {}", leaveHospitalDTO);
        });
    }


    public void patientJoin(ArtemisPatientDTO patientDTO) {
        doWithRetry(() -> {
            ArtemisResponse<Void> response = doRequest("/patientAdd", patientDTO, new TypeReference<>() {
            });
            checkResponse(response, patientDTO, "Failed to sync patient join");
            log.debug("Success access artemis to patient join with body {}", patientDTO);
        });
    }


    public void patientLeave(ArtemisLeaveDTO leaveHospitalDTO) {
        doWithRetry(() -> {
            ArtemisResponse<Void> response = doRequest("/patientDel", leaveHospitalDTO, new TypeReference<>() {
            });
            checkResponse(response, leaveHospitalDTO, "Failed to sync patient leave ");
            log.debug("Success access artemis to patient leave with body {}", leaveHospitalDTO);
        });
    }


    public void patientTransfer(ArtemisTransferDTO transferDTO) {
        doWithRetry(() -> {
            ArtemisResponse<Void> response = doRequest("/patientTransfer", transferDTO, new TypeReference<>() {
            });
            checkResponse(response, transferDTO, "Failed to sync patient transfer");
            log.debug("Success access artemis to patient transfer with body {}", transferDTO);
        });
    }


    public void careJoin(ArtemisCareDTO careDTO) {
        doWithRetry(() -> {
            ArtemisResponse<Void> response = doRequest("/phAdd", careDTO, new TypeReference<>() {
            });
            checkResponse(response, careDTO, "Failed to sync care join ");
            log.debug("Success access artemis to care join with body {}", careDTO);
        });
    }

    @SneakyThrows
    protected void checkResponse(ArtemisResponse response, Object body, String msgPrefix) {
        if (!response.isSuccess()) {
            throw new RuntimeException(msgPrefix + objectMapper.writeValueAsString(body) + " with message "
                    + response.getMessage() + "  and code " + response.getCode());
        }
    }

    @SneakyThrows
    protected <T> T doRequest(String pathPrefix, Object body, @Nullable TypeReference<T> typeReference) {
        log.debug("Do hikvision artemis request with prefix {} and body {}", pathPrefix, body);
        String result = ArtemisHttpUtil.doPostStringArtemis(withRequestPath(pathPrefix),
                objectMapper.writeValueAsString(body), null, CONTENT_TYPE, CONTENT_TYPE, null);
        log.debug("Do hikvision artemis request with response {}", result);
        if (typeReference == null) {
            return null;
        }
        return objectMapper.readValue(result, typeReference);
    }

    private void doWithRetry(Runnable runnable) {
        try {
            if (artemisRetryTemplate == null) {
                runnable.run();
                return;
            }
            artemisRetryTemplate.execute(context -> {
                runnable.run();
                return null;
            });
        } catch (Throwable e) {
            log.warn("Failed access artemis request", e);
        }
    }

    private Map<String, String> withRequestPath(String prefix) {
        return Map.of(artemisConfig.getSchema(), artemisConfig.getHisMsPrefix() + prefix);
    }
}
