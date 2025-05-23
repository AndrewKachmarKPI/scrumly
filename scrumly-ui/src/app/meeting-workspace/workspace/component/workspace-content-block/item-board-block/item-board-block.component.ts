import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import {
  ActivityBlock,
  ActivityItemBoardBlock,
  ActivityRoomStatusMetadata,
  ItemBoardColumnIssues, ItemBoardColumnMetadata,
  ItemBoardColumnUserIssue, MeterProgressMetadata,
  TeamMetadata,
  UserColumnReflectCard,
  UserMetadata
} from '../../../model/activity-room.model';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivityRoomActionsWsService } from '../../../services/activity-room-actions-ws.service';
import { MessageService } from 'primeng/api';
import { AuthService } from '../../../../../auth/auth.service';
import {
  control,
  controlArray, getRGB,
  specialCharacterValidator,
  trackByColumnIssueId, trackById, trackByIssueId
} from '../../../../../ui-components/services/utils';
import { DndDropEvent, DropEffect } from 'ngx-drag-drop';
import { filter } from 'rxjs';
import { DialogService } from 'primeng/dynamicdialog';
import { SelectBacklogItemsDialogComponent } from './select-backlog-items-dialog/select-backlog-items-dialog.component';
import { ActivityRoomActionsService } from '../../../services/activity-room-actions.service';
import { IssueShortInfo } from '../../../model/issues.model';
import { TeamDto } from '../../../../../member-dashboard/modules/organizations/model/organization.model';
import { TeamService } from '../../../../../member-dashboard/modules/organizations/services/team.service';
import { UserProfileDto } from '../../../../../auth/auth.model';
import { OverlayPanel } from 'primeng/overlaypanel';

@Component({
  selector: 'item-board-block',
  templateUrl: './item-board-block.component.html',
  styleUrl: './item-board-block.component.css'
})
export class ItemBoardBlockComponent implements OnInit, OnChanges {
  @Input('block') set itemBoardBlock(block: ActivityBlock) {
    this.block = block as ActivityItemBoardBlock;
  }

  block!: ActivityItemBoardBlock;

  @Input() activityId?: string;
  @Input() isFacilitator?: boolean;
  @Input() userMetadata?: UserMetadata;
  @Input() teamMetadata?: TeamMetadata;
  @Input() numberOfUsers?: number;
  @Input() nestedBlockId?: string;
  @Input() activityRoomStatus?: ActivityRoomStatusMetadata;

  columnCardGroup?: FormGroup;
  columnCardsMap: Map<number, ItemBoardColumnUserIssue[]> = new Map<number, ItemBoardColumnUserIssue[]>();
  columnCardsMapVersion?: number;

  filteredBacklog: IssueShortInfo[] = [];

  estimateControl = new FormControl('', Validators.compose([
    Validators.required, specialCharacterValidator
  ]))

  constructor(private activityRoomWsAction: ActivityRoomActionsWsService,
              private activityRoomAction: ActivityRoomActionsService,
              private teamService: TeamService,
              private messageService: MessageService,
              public authService: AuthService,
              private dialogService: DialogService) {
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['itemBoardBlock'].firstChange && this.block) {
      this.initializeColumnCardGroup();
    }
    if (changes['itemBoardBlock'].currentValue && this.block) {
      this.initializeColumnCardsMap();
    }
  }

  initializeColumnCardGroup() {
    this.columnCardGroup = new FormGroup({
      columns: new FormArray([])
    });
    this.block.columnMetadata.forEach(column => {
      this.columnGroupArray().push(
        new FormGroup({
          columnId: new FormControl(column.id),
          isEditing: new FormControl(false),
          isCreate: new FormControl(false),
          cardId: new FormControl(''),
          cardContent: new FormControl('', Validators.compose([
            Validators.required, specialCharacterValidator, Validators.maxLength(300)
          ]))
        })
      )
    })
  }

  initializeColumnCardsMap() {
    this.columnCardsMap = new Map<number, ItemBoardColumnUserIssue[]>();
    this.block.columnMetadata.forEach(column => {
      this.columnCardsMap.set(column.id, this.getColumnUserCards(column.id));
    });
    const usedIssues = this.block.columnIssues
      .flatMap(columnIssue => columnIssue.userColumnIssues)
      // .filter(userIssue => userIssue.userMetadata?.userId === this.userMetadata?.userId)
      .map(userIssue => userIssue.issueMetadata)
      .map(issue => issue?.issueId);
    this.filteredBacklog = this.block?.issueBacklog.filter(issue => {
      return !usedIssues.includes(issue.issueId);
    });
  }

  columnGroupArray() {
    return controlArray(this.columnCardGroup!, 'columns');
  }

  columnGroup(columnId: number): FormGroup {
    const index = this.block.columnMetadata.findIndex(column => column.id === columnId);
    return this.columnGroupArray().at(index) as FormGroup;
  }

  columnGroupControl(columnId: number, controlName: string): FormControl {
    const index = this.block.columnMetadata.findIndex(column => column.id === columnId);
    return this.columnGroupArray().at(index).get(controlName) as FormControl;
  }

  onSubmitColumnCard(columnId: number) {

  }

  onRemoveColumnCard(card: UserColumnReflectCard) {

  }

  onEditColumnCard(card: UserColumnReflectCard) {
    this.columnGroupControl(card.columnId, 'cardContent').setValue(card.cardContent);
    this.columnGroupControl(card.columnId, 'cardId').setValue(card.cardId);
    this.columnGroupControl(card.columnId, 'isEditing').setValue(true);
  }

  onCancelColumnCard(columnId: number) {
    this.columnGroupControl(columnId, 'cardContent').reset();
    this.columnGroupControl(columnId, 'cardId').setValue('');
    this.columnGroupControl(columnId, 'isEditing').setValue(false);
    this.columnGroupControl(columnId, 'isCreate').setValue(false);
  }

  getColumnUserCards(columnId: number): ItemBoardColumnUserIssue[] {
    return this.block.columnIssues
      .filter(column => column.columnMetadata.id == columnId)
      .flatMap(column => column.userColumnIssues) || [];
  }

  onDragStart(event: DragEvent, column: ItemBoardColumnMetadata) {
    const cardColumn = this.block.columnIssues
      .find(card => card.columnMetadata.id === column.id);
    this.columnCardsMapVersion = cardColumn?.version || 1;
  }

  onDragEnd(event: DragEvent, item: ItemBoardColumnUserIssue, list: ItemBoardColumnUserIssue[]) {
    const columnIssuesCards: ItemBoardColumnIssues[] = Array.from(this.columnCardsMap.keys())
      .map(columnId => {
        const userCards = this.columnCardsMap.get(columnId);
        const orderedCards = userCards?.map((card, index) => {
          return {
            ...card,
            order: index + 1
          }
        });
        const metadata = this.block.columnMetadata.find(column => column.id === columnId);
        return {
          columnMetadata: metadata!,
          userColumnIssues: orderedCards!,
          version: this.columnCardsMapVersion!
        };
      });
    this.activityRoomWsAction.onChangeBoardBacklogCard(this.activityId!, this.block.metadata.id, columnIssuesCards)
      .subscribe({
        next: () => {
          this.columnCardsMapVersion = undefined;
        }
      })
  }


  onDragged(item: ItemBoardColumnUserIssue, list: ItemBoardColumnUserIssue[], effect: DropEffect) {
    if (effect === 'move') {
      const index = list.indexOf(item);
      list.splice(index, 1);
    }
  }

  onDrop(event: DndDropEvent, column: ItemBoardColumnMetadata) {
    const index = event.index;
    const card = event.data as ItemBoardColumnUserIssue;
    let cards = this.columnCardsMap.get(column.id);
    if (!event.data.userMetadata) {
      const issue: IssueShortInfo = event.data;
      const card: ItemBoardColumnUserIssue = {
        columnId: column.id,
        columnIssueId: issue.issueId,
        userMetadata: this.userMetadata,
        order: index,
        issueMetadata: event.data,
        statusMetadata: column.columnIssueStatuses.find(status => {
          return status.backlogId === issue.projectId
        })
      };
      console.log('column', column)
      console.log('card:', card);
      this.activityRoomWsAction.onCreateBoardBacklogCard(this.activityId!, this.block.metadata.id, card);
    }
    cards?.splice(index!, 0, card);
  }

  onClearBoard() {
    const ids = this.block.columnMetadata.map(column => column.id);
    this.activityRoomWsAction.onClearBoardBacklogColumns(this.activityId!, this.block.metadata.id, ids)
      .subscribe({
        next: () => {

        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Failed delete',
            detail: 'Your reflection is not deleted!',
          });
        }
      });
  }


  onSelectBacklogItems() {
    const ref = this.dialogService.open(SelectBacklogItemsDialogComponent, {
      width: '50rem',
      height: 'auto',
      breakpoints: {
        '1199px': '45vw',
        '575px': '90vw'
      },
      resizable: true,
      draggable: false,
      closeOnEscape: true,
      closable: true,
      header: 'Search backlog items',
      data: {
        teamMetadata: this.teamMetadata,
        blockId: this.block.metadata?.id,
        activityId: this.activityId
      }
    });
    ref.onClose
      .pipe(filter(Boolean))
      .subscribe({
        next: (selectedRule) => {
        }
      })
  }

  onClearBacklog() {
    this.activityRoomWsAction.onClearBacklog(this.activityId!, this.block?.metadata?.id);
  }

  onRemoveBacklogItem(issueId: string) {
    this.activityRoomAction.onDeleteBoardBacklogIssues(this.activityId!, this.block?.metadata?.id, [ issueId ])
      .subscribe({
        next: () => {

        }
      });
  }

  onRemoveColumnIssue(issue: ItemBoardColumnUserIssue) {
    this.activityRoomWsAction.onDeleteBoardBacklogCard(this.activityId!, this.block?.metadata?.id, issue);
  }

  onSelectCardAssignee(card: ItemBoardColumnUserIssue, user: UserMetadata) {
    card.userMetadata = user;
    this.activityRoomWsAction.onSelectItemBoardColumnIssueAssignee(this.activityId!, this.block?.metadata?.id, card);
  }

  openEstimateOverlay(event: Event, overlay: OverlayPanel, card: ItemBoardColumnUserIssue) {
    if (!this.activityRoomStatus?.isActive) {
      return;
    }
    overlay.toggle(event);
    this.estimateControl.setValue(card?.issueMetadata?.estimate!);
  }

  onEstimateChange(overlay: OverlayPanel, card: ItemBoardColumnUserIssue) {
    if (!this.estimateControl.valid) {
      return;
    }
    const estimate = this.estimateControl.value!;
    overlay.hide();
    card!.issueMetadata!.estimate = estimate;
    this.activityRoomWsAction.onChangeIssueEstimation(this.activityId!, this.block?.metadata?.id, card);
  }

  protected readonly control = control;
  protected readonly trackByColumnIssueId = trackByColumnIssueId;
  protected readonly trackByIssueId = trackByIssueId;
  protected readonly trackById = trackById;
  protected readonly getRGB = getRGB;
  protected readonly String = String;
}
