package com.scrumly.messaging;

import com.scrumly.messaging.dto.ExportIssueDto;
import com.scrumly.messaging.topic.TopicConfiguration;
import com.scrumly.service.backlog.BacklogIssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;


@Slf4j
public class MessageHandler {
    private final BacklogIssueService backlogIssueService;

    public MessageHandler(BacklogIssueService backlogIssueService) {
        this.backlogIssueService = backlogIssueService;
    }

    @RabbitListener(queues = TopicConfiguration.QUEUE_EXPORT_SCRUMLY_ISSUE)
    public void handleMessage(@Payload ExportIssueDto exportIssueDto) {
        log.info("[{}] - RECEIVE | new export issue dto: {}", TopicConfiguration.QUEUE_EXPORT_SCRUMLY_ISSUE, exportIssueDto);
        backlogIssueService.exportEstimation(exportIssueDto);
    }
}
