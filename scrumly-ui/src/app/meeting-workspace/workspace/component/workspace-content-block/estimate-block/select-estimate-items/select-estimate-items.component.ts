import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import {
  IntegrationService
} from '../../../../../../member-dashboard/modules/integration/services/integration.service';
import { TabViewChangeEvent } from 'primeng/tabview';
import { IntegrationServiceType } from '../../../../../../member-dashboard/models/integration.model';
import { TeamMetadata } from '../../../../model/activity-room.model';
import { filter, Subject, takeUntil } from 'rxjs';
import { control, specialCharacterValidator } from '../../../../../../ui-components/services/utils';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { IssueService } from '../../../../services/issue.service';
import { IssueShortInfo } from '../../../../model/issues.model';
import { CheckboxChangeEvent } from 'primeng/checkbox';
import { ActivityRoomActionsService } from '../../../../services/activity-room-actions.service';
import {
  BacklogIssueDialogComponent
} from '../../../../../../member-dashboard/modules/backlog/dialogs/backlog-issue-dialog/backlog-issue-dialog.component';
import { DialogService } from 'primeng/dynamicdialog';

export interface TabConfig {
  index: number;
  service: IntegrationServiceType;
  logo: string;
  name: string;
  isConnected: boolean | undefined;
  isExternal: boolean;
  issues?: IssueShortInfo[]
}


@Component({
  selector: 'select-estimate-items',
  templateUrl: './select-estimate-items.component.html',
  styleUrl: './select-estimate-items.component.css'
})
export class SelectEstimateItemsComponent implements OnInit, OnDestroy {
  @Input() blockId?: string;
  @Input() activityId?: string;
  @Input() teamMetadata?: TeamMetadata;
  @Input() teamId?: string;
  @Input() styleClass: string = 'w-fit select-item-form';

  destroy$: Subject<void> = new Subject<void>();
  filtersGroup: FormGroup;
  tabConfig: TabConfig[] = [
    {
      index: 0,
      service: IntegrationServiceType.SCRUMLY,
      logo: 'SCRUMLY.png',
      name: 'Scrumly',
      isConnected: true,
      isExternal: false
    },
    {
      index: 1,
      service: IntegrationServiceType.JIRA_CLOUD,
      logo: 'JIRA_CLOUD.png',
      name: 'Jira Cloud',
      isConnected: undefined,
      isExternal: true,
    },
  ];
  activeTab: TabConfig = this.tabConfig[0];
  isLoadIssues: boolean = false;

  @Input() selectedIssues: IssueShortInfo[] = [];
  @Output() onSelectIssuesSubmit: EventEmitter<IssueShortInfo[]> = new EventEmitter<IssueShortInfo[]>();

  constructor(private issueService: IssueService,
              private integrationService: IntegrationService,
              private dialogService: DialogService,
              private activityRoomActionService: ActivityRoomActionsService) {
    this.filtersGroup = new FormGroup({
      searchIssue: new FormControl('', Validators.compose([
        Validators.required, specialCharacterValidator, Validators.maxLength(100)
      ])),
    })
  }

  ngOnInit(): void {
    this.onServiceConnected();
    this.loadTopIssues();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onTabChange(event: TabViewChangeEvent) {
    this.selectedIssues = [];
    this.activeTab = this.tabConfig.find(tab => tab.index === event.index)!;
    this.checkIfServiceConnected(this.activeTab);
    this.loadTopIssues();
  }

  checkIfServiceConnected(tab: TabConfig) {
    if (!tab.isExternal || tab.isConnected) {
      return;
    }
    this.integrationService.isConnected(this.teamMetadata?.organizationId!, tab?.service!)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (isConnected) => {
          tab.isConnected = isConnected;
        }
      });
  }

  connectJira() {
    const service = IntegrationServiceType.JIRA_CLOUD;
    const callback = this.integrationService.getIntegrationCallback(service);
    if (callback) {
      callback(this.teamMetadata?.organizationId!);
    }
  }


  onServiceConnected() {
    this.integrationService.onServiceConnectionChange
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.checkIfServiceConnected(this.activeTab);
        }
      });
  }

  loadTopIssues() {
    if (this.activeTab.issues && this.activeTab?.issues?.length > 0) {
      return;
    }
    const topLimit = 10;
    const serviceType = this.activeTab.service;
    if (serviceType === IntegrationServiceType.JIRA_CLOUD) {
      this.sendLoadTopIssues(IntegrationServiceType.JIRA_CLOUD, this.teamMetadata?.organizationId!, topLimit);
    } else if (serviceType === IntegrationServiceType.SCRUMLY) {
      this.sendLoadTopIssues(IntegrationServiceType.SCRUMLY, this.teamMetadata?.teamId!, topLimit);
    }
  }

  searchIssue() {
    if (this.filtersGroup.invalid) {
      return;
    }
    const query = control(this.filtersGroup, 'searchIssue').value;
    const serviceType = this.activeTab.service;
    if (serviceType === IntegrationServiceType.JIRA_CLOUD) {
      this.sendLoadIssues(IntegrationServiceType.JIRA_CLOUD, query, this.teamMetadata?.organizationId!);
    } else if (serviceType === IntegrationServiceType.SCRUMLY) {
      this.sendLoadIssues(IntegrationServiceType.SCRUMLY, query, this.teamMetadata?.teamId!);
    }
  }

  private sendLoadIssues(serviceType: IntegrationServiceType, query: string, connectionId: string) {
    this.isLoadIssues = true;
    this.issueService.searchIssues(serviceType, connectionId, query)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (issues) => {
          this.isLoadIssues = false;
          this.activeTab.issues = issues;
        }
      });
  }

  private sendLoadTopIssues(serviceType: IntegrationServiceType, connectionId: string, topLimit: number) {
    this.isLoadIssues = true;
    this.issueService.loadTopIssues(serviceType, connectionId, topLimit)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (issues) => {
          this.isLoadIssues = false;
          this.activeTab.issues = issues;
        }
      });
  }

  onSelectIssue(event: CheckboxChangeEvent, issue: IssueShortInfo) {
    if (event.checked) {
      this.selectedIssues.push(issue);
    } else {
      const idx = this.selectedIssues.findIndex(iss => iss.issueId === issue.issueId);
      this.selectedIssues.splice(idx, 1);
    }
  }

  onSubmitSelectIssue() {
    if (this.selectedIssues && this.selectedIssues.length) {
      this.onSelectIssuesSubmit.emit(this.selectedIssues);
    }
  }

  onCreateIssue() {
    const ref = this.dialogService.open(BacklogIssueDialogComponent, {
      width: '45vw',
      breakpoints: {
        '1199px': '45vw',
        '575px': '90vw'
      },
      header: 'Create issue',
      draggable: true,
      modal: true,
      data: {
        teamId: this.teamId!,
        isCreateMode: true
      }
    });
    ref.onClose
      .pipe(filter(Boolean))
      .subscribe({
        next: (res) => {
          if (res) {
            this.searchIssue();
          }
        }
      })
  }

  protected readonly IntegrationServiceType = IntegrationServiceType;
  protected readonly control = control;
}
