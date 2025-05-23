package com.scrumly.messaging;

import com.scrumly.messaging.dto.ExportIssueDto;

public interface MessageProducer {
    void sendExportIssueEvent(String queueName, ExportIssueDto exportIssueDto);
}
