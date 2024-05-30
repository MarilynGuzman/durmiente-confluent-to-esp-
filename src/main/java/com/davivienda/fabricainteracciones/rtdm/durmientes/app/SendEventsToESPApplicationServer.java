package com.davivienda.fabricainteracciones.rtdm.durmientes.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * Inicia la aplicación que toma las tramas de Kafka para enviarlas a ESP
 */
@ImportResource("classpath:camel-context.xml")
@SpringBootApplication
public class SendEventsToESPApplicationServer {

    public static void main(String[] args) {

        SpringApplication.run(SendEventsToESPApplicationServer.class, args);
    }

}