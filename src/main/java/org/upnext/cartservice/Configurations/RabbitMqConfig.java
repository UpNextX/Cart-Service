package org.upnext.cartservice.Configurations;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "payment.exchange";
    public static final String CART_CLEAR_QUEUE = "cart.clear.queue";

    public static final String CART_CLEAR_ROUTING_KEY = "payment.success";
    public static final String CART_DLX = "cart.dlx";
    public static final String CART_DLQ = "cart.clear.dlx.queue";
    public static final String CART_DLQ_ROUTING_KEY = "cart.clear.dlx";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue cartClearQueue() {
        return QueueBuilder.durable(CART_CLEAR_QUEUE)
                .withArgument("x-dead-letter-exchange", CART_DLX)
                .withArgument("x-dead-letter-routing-key", CART_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding cartClearBinding(Queue cartClearQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(cartClearQueue)
                .to(paymentExchange)
                .with(CART_CLEAR_ROUTING_KEY);
    }

    @Bean
    public DirectExchange cartDLX() {
        return new DirectExchange(CART_DLX);
    }

    @Bean
    public Queue cartClearDLQ() {
        return QueueBuilder.durable(CART_DLQ).build();
    }

    @Bean
    public Binding cartDLQBinding(Queue cartClearDLQ, DirectExchange cartDLX) {
        return BindingBuilder.bind(cartClearDLQ).to(cartDLX).with(CART_DLQ_ROUTING_KEY);
    }

    // Deserialize
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }


}
