package com.scrumly.messaging.config;

import com.scrumly.messaging.MessageHandler;
import com.scrumly.messaging.topic.TopicConfiguration;
import com.scrumly.service.backlog.BacklogIssueService;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private final BacklogIssueService backlogIssueService;

    @Autowired
    public MessagingConfiguration(ConnectionFactory connectionFactory, BacklogIssueService backlogIssueService) {
        this.connectionFactory = connectionFactory;
        this.backlogIssueService = backlogIssueService;
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
        return new MessageHandler(backlogIssueService);
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
