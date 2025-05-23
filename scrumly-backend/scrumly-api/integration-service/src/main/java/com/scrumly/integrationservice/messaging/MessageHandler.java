package com.scrumly.integrationservice.messaging;

import com.scrumly.integrationservice.messaging.dto.ExportIssueDto;
import com.scrumly.integrationservice.messaging.topic.TopicConfiguration;
import com.scrumly.integrationservice.service.jira.JiraCloudApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;


@Slf4j
public class MessageHandler {
    private final JiraCloudApiService jiraCloudApiService;

    public MessageHandler(JiraCloudApiService jiraCloudApiService) {
        this.jiraCloudApiService = jiraCloudApiService;
    }

    @RabbitListener(queues = TopicConfiguration.QUEUE_EXPORT_JIRA_ISSUE)
    public void handleMessage(@Payload ExportIssueDto exportIssueDto) {
        log.info("[{}] - RECEIVE | new export issue dto: {}", TopicConfiguration.QUEUE_EXPORT_JIRA_ISSUE, exportIssueDto);
        jiraCloudApiService.exportIssue(exportIssueDto.getConnectingId(), exportIssueDto);
    }
}
