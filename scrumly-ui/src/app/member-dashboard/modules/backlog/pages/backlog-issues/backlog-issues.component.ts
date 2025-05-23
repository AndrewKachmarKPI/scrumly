import { Component, OnDestroy, OnInit } from '@angular/core';
import { filter, Subject, takeUntil } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { CompareOption, PageDto, SearchOperators } from '../../../../../ui-components/models/search-filter.model';
import { IssueDto, IssueStatusDto } from '../../model/backlog.model';
import { IssueService } from '../../services/issue.service';
import { ConfirmationService, MessageService } from 'primeng/api';
import { SearchFilterService } from '../../../../../ui-components/services/search-filter.service';
import {
  control,
  defaultPageOptions,
  getMemberRoleSeverity,
  specialCharacterValidator,
  trackById,
  trackByIssueId
} from '../../../../../ui-components/services/utils';
import { OrganizationMemberRole } from '../../../organizations/model/organization.model';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { PaginatorState } from 'primeng/paginator';
import { DialogService } from 'primeng/dynamicdialog';
import { BacklogIssueDialogComponent } from '../../dialogs/backlog-issue-dialog/backlog-issue-dialog.component';
import { IssueStatusService } from '../../services/issue-status.service';
import { UserProfileDto } from '../../../../../auth/auth.model';
import { BacklogService } from '../../services/backlog.service';

@Component({
  selector: 'app-backlog-issues',
  templateUrl: './backlog-issues.component.html',
  styleUrl: './backlog-issues.component.css'
})
export class BacklogIssuesComponent implements OnInit, OnDestroy {
  backlogId?: string;

  issues?: PageDto<IssueDto>;
  filtersGroup: FormGroup;
  issueStatuses: IssueStatusDto[] = [];
  issueAssignee: UserProfileDto[] = [];
  isRedirect: boolean = true;

  private destroy$ = new Subject<void>();


  constructor(private activateRoute: ActivatedRoute,
              private issueService: IssueService,
              private filterService: SearchFilterService,
              private dialogService: DialogService,
              private issueStatusService: IssueStatusService,
              private backlogService: BacklogService,
              private confirmationService: ConfirmationService,
              private messageService: MessageService,
              private router: Router) {
    this.filtersGroup = new FormGroup({
      searchIssue: new FormControl('', Validators.compose([
        specialCharacterValidator, Validators.maxLength(100)
      ])),
    })
  }

  ngOnInit(): void {
    this.filterService.changePagination({
      pageSize: 100,
      pageIndex: 0
    })
    this.activateRoute.queryParams
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        this.backlogId = params['backlogId'];
        this.loadIssues();
        this.loadIssueCommonData();
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
    this.filterService.resetFilterService();
  }

  loadIssues() {
    this.filterService.applySearchFilter({
      property: 'backlogId',
      operator: SearchOperators.EQUALS,
      value: this.backlogId!,
      compareOption: CompareOption.AND
    })
    this.issueService.searchIssues(this.filterService.searchQuery)
      .subscribe({
        next: (page) => {
          this.issues = page;
        }
      })
  }

  onSearchChange() {
    this.filterService.applySearchFilters([
      {
        value: this.control(this.filtersGroup, 'searchIssue').value,
        property: 'title',
        compareOption: CompareOption.OR,
        operator: SearchOperators.LIKE
      },
      {
        value: this.control(this.filtersGroup, 'searchIssue').value,
        property: 'issueKey',
        compareOption: CompareOption.OR,
        operator: SearchOperators.LIKE
      }
    ]);
    this.loadIssues();
  }

  onPageChange(page: PaginatorState) {
    this.filterService.changePagination({
      pageSize: page.rows!,
      pageIndex: page.page!
    });
    this.loadIssues();
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
        backlogId: this.backlogId!,
        isCreateMode: true
      }
    });
    ref.onClose
      .pipe(filter(Boolean))
      .subscribe({
        next: (res) => {
          if (res) {
            this.loadIssues();
          }
        }
      })
  }

  loadIssueCommonData() {
    this.issueStatusService.getAllIssueStatuses(this.backlogId!).subscribe({
      next: (data) => {
        this.issueStatuses = data;
      }
    });
    this.backlogService.getAllAssignee(this.backlogId!).subscribe({
      next: (data) => {
        this.issueAssignee = data;
      }
    })
  }

  onChangeIssueStatus(issue: IssueDto, status: IssueStatusDto) {
    issue.status = status;
    this.issueService.updateIssue(issue.id!, issue)
      .subscribe({
        next: () => {

        }
      });
  }

  onChangeIssueAssignee(issue: IssueDto, assignee: UserProfileDto) {
    issue.assignee = assignee;
    this.issueService.updateIssue(issue.id!, issue)
      .subscribe({
        next: () => {

        }
      });
  }


  deleteIssue(issue: IssueDto) {
    this.confirmationService.confirm({
      message: `Are you sure that you want to delete this issue ?`,
      header: 'Delete issue',
      icon: 'pi pi-exclamation-triangle',
      acceptIcon: 'none',
      rejectIcon: 'none',
      acceptButtonStyleClass: 'p-button-success',
      rejectButtonStyleClass: 'p-button-text',
      accept: () => {
        this.issueService.deleteIssue(issue.id!)
          .subscribe({
            next: () => {
              this.messageService.add({
                severity: 'success',
                summary: 'Issue deleted',
                detail: 'You have successfully deleted issue',
              });
              this.loadIssues();
            }
          });
      }
    });
  }

  viewIssue(isRedirect: boolean, issue: IssueDto) {
    if (isRedirect) {
      this.router.navigate([ `/app/backlog/${ issue.issueKey }/issue` ]);
    }
  }

  protected readonly defaultPageOptions = defaultPageOptions;
  protected readonly control = control;
  protected readonly OrganizationMemberRole = OrganizationMemberRole;
  protected readonly getMemberRoleSeverity = getMemberRoleSeverity;
  protected readonly trackById = trackById;
  protected readonly trackByIssueId = trackByIssueId;
}
