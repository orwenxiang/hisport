package com.orwen.hisport.artemis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hikvision.artemis.sdk.ArtemisHttpUtil;
import com.orwen.hisport.artemis.model.*;
import com.orwen.hisport.autoconfigure.HisPortProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
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

    private HisPortProperties.HikVisionArtemisConfig artemisConfig;
    private ObjectMapper objectMapper;

    @PostConstruct
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

    @SneakyThrows
    public void syncDepart(List<ArtemisDepartDTO> departs) {
        ArtemisResponse<Void> response = doRequest("/orgSync", departs, new TypeReference<>() {
        });
        checkResponse(response, departs, "Failed to sync department ");
    }

    @SneakyThrows
    public void staffJoin(ArtemisStaffDTO staffDTO) {
        ArtemisResponse<Void> response = doRequest("/insiderAdd", staffDTO, new TypeReference<>() {
        });
        checkResponse(response, staffDTO, "Failed to sync staff join ");
    }

    @SneakyThrows
    public void staffLeave(ArtemisLeaveDTO leaveHospitalDTO) {
        ArtemisResponse<Void> response = doRequest("/insiderDel", leaveHospitalDTO, new TypeReference<>() {
        });
        checkResponse(response, leaveHospitalDTO, "Failed to sync staff leave ");
    }

    @SneakyThrows
    public void patientJoin(ArtemisPatientDTO patientDTO) {
        ArtemisResponse<Void> response = doRequest("/patientAdd", patientDTO, new TypeReference<>() {
        });
        checkResponse(response, patientDTO, "Failed to sync patient join ");
    }

    @SneakyThrows
    public void patientLeave(ArtemisLeaveDTO leaveHospitalDTO) {
        ArtemisResponse<Void> response = doRequest("/patientDel", leaveHospitalDTO, new TypeReference<>() {
        });
        checkResponse(response, leaveHospitalDTO, "Failed to sync patient leave ");
    }

    @SneakyThrows
    public void patientTransfer(ArtemisTransferDTO transferDTO) {
        ArtemisResponse<Void> response = doRequest("/patientTransfer", transferDTO, new TypeReference<>() {
        });
        checkResponse(response, transferDTO, "Failed to sync patient transfer ");
    }

    @SneakyThrows
    public void careJoin(ArtemisCareDTO careDTO) {
        ArtemisResponse<Void> response = doRequest("/phAdd", careDTO, new TypeReference<>() {
        });
        checkResponse(response, careDTO, "Failed to sync care join ");
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
                objectMapper.writeValueAsString(body), null, null, CONTENT_TYPE, null);
        log.debug("Do hikvision artemis request with response {}", result);
        if (typeReference == null) {
            return null;
        }
        return objectMapper.readValue(result, typeReference);
    }

    private Map<String, String> withRequestPath(String prefix) {
        return Map.of(artemisConfig.getSchema(), artemisConfig.getHisMsPrefix() + prefix);
    }
}
