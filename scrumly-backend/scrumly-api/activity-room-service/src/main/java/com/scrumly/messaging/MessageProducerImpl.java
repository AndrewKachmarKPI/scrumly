package com.scrumly.messaging;

import com.scrumly.messaging.dto.ExportIssueDto;
import com.scrumly.messaging.topic.TopicConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageProducerImpl implements MessageProducer {
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void sendExportIssueEvent(String queueName, ExportIssueDto exportIssueDto) {
        log.info("[{}] - SEND | new export issue dto: {}", queueName, exportIssueDto);
        rabbitTemplate.convertAndSend(queueName, exportIssueDto);
    }
}
