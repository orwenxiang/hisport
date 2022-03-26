package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.orwen.hisport.hxhis.puller.AbstractHxHisPatientPuller;
import lombok.SneakyThrows;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.Duration;
import java.util.Date;

public class Main {
    @SneakyThrows
    public static void main(String[] args) {
        Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder = new Jackson2ObjectMapperBuilder();
        ObjectMapper objectMapper = jackson2ObjectMapperBuilder.createXmlMapper(false).build();

        ObjectWriter objectWriter = objectMapper.writer().withDefaultPrettyPrinter();


        System.out.println(objectWriter.writeValueAsString(new AbstractHxHisPatientPuller.PullRange(new Date(), Duration.ofMinutes(10))));

    }
}
