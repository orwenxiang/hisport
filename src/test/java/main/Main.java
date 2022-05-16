package main;

import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Main {
    private static final String RECEIVE_KEY = "Receive notify content ";
    private static final HttpHeaders REQUEST_HEADERS = HttpHeaders.readOnlyHttpHeaders(new LinkedMultiValueMap<>(Map
            .of(HttpHeaders.CONTENT_TYPE, List.of("text/xml;charset=UTF-8"))));

    @SneakyThrows
    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(List.of(new StringHttpMessageConverter(StandardCharsets.UTF_8)));

        Files.lines(Path.of("D:\\temp\\wrapper.log")).filter(line -> line.contains(RECEIVE_KEY))
                .map(line -> line.substring(line.indexOf(RECEIVE_KEY) + RECEIVE_KEY.length()))
                .forEach(line -> {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    restTemplate.postForEntity("http://127.0.0.1:7180/hxhis/mdm/notify",
                            new HttpEntity<>(line, REQUEST_HEADERS), String.class);
                });
    }
}
