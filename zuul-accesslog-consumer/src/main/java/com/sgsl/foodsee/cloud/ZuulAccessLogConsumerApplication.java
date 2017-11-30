package com.sgsl.foodsee.cloud;

import com.sgsl.foodsee.cloud.receiver.AccessLogReceiver;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootApplication
@EnableDiscoveryClient
public class ZuulAccessLogConsumerApplication {
    @Bean
    public TopicExchange accessLogExchange() {
        return new TopicExchange("zuul.access.logger");
    }

    @Bean
    public Queue accessLogQueue() {
        return new Queue("zuulAccessQueue");
    }

    @Bean
    public Binding bindingRoutingRule(Queue accessLogQueue, TopicExchange accessLogExchange) {
        return BindingBuilder.bind(accessLogQueue).to(accessLogExchange).with("zuul.#");
    }

    @Bean
    public AccessLogReceiver receiver(MongoTemplate mongoTemplate) {
        return new AccessLogReceiver(mongoTemplate);
    }

    public static void main(String[] args) {
        SpringApplication.run(ZuulAccessLogConsumerApplication.class, args);
    }
}
