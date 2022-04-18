package com.orwen.hisport;

import com.orwen.hisport.hxhis.HxHisPatientRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class Bootstrap {
    public static void main(String[] args) {
        SpringApplication.run(Bootstrap.class, args);
    }

    @Autowired
    private HxHisPatientRetriever patientRetriever;

    @EventListener(ApplicationReadyEvent.class)
    void onReady(ApplicationReadyEvent event) {
        if (event.getApplicationContext() instanceof AnnotationConfigServletWebServerApplicationContext) {
            patientRetriever.scheduleSelectDoPullInstance();
        }
    }
}

