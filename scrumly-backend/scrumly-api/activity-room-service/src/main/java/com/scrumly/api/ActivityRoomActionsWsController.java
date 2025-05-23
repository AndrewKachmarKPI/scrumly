package com.scrumly.api;

import com.scrumly.domain.ActivityRoom;
import com.scrumly.domain.ActivityTimerState;
import com.scrumly.dto.events.OnUpdateMeetingNotes;
import com.scrumly.dto.events.SyncBlockOption;
import com.scrumly.dto.issues.IssueEstimate;
import com.scrumly.dto.user.UserIdDto;
import com.scrumly.service.ActivityRoomActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.util.List;


@Controller
@RequiredArgsConstructor
public class ActivityRoomActionsWsController {
    private final ActivityRoomActionService roomActionService;

    @MessageMapping("/activity/{activityId}/onNavigationChange")
    public void onActivityRoomNavigationChange(@DestinationVariable String activityId,
                                               @Payload ActivityRoom.BlockNavigationMetadata navigationMetadata,
                                               @AuthenticationPrincipal JwtAuthenticationToken auth) {
        roomActionService.onActivityRoomNavigationChange(auth.getName(), activityId, navigationMetadata);
    }

    @MessageMapping("/activity/{activityId}/onUserNavigationChange")
    public void onUserNavigationChange(@DestinationVariable String activityId,
                                       @Payload String activityBlockId,
                                       @AuthenticationPrincipal JwtAuthenticationToken auth) {
        roomActionService.onUserNavigationChange(auth.getName(), activityId, activityBlockId);
    }

    @MessageMapping("/activity/{activityId}/onKickUser")
    public void onUserNavigationChange(@DestinationVariable String activityId,
                                       @Payload UserIdDto userIdDto,
                                       @AuthenticationPrincipal JwtAuthenticationToken auth) {
        roomActionService.onKickUser(auth.getName(), activityId, userIdDto.getUserId());
    }


    @MessageMapping("/activity/{activityId}/block/{blockId}/onAnswerQuestion")
    public void onAnswerQuestion(@DestinationVariable String activityId,
                                 @DestinationVariable String blockId,
                                 @Payload ActivityRoom.ActivityBlock.ActivityQuestionBlock.UserQuestionAnswer userQuestionAnswer,
                                 @AuthenticationPrincipal JwtAuthenticationToken auth) {
        roomActionService.onAnswerQuestion(auth.getName(), activityId, blockId, userQuestionAnswer);
    }

    @MessageMapping("/activity/{activityId}/block/{blockId}/onDeleteQuestionAnswer")
    public void onDeleteQuestionAnswer(@DestinationVariable String activityId,
                                       @DestinationVariable String blockId,
                                       @AuthenticationPrincipal JwtAuthenticationToken auth) {
        roomActionService.onDeleteQuestionAnswer(auth.getName(), activityId, blockId);
    }


    @MessageMapping("/activity/{activityId}/block/{blockId}/onCreateReflectCard")
    public void onCreateReflectCard(@DestinationVariable String activityId,
                                    @DestinationVariable String blockId,
                                    @Payload ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard.UserColumnReflectCard reflectCard,
                                    @AuthenticationPrincipal JwtAuthenticationToken auth) {
        roomActionService.onCreateReflectCard(auth.getName(), activityId, blockId, reflectCard);
    }


    @MessageMapping("/activity/{activityId}/block/{blockId}/onDeleteReflectCard")
    public void onDeleteReflectCard(@DestinationVariable String activityId,
                                    @DestinationVariable String blockId,
                                    @Payload ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard.UserColumnReflectCard reflectCard,
                                    @AuthenticationPrincipal JwtAuthenticationToken auth) {
        roomActionService.onDeleteReflectCard(auth.getName(), activityId, blockId, reflectCard);
    }

    @MessageMapping("/activity/{activityId}/block/{blockId}/onChangeReflectCard")
    public void onChangeReflectCard(@DestinationVariable String activityId,
                                    @DestinationVariable String blockId,
                                    @Payload List<ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard> reflectColumnCards,
                                    @AuthenticationPrincipal JwtAuthenticationToken auth) {
        roomActionService.onChangeReflectCard(auth.getName(), activityId, blockId, reflectColumnCards);
    }

    @MessageMapping("/activity/{activityId}/block/{blockId}/onClearReflectColumns")
    public void onClearReflectColumns(@DestinationVariable String activityId,
                                      @DestinationVariable String blockId,
                                      @Payload List<Long> clearIds,
                                      @AuthenticationPrincipal JwtAuthenticationToken auth) {
        roomActionService.onClearReflectColumns(auth.getName(), activityId, blockId, clearIds);
    }

    @MessageMapping("/activity/{activityId}/onChangeTimerState")
    public void onChangeTimerState(@DestinationVariable String activityId,
                                   @Payload ActivityTimerState timerState,
                                   @AuthenticationPrincipal JwtAuthenticationToken auth) {
        roomActionService.onChangeTimerState(auth.getName(), activityId, timerState);
    }

    @MessageMapping("/activity/{activityId}/block/{blockId}/issue/{issueId}/onChangeActiveEstimateIssue")
    public void onChangeActiveEstimateIssue(@DestinationVariable String activityId,
                                            @DestinationVariable String blockId,
                                            @DestinationVariable String issueId,
                                            @AuthenticationPrincipal JwtAuthenticationToken auth) {
        roomActionService.onChangeActiveEstimateIssue(auth.getName(), activityId, blockId, issueId);
    }

    @MessageMapping("/activity/{activityId}/block/{blockId}/onOpenSearchEstimateIssue")
    public void onOpenSearchEstimateIssue(@DestinationVariable String activityId,
                                          @DestinationVariable String blockId,
                                          @AuthenticationPrincipal JwtAuthenticationToken auth) {
        roomActionService.onOpenSearchEstimateIssue(auth.getName(), activityId, blockId);
    }

    @MessageMapping("/activity/{activityId}/block/{blockId}/issue/{issueId}/onSelectEstimate")
    public void onSelectEstimate(@DestinationVariable String activityId,
                                 @DestinationVariable String blockId,
                                 @DestinationVariable String issueId,
                                 @Payload ActivityRoom.ActivityBlock.ActivityEstimateBlock.UserEstimateMetadata userEstimateMetadata,
                                 @AuthenticationPrincipal JwtAuthenticationToken auth) {
        roomActionService.onSelectEstimate(auth.getName(), activityId, blockId, issueId, userEstimateMetadata);
    }

    @MessageMapping("/activity/{activityId}/block/{blockId}/issue/{issueId}/onRevealEstimates")
    public void onRevealEstimates(@DestinationVariable String activityId,
                                  @DestinationVariable String blockId,
                                  @DestinationVariable String issueId,
                                  @AuthenticationPrincipal JwtAuthenticationToken auth) {
        roomActionService.onRevealEstimates(auth.getName(), activityId, blockId, issueId);
    }

    @MessageMapping("/activity/{activityId}/block/{blockId}/issue/{issueId}/onHideEstimates")
    public void onHideEstimates(@DestinationVariable String activityId,
                                @DestinationVariable String blockId,
                                @DestinationVariable String issueId,
                                @AuthenticationPrincipal JwtAuthenticationToken auth) {
        roomActionService.onHideEstimates(auth.getName(), activityId, blockId, issueId);
    }

    @MessageMapping("/activity/{activityId}/block/{blockId}/issue/{issueId}/onSelectFinalEstimate")
    public void onSelectFinalEstimate(@DestinationVariable String activityId,
                                      @DestinationVariable String blockId,
                                      @DestinationVariable String issueId,
                                      @Payload IssueEstimate estimate,
                                      @AuthenticationPrincipal JwtAuthenticationToken auth) {
        roomActionService.onSelectFinalEstimate(auth.getName(), activityId, blockId, issueId, estimate.getEstimate());
    }

    @MessageMapping("/activity/{activityId}/onUpdateMeetingNotes")
    public void onUpdateMeetingNotes(@DestinationVariable String activityId,
                                     @Payload OnUpdateMeetingNotes notes,
                                     @AuthenticationPrincipal JwtAuthenticationToken auth) {
        roomActionService.onUpdateMeetingNotes(auth.getName(), activityId, notes);
    }


    @MessageMapping("/activity/{activityId}/block/{blockId}/onCreateBoardBacklogCard")
    public void onCreateBoardBacklogCard(@DestinationVariable String activityId,
                                         @DestinationVariable String blockId,
                                         @Payload ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue userIssue,
                                         @AuthenticationPrincipal JwtAuthenticationToken auth) {
        roomActionService.onCreateBoardBacklogCard(auth.getName(), activityId, blockId, userIssue);
    }


    @MessageMapping("/activity/{activityId}/block/{blockId}/onDeleteBoardBacklogCard")
    public void onDeleteBoardBacklogCard(@DestinationVariable String activityId,
                                         @DestinationVariable String blockId,
                                         @Payload ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue userIssue,
                                         @AuthenticationPrincipal JwtAuthenticationToken auth) {
        roomActionService.onDeleteBoardBacklogCard(auth.getName(), activityId, blockId, userIssue);
    }

    @MessageMapping("/activity/{activityId}/block/{blockId}/onChangeBoardBacklogCard")
    public void onChangeBoardBacklogCard(@DestinationVariable String activityId,
                                         @DestinationVariable String blockId,
                                         @Payload List<ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues> userIssues,
                                         @AuthenticationPrincipal JwtAuthenticationToken auth) {
        roomActionService.onChangeBoardBacklogCard(auth.getName(), activityId, blockId, userIssues);
    }

    @MessageMapping("/activity/{activityId}/block/{blockId}/onClearBoardBacklogColumns")
    public void onClearBoardBacklogColumns(@DestinationVariable String activityId,
                                           @DestinationVariable String blockId,
                                           @Payload List<Long> clearIds,
                                           @AuthenticationPrincipal JwtAuthenticationToken auth) {
        roomActionService.onClearBoardBacklogColumns(auth.getName(), activityId, blockId, clearIds);
    }

    @MessageMapping("/activity/{activityId}/block/{blockId}/onSelectItemBoardColumnIssueAssignee")
    public void onSelectItemBoardColumnIssueAssignee(@DestinationVariable String activityId,
                                                     @DestinationVariable String blockId,
                                                     @Payload ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue userIssue,
                                                     @AuthenticationPrincipal JwtAuthenticationToken auth) {
        roomActionService.onSelectItemBoardColumnIssueAssignee(auth.getName(), activityId, blockId, userIssue);
    }

    @MessageMapping("/activity/{activityId}/block/{blockId}/onClearBacklog")
    public void onClearBacklog(@DestinationVariable String activityId,
                               @DestinationVariable String blockId,
                               @AuthenticationPrincipal JwtAuthenticationToken auth) {
        roomActionService.onClearBacklog(auth.getName(), activityId, blockId);
    }


    @MessageMapping("/activity/{activityId}/block/{blockId}/onChangeIssueEstimation")
    public void onChangeIssueEstimation(@DestinationVariable String activityId,
                                        @DestinationVariable String blockId,
                                        @Payload ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue userIssue,
                                        @AuthenticationPrincipal JwtAuthenticationToken auth) {
        roomActionService.onChangeIssueEstimation(auth.getName(), activityId, blockId, userIssue);
    }

    @MessageMapping("/activity/{activityId}/block/{blockId}/onSyncActivityBlock")
    public void onSyncActivityBlock(@DestinationVariable String activityId,
                                    @DestinationVariable String blockId,
                                    @Payload SyncBlockOption syncBlockOption,
                                    @AuthenticationPrincipal JwtAuthenticationToken auth) {
        roomActionService.onSyncActivityBlock(auth.getName(), activityId, blockId, syncBlockOption);
    }
}