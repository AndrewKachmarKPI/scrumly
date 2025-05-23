import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import {
  CreateActivityBlockRQ,
  CreateItemsBoardRQ,
  ItemsBoardColumnDto, ItemsBoardColumnStatusDto,
} from '../../../../../events/model/activity.model';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import { OnBlockUpdateEvent } from '../create-activity-block/create-activity-block.component';
import {
  asControl,
  control,
  controlArray,
  randomHexColor,
} from '../../../../../../../ui-components/services/utils';
import { IssueStatusService } from '../../../../../backlog/services/issue-status.service';
import { BacklogIssueStatusesDto } from '../../../../../backlog/model/backlog.model';

@Component({
  selector: 'create-items-board-block',
  templateUrl: './create-items-board-block.component.html',
  styleUrl: './create-items-board-block.component.css'
})
export class CreateItemsBoardBlockComponent implements OnInit, OnChanges {
  blockRQ?: CreateItemsBoardRQ;
  formGroup: FormGroup;
  isInitialized: boolean = false;
  statuses?: BacklogIssueStatusesDto[] = [
    {
      'backlogId': '359981a6-e81a-4bbb-b43d-d73a38ee29b5',
      'backlogName': 'IAG 2.0 -  Backlog',
      'serviceType': 'SCRUMLY',
      'statusList': [
        {
          'id': 1,
          'statusId': '1',
          'backlogId': '359981a6-e81a-4bbb-b43d-d73a38ee29b5',
          'status': 'New',
          'color': '#A8D08D'
        },
        {
          'id': 2,
          'statusId': '2',
          'backlogId': '359981a6-e81a-4bbb-b43d-d73a38ee29b5',
          'status': 'In Progress',
          'color': '#F4E041'
        },
        {
          'id': 3,
          'statusId': '3',
          'backlogId': '359981a6-e81a-4bbb-b43d-d73a38ee29b5',
          'status': 'Closed',
          'color': '#FF6347'
        },
        {
          'id': 4,
          'statusId': '4',
          'backlogId': '359981a6-e81a-4bbb-b43d-d73a38ee29b5',
          'status': 'Blocked',
          'color': '#9B59B6'
        },
        {
          'id': 5,
          'statusId': '5',
          'backlogId': '359981a6-e81a-4bbb-b43d-d73a38ee29b5',
          'status': 'On Hold',
          'color': '#FFB84D'
        },
        {
          'id': 6,
          'statusId': '6',
          'backlogId': '359981a6-e81a-4bbb-b43d-d73a38ee29b5',
          'status': 'Completed',
          'color': '#006400'
        },
        {
          'id': 7,
          'statusId': '7',
          'backlogId': '359981a6-e81a-4bbb-b43d-d73a38ee29b5',
          'status': 'Under Review',
          'color': '#4682B4'
        }
      ]
    },
    {
      'backlogId': '10001',
      'backlogName': 'GL IT Kanban',
      'serviceType': 'JIRA_CLOUD',
      'statusList': []
    },
    {
      'backlogId': '10000',
      'backlogName': 'GL IT Scrum',
      'serviceType': 'JIRA_CLOUD',
      'statusList': [
        {
          'id': null,
          'statusId': '10002',
          'backlogId': '10000',
          'status': 'Done',
          'color': null
        },
        {
          'id': null,
          'statusId': '10001',
          'backlogId': '10000',
          'status': 'In Progress',
          'color': null
        },
        {
          'id': null,
          'statusId': '10000',
          'backlogId': '10000',
          'status': 'To Do',
          'color': null
        }
      ]
    }
  ];

  @Input() isEditMode: boolean = false;
  @Input() ownerId?: string;

  @Input() set block(value: CreateActivityBlockRQ) {
    this.blockRQ = value as CreateItemsBoardRQ;
    this.initForm();
  }

  @Output() onBlockUpdate: EventEmitter<OnBlockUpdateEvent<CreateItemsBoardRQ>> = new EventEmitter<OnBlockUpdateEvent<CreateItemsBoardRQ>>();

  constructor(private issueStatusService: IssueStatusService) {
    this.formGroup = new FormGroup({
      name: new FormControl(''),
      description: new FormControl(''),
      answerTimeLimit: new FormControl(null),
      maxItems: new FormControl(10),
      boardColumns: new FormArray([], Validators.compose([
        Validators.required,
        Validators.minLength(1),
        Validators.maxLength(10)
      ])),
      doneStatuses: new FormControl([]),
      inProgressStatuses: new FormControl([])
    });
    this.listenFormChanges();
  }


  ngOnInit(): void {
    if (!this.isEditMode) {
      this.addColumn();
    }
    this.loadIssueStatuses();
  }


  ngOnChanges(changes: SimpleChanges): void {
    if (changes['block'] && changes['block'].currentValue && this.isEditMode) {
      this.initForm();
    }
  }

  private listenFormChanges() {
    this.formGroup.valueChanges.subscribe(value => {
      this.sendUpdateRqValue(value);
    })
  }

  private sendUpdateRqValue(value: any) {
    const blockRQ: CreateItemsBoardRQ = value as CreateItemsBoardRQ;
    const rq: CreateItemsBoardRQ = {
      description: this.blockRQ?.description!,
      name: this.blockRQ?.name!,
      type: this.blockRQ?.type!,
      boardColumns: blockRQ.boardColumns,
      doneStatuses: blockRQ?.doneStatuses,
      inProgressStatuses: blockRQ?.inProgressStatuses
    };
    this.onBlockUpdate.emit({
      formGroup: this.formGroup,
      blockRQ: rq
    });
  }

  get columnsArray(): FormArray {
    return controlArray(this.formGroup, 'boardColumns');
  }

  createColumn(col?: ItemsBoardColumnDto): FormGroup {
    return new FormGroup({
      title: new FormControl(col?.title || '', Validators.compose([
        Validators.required,
        Validators.maxLength(200)
      ])),
      color: new FormControl(col?.color || randomHexColor(), Validators.compose([
        Validators.required
      ])),
      instruction: new FormControl(col?.instruction || '', Validators.compose([
        Validators.required,
        Validators.maxLength(1000)
      ])),
      columnOrder: new FormControl(col?.columnOrder || this.columnsArray.length + 1),
      maxItems: new FormControl(col?.maxItems || 10),
      statusMapping: new FormControl(col?.statusMapping || [])
    });
  }

  addColumn() {
    this.columnsArray.push(this.createColumn());
  }

  removeColumn(index: number) {
    this.columnsArray.removeAt(index);
    this.sendUpdateRqValue(this.formGroup.value);
  }

  moveColumn(index: number, direction: number) {
    const newIndex = index + direction;
    if (newIndex < 0 || newIndex >= this.columnsArray.length) {
      return;
    }
    const formArray = this.columnsArray.controls;
    const itemToMove = formArray[index];
    formArray.splice(index, 1);
    formArray.splice(newIndex, 0, itemToMove);
    this.columnsArray.updateValueAndValidity();
    this.columnsArray.controls.forEach((group: any, position) => {
      control(group, 'columnOrder').setValue(position + 1);
    })
  }


  initForm() {
    if (this.isInitialized) {
      return;
    }
    control(this.formGroup, 'name').setValue(this.blockRQ?.name || '');
    control(this.formGroup, 'description').setValue(this.blockRQ?.description || '');
    control(this.formGroup, 'doneStatuses').setValue(this.blockRQ?.doneStatuses || []);
    control(this.formGroup, 'inProgressStatuses').setValue(this.blockRQ?.inProgressStatuses || []);

    if (this.blockRQ?.boardColumns) {
      this.blockRQ?.boardColumns.forEach(value => {
        this.columnsArray.push(this.createColumn(value))
      })
    }
    this.isInitialized = true;
  }

  loadIssueStatuses() {
    if (!this.ownerId) {
      return;
    }
    // this.issueStatusService.getTeamAllIssueStatuses(this.ownerId!).subscribe({
    //   next: (statuses) => {
    //     this.statuses = statuses;
    //   }
    // })
  }

  protected readonly control = control;
  protected readonly asControl = asControl;
}
