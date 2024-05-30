package com.davivienda.fabricainteracciones.rtdm.durmientes.app.processor;

import com.davivienda.fabricainteracciones.rtdm.durmientes.app.exception.SendEventToESPException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.davivienda.leyintimidad.VerifyPrivacyLaw;
import com.davivienda.fabricainteracciones.rtdm.durmientes.app.utilis.Utilities;

import com.sas.esp.clients.camel.transforms.CsvTransform;
import com.sas.esp.clients.camel.util.Generator;
import org.springframework.beans.factory.annotation.Value;

/**
 * Procesa la trama recibida de Kafka para enviar solicitud hacia ESP
 */
public class EventProcessor implements Processor{

    @Value("${min.hour}")
    Integer minHour;

    @Value("${max.hour}")
    Integer maxHour;

    private static final String OPCODE = "i";
    private static final String DELIMITER = ",";
    private static final String SYMBOL = "n";
    private static final Logger logger = LoggerFactory.getLogger(EventProcessor.class);
    private static final int FRAME_LENGHT = 940;

    /**
     * Crea la solicitud de envío de eventos hacia ESP
     * @param exchange objeto de Camel compuesto que contiene propiedades almacenadas durante ejecución
     */
    @Override
    public void process(Exchange exchange) throws Exception {

        try {
            VerifyPrivacyLaw verifyPrivacyLaw = new VerifyPrivacyLaw(minHour, maxHour);
            String frame = exchange.getIn().getBody(String.class);
            Boolean privacyLawIsAllowed = verifyPrivacyLaw.isAllowed();
            Boolean isKafkaAllowed = Utilities.isKafkaAllowed(frame);

            if(frame != null && !frame.isEmpty() && privacyLawIsAllowed && isKafkaAllowed) {

                CsvTransform csvTransform = new CsvTransform();
                StringBuilder espFrame = new StringBuilder();
                String trackingEsp = Generator.getInstance().generateIdentifier();
                exchange.setProperty("Tracking", trackingEsp);

                if(frame.length() != FRAME_LENGHT) {
                    exchange.setProperty("InputFrameDiffLenght", frame);
                    String message = "FrameLenghtReceived: ".concat(String.valueOf(frame.length())).concat(" - Tracking: ").concat(trackingEsp).concat(" - Trama: ").concat(frame);
                    exchange.setProperty("InputFrameDiffLenghtMessage", message);
                }

                espFrame.append(OPCODE).append(DELIMITER);
                espFrame.append(SYMBOL).append(DELIMITER);
                espFrame.append(trackingEsp).append(DELIMITER);
                espFrame.append(exchange.getIn().getBody(String.class)).append(DELIMITER);

                csvTransform.setSchema("tracking_esp*:string,trama:string");
                Object obj = csvTransform.transform(espFrame);
                exchange.getOut().setBody(obj);
            }else {
                exchange.setProperty("isKafkaAllowed", !isKafkaAllowed);
                exchange.setProperty("privacyLawIsAllowed", !privacyLawIsAllowed);
                exchange.getOut().setBody(null);
            }
        }catch(Exception e) {
            throw new SendEventToESPException(e.getMessage());
        }
    }

}
