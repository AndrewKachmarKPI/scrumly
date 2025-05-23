package com.scrumly.integrationservice.messaging.topic;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TopicConfiguration {
    public static final String TOPIC_EXPORT_ISSUE = "onExportIssue:topic";
    public static final String QUEUE_EXPORT_SCRUMLY_ISSUE = "exportScrumlyIssue:queue";
    public static final String QUEUE_EXPORT_JIRA_ISSUE = "exportJiraIssue:queue";
}
