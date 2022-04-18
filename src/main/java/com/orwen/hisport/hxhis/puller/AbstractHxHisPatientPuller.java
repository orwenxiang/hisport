package com.orwen.hisport.hxhis.puller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.orwen.hisport.autoconfigure.HisPortProperties;
import com.orwen.hisport.dispatcher.HisPortDispatcher;
import com.orwen.hisport.hxhis.HxHisRecordService;
import com.orwen.hisport.hxhis.dbaccess.HxHisRecordPO;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Getter
public abstract class AbstractHxHisPatientPuller {
    private static final String WS_SOAP_MESSAGE_TEMPLATE = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:int=\"http://hospital.service.com/interface\">\n" +
            "   <soapenv:Header/>\n" +
            "   <soapenv:Body>\n" +
            "      <int:InvokeToString>\n" +
            "         <int:Method>REQUEST_METHOD</int:Method>\n" +
            "         <int:DataString><![CDATA[REQUEST_BODY]]></int:DataString>\n" +
            "      </int:InvokeToString>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";
    private static final HttpHeaders DEFAULT_MODIFY_HEADERS = HttpHeaders.readOnlyHttpHeaders(new HttpHeaders(new LinkedMultiValueMap<>(
            Map.of("Content-Type", List.of("text/xml;charset=UTF-8"),
                    "SOAPAction", List.of("\"http://hospital.service.com/interface/Huaxi.InvokeMessage.BS.InvokeService.InvokeToString\"")))));

    private static final String REQUEST_METHOD = "REQUEST_METHOD";
    private static final String REQUEST_BODY = "REQUEST_BODY";
    private static final String RETURN_START_KEY = "<InvokeToStringResult><![CDATA[";
    private static final String RETURN_END_KEY = "]]></InvokeToStringResult>";
    @Autowired
    private HisPortProperties properties;

    @Autowired
    protected HisPortDispatcher dispatcher;

    @Autowired
    protected HxHisRecordService recordService;

    @Autowired
    @Qualifier("patientPullerRestTemplate")
    private RestTemplate patientPullerRestTemplate;

    @Autowired
    @Qualifier("patientPullRate")
    private RRateLimiter patientPullRate;

    @Autowired
    private ObjectMapper objectMapper;

    public void pull(PullRange pullRange) {
        try {
            log.debug("Doing {} pull with range {}", getClass().getSimpleName(), pullRange);
            doPull(pullRange);
            log.debug("Do {} pull with range {} success", getClass().getSimpleName(), pullRange);
        } catch (Throwable e) {
            log.warn("Do {} pull with range {} failed", getClass().getSimpleName(), pullRange, e);
        }
    }

    protected abstract void doPull(PullRange pullRange);

    @SneakyThrows
    protected <T> List<T> retrievePatientContent(String methodCode, PullRange pullRange, TypeReference<T> typeReference) {
        String response = retrievePatientContent(methodCode, objectMapper.writeValueAsString(pullRange));
        if (!StringUtils.hasText(response)) {
            return Collections.emptyList();
        }
        PullResponse<ObjectNode> pullResponse = objectMapper.readValue(response, new TypeReference<>() {
        });
        if (!pullResponse.isSuccess()) {
            throw new RuntimeException("Failed to pull patient with message " + pullResponse.getMessage());
        }
        return pullResponse.getContents().stream().map(content -> {
            try {
                return objectMapper.writeValueAsBytes(content);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("failed write content as bytes");
            }
        }).map(content -> {
            try {
                return objectMapper.readValue(content, typeReference);
            } catch (IOException e) {
                throw new RuntimeException("failed read values to type " + typeReference);
            }
        }).collect(Collectors.toList());
    }

    private String retrievePatientContent(String methodCode, String requestStr) {
        String requestBody = WS_SOAP_MESSAGE_TEMPLATE.replace(REQUEST_METHOD, methodCode).replace(REQUEST_BODY, requestStr);

        patientPullRate.acquire();

        log.debug("Do patient pull with request body {}", requestBody);

        ResponseEntity<String> responseEntity = patientPullerRestTemplate.exchange(properties.getPull().getEndpoint(), HttpMethod.POST,
                new HttpEntity<>(requestBody, DEFAULT_MODIFY_HEADERS), new ParameterizedTypeReference<>() {
                });

        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            log.warn("Failed request pull his patient with method {} and request string {}", methodCode, requestStr);
            return null;
        }
        String rawResponseBody = responseEntity.getBody();
        log.debug("Retrieve patient response by method code {} and request pull range {}  with raw body {}",
                methodCode, requestStr, rawResponseBody);
        if (rawResponseBody == null || !rawResponseBody.contains(RETURN_START_KEY) || !rawResponseBody.contains(RETURN_END_KEY)) {
            log.warn("The response not contain return {} and {} field", RETURN_START_KEY, RETURN_END_KEY);
            return null;
        }
        return rawResponseBody.substring(rawResponseBody.indexOf(RETURN_START_KEY) + RETURN_START_KEY.length(),
                rawResponseBody.indexOf(RETURN_END_KEY));
    }

    @SneakyThrows
    protected <T> void storeRecord(T content, Boolean dispatched, Date pullAt) {
        HxHisRecordPO hisRecordPO = new HxHisRecordPO();
        hisRecordPO.setType(content.getClass().getName());
        hisRecordPO.setDispatched(dispatched);
        hisRecordPO.setPullAt(pullAt);
        hisRecordPO.setContent(objectMapper.writeValueAsString(content));
        recordService.storeRecord(hisRecordPO);
    }

    @Getter
    @Setter
    @ToString
    private static class PullResponse<T> {
        @JsonProperty("resultCode")
        private String code;
        @JsonProperty("errorMsg")
        private String message;
        @JsonProperty("data")
        private List<T> contents;

        @JsonIgnore
        public boolean isSuccess() {
            return Objects.equals(code, "0");
        }
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString(of = {"startDate", "endDate"})
    public static class PullRange implements Serializable {
        private static final long serialVersionUID = 1L;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date startDate;
        @JsonFormat(pattern = "HH:mm:ss")
        private Date startTime;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date endDate;
        @JsonFormat(pattern = "HH:mm:ss")
        private Date endTime;

        public PullRange(Date startAt, Duration duration) {
            Calendar start = Calendar.getInstance();
            start.setTime(startAt);
            start.add(Calendar.SECOND, -1);

            this.startDate = start.getTime();
            this.startTime = this.startDate;

            this.endDate = new Date(startDate.getTime() + duration.toMillis());
            this.endTime = this.endDate;
        }

        @JsonIgnore
        public boolean isBiggerThanNow() {
            return startDate.after(new Date()) || endDate.after(new Date());
        }

        @JsonIgnore
        public PullRange nextDuration(Duration duration) {
            return new PullRange(endDate, duration);
        }
    }
}
