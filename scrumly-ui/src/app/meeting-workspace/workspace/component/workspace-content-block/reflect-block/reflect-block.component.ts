import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import {
  ActivityBlock,
  ActivityQuestionBlock,
  ActivityReflectBlock,
  ActivityRoomStatusMetadata,
  ReflectColumnCards,
  ReflectColumnMetadata,
  UserColumnReflectCard,
  UserMetadata
} from '../../../model/activity-room.model';
import { MenuItem, MessageService } from 'primeng/api';
import {
  control,
  controlArray,
  getRGB,
  specialCharacterValidator, trackByCardId, trackById,
} from '../../../../../ui-components/services/utils';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../../../auth/auth.service';
import { DndDropEvent, DropEffect } from 'ngx-drag-drop';
import { ActivityRoomActionsWsService } from '../../../services/activity-room-actions-ws.service';
import { ActivityRoomActionsService } from '../../../services/activity-room-actions.service';
import { NgxSpinnerService } from 'ngx-spinner';

@Component({
  selector: 'reflect-block',
  templateUrl: './reflect-block.component.html',
  styleUrl: './reflect-block.component.css'
})
export class ReflectBlockComponent implements OnInit, OnChanges {
  @Input('block') set reflectBlock(block: ActivityBlock) {
    this.block = block as ActivityReflectBlock;
  }

  block!: ActivityReflectBlock;

  @Input() activityId?: string;
  @Input() userMetadata?: UserMetadata;
  @Input() activityRoomStatus?: ActivityRoomStatusMetadata;

  columnCardGroup?: FormGroup;
  columnCardsMap: Map<number, UserColumnReflectCard[]> = new Map<number, UserColumnReflectCard[]>();
  columnCardsMapVersion?: number;

  constructor(private activityRoomActionWsService: ActivityRoomActionsWsService,
              private activityRoomActionService: ActivityRoomActionsService,
              private ngxSpinnerService: NgxSpinnerService,
              private messageService: MessageService,
              public authService: AuthService) {
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['reflectBlock'].firstChange && this.block) {
      this.initializeColumnCardGroup();
    }
    if (changes['reflectBlock'].currentValue && this.block) {
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
          isGenerating: new FormControl(false),
          cardId: new FormControl(''),
          cardContent: new FormControl('', Validators.compose([
            Validators.required, specialCharacterValidator, Validators.maxLength(300)
          ]))
        })
      )
    })
  }

  initializeColumnCardsMap() {
    this.columnCardsMap = new Map<number, UserColumnReflectCard[]>();
    this.block.columnMetadata.forEach(column => {
      this.columnCardsMap.set(column.id, this.getColumnUserCards(column.id));
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
    if (!this.columnGroup(columnId).valid) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Form warning',
        detail: 'Fill the form before sending!',
      });
      return;
    }

    const cardContent = this.columnGroupControl(columnId, 'cardContent').value;
    const cardId = this.columnGroupControl(columnId, 'cardId').value;
    const isEditing = this.columnGroupControl(columnId, 'isEditing').value;
    const reflectCard: UserColumnReflectCard = {
      columnId: columnId,
      cardId: isEditing ? cardId : undefined,
      cardContent: cardContent,
      userMetadata: this.userMetadata!
    }

    this.activityRoomActionWsService.onCreateReflectCard(this.activityId!, this.block.metadata.id, reflectCard)
      .subscribe({
        next: () => {
          console.log('columnId');
          this.onCancelColumnCard(columnId);
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Failed save',
            detail: 'Your reflection is not recorded!',
          });
        }
      })
  }

  onRemoveColumnCard(card: UserColumnReflectCard) {
    this.activityRoomActionWsService.onDeleteReflectCard(this.activityId!, this.block.metadata.id, card)
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
      })
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

  onGenerateReflection(column: ReflectColumnMetadata) {
    const prompt = this.columnGroupControl(column.id, 'cardContent').value;
    this.columnGroupControl(column.id, 'isGenerating').setValue(true);
    this.ngxSpinnerService.show(column.title);
    this.activityRoomActionService.onGenerateColumnReflection(this.activityId!, this.block.metadata.id!, prompt, column)
      .subscribe({
        next: (columnCard) => {
          if (columnCard) {
            this.columnGroupControl(column.id, 'cardContent')
              .setValue(columnCard.cardContent);
          }
          this.ngxSpinnerService.hide(column.title)
          this.columnGroupControl(column.id, 'isGenerating').setValue(false);
        },
        error: () => {
          this.ngxSpinnerService.hide(column.title);
          this.columnGroupControl(column.id, 'isGenerating').setValue(false);
        }
      })
  }

  getColumnUserCards(columnId: number): UserColumnReflectCard[] {
    return this.block.columnCards
      .filter(column => column.columnMetadata.id == columnId)
      .flatMap(column => column.userColumnReflectCards) || [];
  }

  onDragStart(event: DragEvent, column: ReflectColumnMetadata) {
    const cardColumn = this.block.columnCards
      .find(card => card.columnMetadata.id === column.id);
    this.columnCardsMapVersion = cardColumn?.version || 1;
  }

  onDragEnd(event: DragEvent, item: UserColumnReflectCard, list: UserColumnReflectCard[]) {
    const reflectColumnCards: ReflectColumnCards[] = Array.from(this.columnCardsMap.keys())
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
          userColumnReflectCards: orderedCards!,
          version: this.columnCardsMapVersion!
        };
      });
    this.activityRoomActionWsService.onChangeReflectCard(this.activityId!, this.block.metadata.id, reflectColumnCards)
      .subscribe({
        next: () => {
          this.columnCardsMapVersion = undefined;
        }
      })
  }


  onDragged(item: UserColumnReflectCard, list: UserColumnReflectCard[], effect: DropEffect) {
    if (effect === 'move') {
      const index = list.indexOf(item);
      list.splice(index, 1);
    }
  }

  onDrop(event: DndDropEvent, column: ReflectColumnMetadata) {
    const card: UserColumnReflectCard = event.data;
    const index = event.index;
    let cards = this.columnCardsMap.get(column.id);
    cards?.splice(index!, 0, card);
  }

  onClearReflectColumns() {
    const ids = this.block.columnMetadata.map(column => column.id);
    this.activityRoomActionWsService.onClearReflectColumns(this.activityId!, this.block.metadata.id, ids)
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
      })
  }


  protected readonly control = control;
  protected readonly getRGB = getRGB;
  protected readonly trackByCardId = trackByCardId;
  protected readonly trackById = trackById;
}
