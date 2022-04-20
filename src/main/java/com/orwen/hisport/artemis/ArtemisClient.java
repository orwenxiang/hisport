package com.orwen.hisport.artemis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hikvision.artemis.sdk.ArtemisHttpUtil;
import com.orwen.hisport.artemis.dbaccess.ArtemisDepartPO;
import com.orwen.hisport.artemis.dbaccess.QArtemisDepartPO;
import com.orwen.hisport.artemis.dbaccess.repository.ArtemisDepartRepository;
import com.orwen.hisport.artemis.model.*;
import com.orwen.hisport.autoconfigure.HisPortProperties;
import com.orwen.hisport.defs.HxPortDefs;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLocalCachedMap;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.lang.Nullable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ArtemisClient {
    private static final QArtemisDepartPO qArtemisDepart = QArtemisDepartPO.artemisDepartPO;
    private static final String CONTENT_TYPE = "application/json";
    private static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

    @Autowired
    private HisPortProperties properties;
    @Autowired
    private Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;
    @Autowired
    private JacksonProperties jacksonProperties;
    @Autowired
    private ArtemisDepartRepository departs;

    @Autowired
    @Qualifier("artemisDepartCache")
    private RLocalCachedMap<String, ArtemisDepartPO> departCache;

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

        if (CollectionUtils.isEmpty(departCache)) {
            Lock loadDataLock = departCache.getLock("load_data_lock");
            if (loadDataLock.tryLock()) {
                try {
                    departs.findAll().forEach(item -> departCache.putAsync(item.getDepartId(), item));
                } finally {
                    loadDataLock.unlock();
                }
            }
        }
    }

    @SneakyThrows
    @RabbitListener(queuesToDeclare = @Queue(HxPortDefs.DEPART_CHANGED_QUEUE), concurrency = "1", ackMode = "MANUAL")
    public void departChanged(ArtemisDepartPO artemisDepart, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        if (!Objects.equals(departCache.put(artemisDepart.getDepartId(), artemisDepart), artemisDepart)) {
            ArtemisDepartPO departPO = departs.findOne(qArtemisDepart.departId.eq(artemisDepart.getDepartId()))
                    .orElseGet(() -> {
                        ArtemisDepartPO artemisDepartPO = new ArtemisDepartPO();
                        artemisDepartPO.setDepartId(artemisDepart.getDepartId());
                        return artemisDepartPO;
                    });
            departPO.setName(artemisDepart.getName());
            departPO.setParentId(artemisDepart.getParentId());
            departPO.setEnabled(artemisDepart.getEnabled());
            departs.save(departPO);
            syncDepart(departCache.values().stream().
                    filter(ArtemisDepartPO::getEnabled).collect(Collectors.toList()));
        }
        channel.basicAck(tag, false);
    }

    @SneakyThrows
    protected synchronized void syncDepart(Collection<ArtemisDepartPO> departs) {
        doWithRetry(() -> {
            ArtemisResponse<Void> response = doRequest("/orgSync", departs, new TypeReference<>() {
            });
            checkResponse(response, departs, "Failed to sync department");
            log.debug("Success access artemis to department sync with department is {}", departs.size());
        });
    }

    @SneakyThrows
    @RabbitListener(queuesToDeclare = @Queue(HxPortDefs.STAFF_JOINED_QUEUE), concurrency = "1", ackMode = "MANUAL")
    public void staffJoin(ArtemisStaffDTO staffDTO, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        doWithRetry(() -> {
            ArtemisResponse<Void> response = doRequest("/insiderAdd", staffDTO, new TypeReference<>() {
            });
            checkResponse(response, staffDTO, "Failed to sync staff join");
            log.debug("Success access artemis to staff join with body {}", staffDTO);
        });
        channel.basicAck(tag, false);
    }

    @SneakyThrows
    @RabbitListener(queuesToDeclare = @Queue(HxPortDefs.STAFF_LEAVED_QUEUE), concurrency = "1", ackMode = "MANUAL")
    public void staffLeave(ArtemisLeaveDTO leaveHospitalDTO, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        doWithRetry(() -> {
            ArtemisResponse<Void> response = doRequest("/insiderDel", leaveHospitalDTO, new TypeReference<>() {
            });
            checkResponse(response, leaveHospitalDTO, "Failed to sync staff leave ");
            log.debug("Success access artemis to staff leave with body {}", leaveHospitalDTO);
        });
        channel.basicAck(tag, false);
    }

    @SneakyThrows
    @RabbitListener(queuesToDeclare = @Queue(HxPortDefs.PATIENT_JOINED_QUEUE), concurrency = "1", ackMode = "MANUAL")
    public void patientJoin(ArtemisPatientDTO patientDTO, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        doWithRetry(() -> {
            ArtemisResponse<Void> response = doRequest("/patientAdd", patientDTO, new TypeReference<>() {
            });
            checkResponse(response, patientDTO, "Failed to sync patient join");
            log.debug("Success access artemis to patient join with body {}", patientDTO);
        });
        channel.basicAck(tag, false);
    }

    @SneakyThrows
    @RabbitListener(queuesToDeclare = @Queue(HxPortDefs.PATIENT_LEAVED_QUEUE), concurrency = "1", ackMode = "MANUAL")
    public void patientLeave(ArtemisLeaveDTO leaveHospitalDTO, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        doWithRetry(() -> {
            ArtemisResponse<Void> response = doRequest("/patientDel", leaveHospitalDTO, new TypeReference<>() {
            });
            checkResponse(response, leaveHospitalDTO, "Failed to sync patient leave ");
            log.debug("Success access artemis to patient leave with body {}", leaveHospitalDTO);
        });
        channel.basicAck(tag, false);
    }

    @SneakyThrows
    @RabbitListener(queuesToDeclare = @Queue(HxPortDefs.PATIENT_TRANSFER_QUEUE), concurrency = "1", ackMode = "MANUAL")
    public void patientTransfer(ArtemisTransferDTO transferDTO, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        doWithRetry(() -> {
            ArtemisResponse<Void> response = doRequest("/patientTransfer", transferDTO, new TypeReference<>() {
            });
            checkResponse(response, transferDTO, "Failed to sync patient transfer");
            log.debug("Success access artemis to patient transfer with body {}", transferDTO);
        });
        channel.basicAck(tag, false);
    }

    @SneakyThrows
    @RabbitListener(queuesToDeclare = @Queue(HxPortDefs.CARE_JOINED_QUEUE), concurrency = "1", ackMode = "MANUAL")
    public void careJoin(ArtemisCareDTO careDTO, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        doWithRetry(() -> {
            ArtemisResponse<Void> response = doRequest("/phAdd", careDTO, new TypeReference<>() {
            });
            checkResponse(response, careDTO, "Failed to sync care join ");
            log.debug("Success access artemis to care join with body {}", careDTO);
        });
        channel.basicAck(tag, false);
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
