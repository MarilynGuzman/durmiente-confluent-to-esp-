package com.davivienda.fabricainteracciones.rtdm.durmientes.app.utilis;


import java.time.*;

public final class Utilities {

    /**
     * Método que permite identificar transacciones ejecutadas en la misma fecha del sistema,
     * esto para evitar enviar inncesariamente la trama al flujo de ESP cuando ocurren encolamientos de eventos
     * en Kafka.
     *
     * @param trama transacción proveniente de stratus.
     * @return true si la transacción tiene fecha yyMMdd igual al sistema; false en caso contrario.
     */
    public static Boolean isKafkaAllowed(String trama) {
        String fechaTransaccion = trama.substring(91, 99).trim();
        LocalDate dateNow = LocalDate.now();
        String strDateNow = dateNow.toString().trim().replace("-", "");

        return fechaTransaccion.equals(strDateNow);
    }

}