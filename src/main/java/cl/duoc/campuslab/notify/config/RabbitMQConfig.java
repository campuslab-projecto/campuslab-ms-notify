package cl.duoc.campuslab.notify.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Value("${campuslab.rabbitmq.exchanges.dead}")
    private String deadLetterExchange;

    @Value("${campuslab.rabbitmq.queues.email}")
    private String emailQueueName;

    @Value("${campuslab.rabbitmq.queues.prep}")
    private String prepQueueName;

    @Value("${campuslab.rabbitmq.queues.voucher}")
    private String voucherQueueName;

    @Value("${campuslab.rabbitmq.queues.email-dlq}")
    private String emailDlqName;

    @Value("${campuslab.rabbitmq.queues.prep-dlq}")
    private String prepDlqName;

    @Value("${campuslab.rabbitmq.queues.voucher-dlq}")
    private String voucherDlqName;

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue emailQueue() {
        return createQueueWithDlq(
                emailQueueName,
                emailDlqName
        );
    }

    @Bean
    public Queue prepQueue() {
        return createQueueWithDlq(
                prepQueueName,
                prepDlqName
        );
    }

    @Bean
    public Queue voucherQueue() {
        return createQueueWithDlq(
                voucherQueueName,
                voucherDlqName
        );
    }

    private Queue createQueueWithDlq(
            String queueName,
            String deadLetterRoutingKey
    ) {

        Map<String, Object> arguments = new HashMap<>();

        arguments.put(
                "x-dead-letter-exchange",
                deadLetterExchange
        );

        arguments.put(
                "x-dead-letter-routing-key",
                deadLetterRoutingKey
        );

        return new Queue(
                queueName,
                true,
                false,
                false,
                arguments
        );
    }
}