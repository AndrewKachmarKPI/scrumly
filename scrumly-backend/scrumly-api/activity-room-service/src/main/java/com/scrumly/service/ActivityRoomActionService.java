package com.scrumly.service;

import com.scrumly.domain.ActivityRoom;
import com.scrumly.domain.ActivityTimerState;
import com.scrumly.dto.events.ActivitySummaryNotes;
import com.scrumly.dto.events.OnUpdateMeetingNotes;
import com.scrumly.dto.events.SyncBlockOption;
import com.scrumly.dto.issues.IssueShortInfo;

import java.util.List;

public interface ActivityRoomActionService {
    void onActivityRoomNavigationChange(String username, String activityId, ActivityRoom.BlockNavigationMetadata navigationMetadata);

    void onUserNavigationChange(String username, String activityId, String activityBlockId);

    void onKickUser(String username, String activityId, String userId);

    void onAnswerQuestion(String username, String activityId, String blockId, ActivityRoom.ActivityBlock.ActivityQuestionBlock.UserQuestionAnswer userQuestionAnswer);

    void onDeleteQuestionAnswer(String username, String activityId, String blockId);

    void onCreateReflectCard(String username, String activityId, String blockId, ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard.UserColumnReflectCard reflectCard);

    void onDeleteReflectCard(String username, String activityId, String blockId, ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard.UserColumnReflectCard reflectCard);

    void onChangeReflectCard(String username, String activityId, String blockId, List<ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard> reflectColumnCards);

    void onClearReflectColumns(String username, String activityId, String blockId, List<Long> columnIds);

    void onChangeTimerState(String username, String activityId, ActivityTimerState timerState);

    void onSelectEstimateIssues(String username, String activityId, String blockId, List<IssueShortInfo> issues);

    void onDeleteEstimateIssues(String username, String activityId, String blockId, List<String> issueIds);

    void onUpdateEstimateIssues(String username, String activityId, String blockId, List<String> issueIds);

    void onSelectBoardBacklogIssues(String username, String activityId, String blockId, List<IssueShortInfo> issues);

    void onDeleteBoardBacklogIssues(String username, String activityId, String blockId, List<String> issueIds);

    void onUpdateBoardBacklogIssues(String username, String activityId, String blockId, List<String> issueIds);

    void onChangeActiveEstimateIssue(String username, String activityId, String blockId, String issueId);

    void onOpenSearchEstimateIssue(String username, String activityId, String blockId);

    void onSelectEstimate(String username, String activityId, String blockId, String issueId,
                          ActivityRoom.ActivityBlock.ActivityEstimateBlock.UserEstimateMetadata userEstimateMetadata);

    void onRevealEstimates(String username, String activityId, String blockId, String issueId);

    void onHideEstimates(String username, String activityId, String blockId, String issueId);

    void onSelectFinalEstimate(String username, String activityId, String blockId, String issueId, String estimate);

    void onUpdateMeetingNotes(String username, String activityId, OnUpdateMeetingNotes meetingNotes);

    void onCreateBoardBacklogCard(String username, String activityId, String blockId, ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue reflectCard);

    void onDeleteBoardBacklogCard(String username, String activityId, String blockId, ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue reflectCard);

    void onChangeBoardBacklogCard(String username, String activityId, String blockId, List<ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues> reflectColumnCards);

    void onClearBoardBacklogColumns(String username, String activityId, String blockId, List<Long> columnIds);

    void onSelectItemBoardColumnIssueAssignee(String username, String activityId, String blockId, ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue userIssue);

    void onClearBacklog(String username, String activityId, String blockId);

    void onRecalculateTeamLoad(String username, String activityId, String blockId);

    void onChangeIssueEstimation(String username, String activityId, String blockId, ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue userIssue);

    void onSyncActivityBlock(String username, String activityId, String blockId, SyncBlockOption syncBlockOption);

    ActivitySummaryNotes onGenerateActivitySummary(String username, String activityId);

    ActivityRoom.ActivityRoomNotesMetadata onSetMeetingNotes(String username, String activityId, String notes);

    ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard.UserColumnReflectCard onGenerateColumnReflection(String username, String activityId, String blockId, String prompt, ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnMetadata reflectColumnMetadata);
}
