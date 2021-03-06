package com.orwen.hisport.hxhis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.orwen.hisport.autoconfigure.HisPortProperties;
import com.orwen.hisport.dispatcher.HisPortDispatcher;
import com.orwen.hisport.hxhis.dbaccess.HxHisRecordPO;
import com.orwen.hisport.hxhis.model.HxHisLevel7DepartDTO;
import com.orwen.hisport.hxhis.model.HxHisNormalDepartDTO;
import com.orwen.hisport.hxhis.model.common.HxHisCommonDepartDTO;
import com.orwen.hisport.hxhis.model.common.HxHisHeader;
import com.orwen.hisport.hxhis.model.request.HxHisRequest;
import com.orwen.hisport.hxhis.model.request.misc.HxHisLevel7DepartWrapper;
import com.orwen.hisport.hxhis.model.request.misc.HxHisNormalDepartWrapper;
import com.orwen.hisport.hxhis.model.request.misc.HxHisStaffWrapper;
import com.orwen.hisport.hxhis.model.response.HxHisResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping("/hxhis/mdm/notify")
public class HxHisMdmCallback {
    private static final ThreadLocal<HxHisHeader> HEADER_HOLDER = ThreadLocal.withInitial(() -> null);
    @Autowired
    private Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;
    @Autowired
    private HxHisRecordService recordService;
    @Autowired
    private HisPortDispatcher dispatcher;
    @Autowired
    private HisPortProperties properties;

    private XmlMapper objectMapper;


    @PostConstruct
    void initialize() {
        this.objectMapper = jackson2ObjectMapperBuilder.createXmlMapper(true).build();
    }

    @SneakyThrows
    @ExceptionHandler
    public String exceptionHandler(Throwable e) {
        log.error("Failed process mdm notify", e);
        return objectMapper.writeValueAsString(HxHisResponse.failed(HEADER_HOLDER.get()));
    }

    @SneakyThrows
    @PostMapping
    @Transactional
    public String notify(HttpServletRequest request) {
        HEADER_HOLDER.remove();
        InputStream httpInputStream = request.getInputStream();
        if (httpInputStream.available() < 1) {
            log.error("No input stream for notify");
            return objectMapper.writeValueAsString(HxHisResponse.failed(null));
        }
        String content = StreamUtils.copyToString(httpInputStream, StandardCharsets.UTF_8);

        log.debug("Receive notify content {}", content);

        HxHisRequest<Map> rawNotifyObject = objectMapper.readValue(content, new TypeReference<>() {
        });

        if (!properties.isProcessMdmCallback()) {
            log.warn("Not process mdm callback");
            return objectMapper.writeValueAsString(HxHisResponse.success(rawNotifyObject.getHeader()));
        }

        HEADER_HOLDER.set(rawNotifyObject.getHeader());

        Map notifyBody = rawNotifyObject.getBody();
        if (ObjectUtils.isEmpty(notifyBody)) {
            log.error("The notify body is empty with raw content {}", content);
            return objectMapper.writeValueAsString(HxHisResponse.failed(rawNotifyObject.getHeader()));
        }

        String notifyType = guessNotifyType(notifyBody);
        if (!StringUtils.hasText(notifyType)) {
            log.warn("The notify type is empty for raw content {}", content);
            return objectMapper.writeValueAsString(HxHisResponse.success(rawNotifyObject.getHeader()));
        }

        HxHisRecordPO hisRecordPO = new HxHisRecordPO();
        hisRecordPO.setType(notifyType);
        hisRecordPO.setContent(content);
        hisRecordPO.setPullAt(new Date());
        hisRecordPO.setDispatched(true);
        switch (notifyType) {
            case HxHisStaffWrapper.TYPE -> processStaffNotifies(content);
            case HxHisLevel7DepartWrapper.TYPE -> processLevel7DepartNotifies(content);
            case HxHisNormalDepartWrapper.TYPE -> processNormalDepartNotifies(content);
            default -> {
                hisRecordPO.setDispatched(false);
                log.debug("Will not dispatch notify {}", notifyType);
            }
        }
        recordService.storeRecord(hisRecordPO);
        log.debug("Done process notify content {}", content);
        return objectMapper.writeValueAsString(HxHisResponse.success(rawNotifyObject.getHeader()));
    }

    @SneakyThrows
    private void processNormalDepartNotifies(String content) {
        HxHisRequest<HxHisNormalDepartWrapper> hisNormalDepartWrapper = objectMapper.readValue(content, new TypeReference<>() {
        });
        if (CollectionUtils.isEmpty(hisNormalDepartWrapper.getBody().getContents())) {
            log.warn("The depart normal(level 6) notifies content is empty for raw content {}", content);
            return;
        }
        hisNormalDepartWrapper.getBody().getContents().forEach(dispatcher::departChanged);
    }

    @SneakyThrows
    private void processLevel7DepartNotifies(String content) {
        HxHisRequest<HxHisLevel7DepartWrapper> hisLevel7DepartWrapper = objectMapper.readValue(content, new TypeReference<>() {
        });
        if (CollectionUtils.isEmpty(hisLevel7DepartWrapper.getBody().getContents())) {
            log.warn("The depart level 7 notifies content is empty for raw content {}", content);
            return;
        }
        hisLevel7DepartWrapper.getBody().getContents().stream()
                .flatMap(this::convertDepart).forEach(dispatcher::departChanged);
    }

    private Stream<HxHisNormalDepartDTO> convertDepart(HxHisLevel7DepartDTO departDTO) {
        List<HxHisNormalDepartDTO> departs = new ArrayList<>();

        HxHisNormalDepartDTO currentDepart = new HxHisNormalDepartDTO();
        currentDepart.setId(departDTO.getFourCode());
        currentDepart.setName(departDTO.getFourName());
        currentDepart.setParent(null);
        currentDepart.setStatus(HxHisCommonDepartDTO.ENABLE_CODE);

        departs.add(currentDepart);

        currentDepart = new HxHisNormalDepartDTO();
        currentDepart.setId(departDTO.getFiveCode());
        currentDepart.setName(departDTO.getFiveName());
        currentDepart.setParent(departDTO.getFourCode());
        currentDepart.setStatus(HxHisCommonDepartDTO.ENABLE_CODE);

        departs.add(currentDepart);

        currentDepart = new HxHisNormalDepartDTO();
        currentDepart.setId(departDTO.getParent());
        currentDepart.setName(departDTO.getParentName());
        currentDepart.setParent(departDTO.getFiveCode());
        currentDepart.setStatus(HxHisCommonDepartDTO.ENABLE_CODE);

        departs.add(currentDepart);


        currentDepart = new HxHisNormalDepartDTO();
        currentDepart.setId(departDTO.getId());
        currentDepart.setName(departDTO.getName());
        currentDepart.setParent(departDTO.getParent());
        currentDepart.setStatus(HxHisCommonDepartDTO.ENABLE_CODE);

        currentDepart.setValidStart(departDTO.getValidStart());
        currentDepart.setValidEnd(departDTO.getValidEnd());

        departs.add(currentDepart);

        return departs.stream();
    }

    @SneakyThrows
    protected void processStaffNotifies(String content) {
        HxHisRequest<HxHisStaffWrapper> hisStaffWrapper = objectMapper.readValue(content, new TypeReference<>() {
        });
        if (CollectionUtils.isEmpty(hisStaffWrapper.getBody().getContents())) {
            log.warn("The staff notifies content is empty for raw content {}", content);
            return;
        }
        hisStaffWrapper.getBody().getContents().forEach(dispatcher::staffChanged);
    }

    protected String guessNotifyType(Map notifyBody) {
        return String.valueOf(notifyBody.keySet().iterator().next())
                .replace("List", "");
    }

}
