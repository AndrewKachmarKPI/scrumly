import { Injectable } from '@angular/core';
import { environment } from '../../../../enviroments/enviroment';
import { HttpClient } from '@angular/common/http';
import { WebsocketActivityRoomService } from '../../websocket/websocket-activity-room.service';
import {
  ActivityTimerState,
  BlockNavigationMetadata, ItemBoardColumnIssues, ItemBoardColumnUserIssue,
  ReflectColumnCards, SyncBlockOption,
  UserColumnReflectCard, UserEstimateMetadata,
  UserQuestionAnswer
} from '../model/activity-room.model';
import { of, Subject } from 'rxjs';
import { IssueShortInfo } from '../model/issues.model';

@Injectable({
  providedIn: 'root'
})
export class ActivityRoomActionsWsService {

  constructor(private wsClient: WebsocketActivityRoomService) {
  }

  onActivityRoomNavigationChange(activityId: string, navigationMetadata: BlockNavigationMetadata) {
    const url = `activity/${ activityId }/onNavigationChange`
    return this.sendMessage(url, navigationMetadata);
  }

  onUserNavigationChange(activityId: string, activityBlockId: string) {
    const url = `activity/${ activityId }/onUserNavigationChange`
    return this.sendMessage(url, activityBlockId);
  }

  onKickUser(activityId: string, userId: string) {
    const url = `activity/${ activityId }/onKickUser`
    return this.sendMessage(url, {
      userId: userId
    });
  }

  onAnswerQuestion(activityId: string, blockId: string, userQuestionAnswer: UserQuestionAnswer) {
    const url = `activity/${ activityId }/block/${ blockId }/onAnswerQuestion`
    return this.sendMessage(url, userQuestionAnswer);
  }

  onDeleteQuestion(activityId: string, blockId: string) {
    const url = `activity/${ activityId }/block/${ blockId }/onDeleteQuestionAnswer`
    return this.sendMessage(url);
  }

  onCreateReflectCard(activityId: string, blockId: string, reflectCard: UserColumnReflectCard) {
    const url = `activity/${ activityId }/block/${ blockId }/onCreateReflectCard`
    return this.sendMessage(url, reflectCard);
  }

  onDeleteReflectCard(activityId: string, blockId: string, reflectCard: UserColumnReflectCard) {
    const url = `activity/${ activityId }/block/${ blockId }/onDeleteReflectCard`;
    return this.sendMessage(url, reflectCard);
  }

  onChangeReflectCard(activityId: string, blockId: string, cards: ReflectColumnCards[]) {
    const url = `activity/${ activityId }/block/${ blockId }/onChangeReflectCard`;
    return this.sendMessage(url, cards);
  }

  onClearReflectColumns(activityId: string, blockId: string, columnIds: number[]) {
    const url = `activity/${ activityId }/block/${ blockId }/onClearReflectColumns`;
    return this.sendMessage(url, columnIds);
  }

  onChangeTimerState(activityId: string, timerState: ActivityTimerState) {
    const url = `activity/${ activityId }/onChangeTimerState`;
    return this.sendMessage(url, timerState);
  }

  onChangeActiveEstimateIssue(activityId: string, blockId: string, issueId: string) {
    const url = `activity/${ activityId }/block/${ blockId }/issue/${ issueId }/onChangeActiveEstimateIssue`;
    return this.sendMessage(url);
  }

  onOpenSearchEstimateIssue(activityId: string, blockId: string) {
    const url = `activity/${ activityId }/block/${ blockId }/onOpenSearchEstimateIssue`;
    return this.sendMessage(url);
  }

  onSelectEstimate(activityId: string, blockId: string, issueId: string, userEstimate: UserEstimateMetadata) {
    const url = `activity/${ activityId }/block/${ blockId }/issue/${ issueId }/onSelectEstimate`;
    return this.sendMessage(url, userEstimate);
  }

  onRevealEstimates(activityId: string, blockId: string, issueId: string) {
    const url = `activity/${ activityId }/block/${ blockId }/issue/${ issueId }/onRevealEstimates`;
    return this.sendMessage(url);
  }

  onHideEstimates(activityId: string, blockId: string, issueId: string) {
    const url = `activity/${ activityId }/block/${ blockId }/issue/${ issueId }/onHideEstimates`;
    return this.sendMessage(url);
  }

  onSelectFinalEstimate(activityId: string, blockId: string, issueId: string, estimate: string) {
    const url = `activity/${ activityId }/block/${ blockId }/issue/${ issueId }/onSelectFinalEstimate`;
    return this.sendMessage(url, {
      estimate: estimate
    });
  }

  onUpdateMeetingNotes(activityId: string, notes: string) {
    const url = `activity/${ activityId }/onUpdateMeetingNotes`;
    return this.sendMessage(url, {
      notes: notes
    });
  }

  onCreateBoardBacklogCard(activityId: string, blockId: string, reflectCard: ItemBoardColumnUserIssue) {
    const url = `activity/${ activityId }/block/${ blockId }/onCreateBoardBacklogCard`
    return this.sendMessage(url, reflectCard);
  }

  onDeleteBoardBacklogCard(activityId: string, blockId: string, reflectCard: ItemBoardColumnUserIssue) {
    const url = `activity/${ activityId }/block/${ blockId }/onDeleteBoardBacklogCard`;
    return this.sendMessage(url, reflectCard);
  }

  onChangeBoardBacklogCard(activityId: string, blockId: string, cards: ItemBoardColumnIssues[]) {
    const url = `activity/${ activityId }/block/${ blockId }/onChangeBoardBacklogCard`;
    return this.sendMessage(url, cards);
  }

  onClearBoardBacklogColumns(activityId: string, blockId: string, columnIds: number[]) {
    const url = `activity/${ activityId }/block/${ blockId }/onClearBoardBacklogColumns`;
    return this.sendMessage(url, columnIds);
  }

  onSelectItemBoardColumnIssueAssignee(activityId: string, blockId: string, userIssue: ItemBoardColumnUserIssue) {
    const url = `activity/${ activityId }/block/${ blockId }/onSelectItemBoardColumnIssueAssignee`;
    return this.sendMessage(url, userIssue);
  }

  onClearBacklog(activityId: string, blockId: string) {
    const url = `activity/${ activityId }/block/${ blockId }/onClearBacklog`;
    return this.sendMessage(url);
  }

  onChangeIssueEstimation(activityId: string, blockId: string, userIssue: ItemBoardColumnUserIssue) {
    const url = `activity/${ activityId }/block/${ blockId }/onChangeIssueEstimation`;
    return this.sendMessage(url, userIssue);
  }

  onSyncActivityBlock(activityId: string, blockId: string, syncOption: SyncBlockOption) {
    const url = `activity/${ activityId }/block/${ blockId }/onSyncActivityBlock`;
    return this.sendMessage(url, syncOption);
  }

  private sendMessage(url: string, data?: any) {
    this.wsClient.send(url, data);
    return of(true);
  }
}
