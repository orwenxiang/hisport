package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.orwen.hisport.hxhis.model.HxHisLevel7DepartDTO;
import com.orwen.hisport.hxhis.model.common.HxHisHeader;
import com.orwen.hisport.hxhis.model.request.HxHisRequest;
import com.orwen.hisport.hxhis.model.request.misc.HxHisLevel7DepartWrapper;
import lombok.SneakyThrows;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.List;

public class Main {
    @SneakyThrows
    public static void main(String[] args) {
        Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder = new Jackson2ObjectMapperBuilder();
        ObjectMapper objectMapper = jackson2ObjectMapperBuilder.createXmlMapper(true).build();

        ObjectWriter objectWriter = objectMapper.writer().withDefaultPrettyPrinter();

        HxHisRequest<HxHisLevel7DepartWrapper> hisRequest = new HxHisRequest<>();
        hisRequest.setHeader(new HxHisHeader());
        hisRequest.setBody(new HxHisLevel7DepartWrapper(List.of(new HxHisLevel7DepartDTO())));

        System.out.println(objectWriter.writeValueAsString(hisRequest));

    }
}
