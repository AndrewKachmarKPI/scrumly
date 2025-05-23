package com.scrumly.integrationservice.messaging.config;

import com.scrumly.integrationservice.messaging.MessageHandler;
import com.scrumly.integrationservice.messaging.topic.TopicConfiguration;
import com.scrumly.integrationservice.service.jira.JiraCloudApiService;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableRabbit
public class MessagingConfiguration implements RabbitListenerConfigurer {
    @Bean
    public FanoutExchange exchange() {
        return new FanoutExchange(TopicConfiguration.TOPIC_EXPORT_ISSUE);
    }

    @Bean
    public Queue queueExportScrumlyIssue() {
        return new Queue(TopicConfiguration.QUEUE_EXPORT_SCRUMLY_ISSUE, false);
    }

    @Bean
    public Queue queueExportJiraIssue() {
        return new Queue(TopicConfiguration.QUEUE_EXPORT_JIRA_ISSUE, false);
    }

    @Bean
    public List<Binding> binding() {
        return Arrays.asList(
                BindingBuilder.bind(queueExportScrumlyIssue()).to(exchange()),
                BindingBuilder.bind(queueExportJiraIssue()).to(exchange()));
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    public final ConnectionFactory connectionFactory;
    private final JiraCloudApiService jiraCloudApiService;

    @Autowired
    public MessagingConfiguration(ConnectionFactory connectionFactory, JiraCloudApiService jiraCloudApiService) {
        this.connectionFactory = connectionFactory;
        this.jiraCloudApiService = jiraCloudApiService;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }

    @Bean
    public MessageHandler eventResultHandler() {
        return new MessageHandler(jiraCloudApiService);
    }

    @Override
    public void configureRabbitListeners(
            RabbitListenerEndpointRegistrar registrar) {
        registrar.setMessageHandlerMethodFactory(myHandlerMethodFactory());
    }

    @Bean
    public DefaultMessageHandlerMethodFactory myHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        factory.setMessageConverter(new MappingJackson2MessageConverter());
        return factory;
    }
}
