package cl.duoc.campuslab.notify.messaging.consumer;

import cl.duoc.campuslab.notify.messaging.dto.BookingEventMessage;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class EmailConsumer {

    @RabbitListener(
            queues = "${campuslab.rabbitmq.queues.email}",
            ackMode = "MANUAL"
    )
    public void consumeEmail(
            BookingEventMessage event,
            Message message,
            Channel channel
    ) throws IOException {

        long deliveryTag =
                message.getMessageProperties().getDeliveryTag();

        try {
            System.out.println("EMAIL recibido:");
            System.out.println("Reserva ID: " + event.getBookingId());
            System.out.println("Estado: " + event.getStatus());
            System.out.println("Tipo: " + event.getType());
            System.out.println("Mensaje: " + event.getMessage());
            System.out.println("Event ID: " + event.getEventId());
            System.out.println("Trace ID: " + event.getTraceId());
            System.out.println("Correlation ID: " + event.getCorrelationId());
            System.out.println("Timestamp: " + event.getTimestamp());

            channel.basicAck(deliveryTag, false);

            System.out.println("Email procesado correctamente. ACK enviado.");

        } catch (Exception e) {

            System.err.println(
                    "Error procesando email: " + e.getMessage()
            );

            channel.basicNack(
                    deliveryTag,
                    false,
                    false
            );

            System.err.println(
                    "NACK enviado. Mensaje derivado a DLQ."
            );
        }
    }
}