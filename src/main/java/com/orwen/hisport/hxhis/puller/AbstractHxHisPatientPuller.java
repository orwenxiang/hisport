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
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.ObjectUtils;
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
    private static final String TO_STRING_METHOD = "InvokeToString";
    private static final String TO_STREAM_METHOD = "InvokeToStream";

    private static final String WS_SOAP_MESSAGE_TEMPLATE = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:int=\"http://hospital.service.com/interface\">\n" +
            "   <soapenv:Header/>\n" +
            "   <soapenv:Body>\n" +
            "      <int:TO_METHOD>\n" +
            "         <int:Method>REQUEST_METHOD</int:Method>\n" +
            "         <int:DataString><![CDATA[REQUEST_BODY]]></int:DataString>\n" +
            "      </int:TO_METHOD>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";

    private static final HttpHeaders TO_STRING_HEADERS = HttpHeaders.readOnlyHttpHeaders(new HttpHeaders(new LinkedMultiValueMap<>(
            Map.of("Content-Type", List.of("text/xml;charset=UTF-8"),
                    "SOAPAction", List.of("\"http://hospital.service.com/interface/Huaxi.InvokeMessage.BS.InvokeService.InvokeToString\"")))));

    private static final HttpHeaders TO_STREAM_HEADERS = HttpHeaders.readOnlyHttpHeaders(new HttpHeaders(new LinkedMultiValueMap<>(
            Map.of("Content-Type", List.of("text/xml;charset=UTF-8"),
                    "SOAPAction", List.of("\"http://hospital.service.com/interface/Huaxi.InvokeMessage.BS.InvokeService.InvokeToStream\"")))));

    private static final String TO_METHOD = "TO_METHOD";
    private static final String REQUEST_METHOD = "REQUEST_METHOD";
    private static final String REQUEST_BODY = "REQUEST_BODY";

    private static final String CDATA_START = "<![CDATA[";
    private static final String CDATA_END = "]]>";

    private static final String RETURN_START_KEY_TO_STRING = "<InvokeToStringResult>";
    private static final String RETURN_END_KEY_TO_STRING = "</InvokeToStringResult>";

    private static final String RETURN_START_KEY_TO_STREAM = "<InvokeToStreamResult>";
    private static final String RETURN_END_KEY_TO_STREAM = "</InvokeToStreamResult>";

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
    protected <T> List<T> retrievePatientContent(boolean toString, String methodCode, PullRange pullRange, TypeReference<T> typeReference) {
        String response = retrievePatientContent(toString, methodCode, objectMapper.writeValueAsString(pullRange));
        if (!StringUtils.hasText(response)) {
            return Collections.emptyList();
        }
        PullResponse<ObjectNode> pullResponse = objectMapper.readValue(response, new TypeReference<>() {
        });
        if (!pullResponse.isSuccess()) {
            throw new RuntimeException("Failed to pull patient with message " + pullResponse.getMessage());
        }
        if (CollectionUtils.isEmpty(pullResponse.getContents())) {
            log.debug("Empty content for {} with method {} in range {}", getClass().getSimpleName(), methodCode, pullRange);
            return Collections.emptyList();
        }
        return pullResponse.getContents().stream().map(content -> {
            try {
                return objectMapper.writeValueAsBytes(content);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("failed write content as bytes", e);
            }
        }).map(content -> {
            try {
                return objectMapper.readValue(content, typeReference);
            } catch (IOException e) {
                throw new RuntimeException("failed read values to type " + typeReference, e);
            }
        }).collect(Collectors.toList());
    }

    private String retrievePatientContent(boolean toString, String methodCode, String requestStr) {
        String requestBody = WS_SOAP_MESSAGE_TEMPLATE.replace(TO_METHOD, toString ? TO_STRING_METHOD : TO_STREAM_METHOD)
                .replace(REQUEST_METHOD, methodCode).replace(REQUEST_BODY, requestStr);

        patientPullRate.acquire();

        ResponseEntity<String> responseEntity = patientPullerRestTemplate.exchange(properties.getPull().getEndpoint(), HttpMethod.POST,
                new HttpEntity<>(requestBody, toString ? TO_STRING_HEADERS : TO_STREAM_HEADERS), new ParameterizedTypeReference<>() {
                });

        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            log.warn("Failed request pull his patient with method {} and request string {}", methodCode, requestStr);
            return null;
        }

        String rawResponseBody = responseEntity.getBody();
        
        log.debug("Retrieve patient response by method code {} and request pull range {}  with raw body {}", methodCode, requestStr, rawResponseBody);

        return splitRawResponse(rawResponseBody, toString ? RETURN_START_KEY_TO_STRING : RETURN_START_KEY_TO_STREAM,
                toString ? RETURN_END_KEY_TO_STRING : RETURN_END_KEY_TO_STREAM);
    }

    private String splitRawResponse(String rawResponseBody, String startAt, String endAt) {
        if (!StringUtils.hasText(rawResponseBody)) {
            log.warn("The response {} is empty", rawResponseBody);
            return null;
        }
        String usingStartAt = startAt + CDATA_START;
        String usingEndAt = CDATA_END + endAt;
        if (rawResponseBody.contains(usingStartAt)) {
            return rawResponseBody.substring(rawResponseBody.indexOf(usingStartAt) + usingStartAt.length(),
                    rawResponseBody.indexOf(usingEndAt));
        }
        usingStartAt = startAt;
        usingEndAt = endAt;
        if (rawResponseBody.contains(usingStartAt)) {
            return rawResponseBody.substring(rawResponseBody.indexOf(usingStartAt) + usingStartAt.length(),
                    rawResponseBody.indexOf(usingEndAt));
        }
        log.warn("The response {} not contain return {} and {}", rawResponseBody, startAt, endAt);
        return null;
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
        private static final String NO_DATA_CODE_1 = "-102";
        private static final String NO_DATA_CODE_2 = "1";
        private static final String NO_DATA_CODE_3 = "-1";
        @JsonProperty("resultCode")
        private String code;

        @JsonProperty("errorMsg")
        private String message;
        @JsonProperty("data")
        private List<T> contents;

        @JsonIgnore
        public boolean isSuccess() {
            return Objects.equals(code, "0") || ObjectUtils.isEmpty(contents) &&
                    (Objects.equals(code, NO_DATA_CODE_1) || Objects.equals(code, NO_DATA_CODE_2)
                            || Objects.equals(code, NO_DATA_CODE_3));
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

            this.startDate = start.getTime();
            this.startTime = this.startDate;

            this.endDate = new Date(startDate.getTime() + duration.toMillis());
            this.endTime = this.endDate;
        }

        @JsonIgnore
        public boolean isBiggerThan(Date at) {
            return startDate.after(at) || endDate.after(at);
        }

        @JsonIgnore
        public PullRange nextDuration(Duration duration) {
            return new PullRange(endDate, duration);
        }
    }
}
