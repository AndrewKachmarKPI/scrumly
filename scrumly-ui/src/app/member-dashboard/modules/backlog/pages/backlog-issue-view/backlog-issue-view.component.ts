import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { IssueService } from '../../services/issue.service';
import { SearchFilterService } from '../../../../../ui-components/services/search-filter.service';
import { filter, Subject, takeUntil } from 'rxjs';
import { IssueDto, IssueStatusDto } from '../../model/backlog.model';
import { UserProfileDto } from '../../../../../auth/auth.model';
import { control, specialCharacterValidator } from '../../../../../ui-components/services/utils';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { BacklogService } from '../../services/backlog.service';
import { ConfirmationService, MenuItem, MessageService } from 'primeng/api';
import { toDate } from 'date-fns-tz';
import { DatePipe } from '@angular/common';
import { BacklogIssueDialogComponent } from '../../dialogs/backlog-issue-dialog/backlog-issue-dialog.component';
import { DialogService } from 'primeng/dynamicdialog';
import { ExportIssueDialogComponent } from '../../dialogs/export-issue-dialog/export-issue-dialog.component';

@Component({
  selector: 'app-backlog-issue-view',
  templateUrl: './backlog-issue-view.component.html',
  styleUrl: './backlog-issue-view.component.css',
  providers: [ DatePipe ]
})
export class BacklogIssueViewComponent implements OnInit {
  issueDto?: IssueDto;
  issueFormGroup: FormGroup;

  isEditing: boolean = false;
  organizationId?: string;
  menuItems: MenuItem[] = [
    {
      label: 'Export issue',
      icon: 'pi pi-upload',
      command: (event) => this.onExportIssue()
    },
    {
      label: 'Delete issue',
      icon: 'pi pi-trash',
      styleClass: 'text-danger-500',
      command: (event) => this.deleteIssue()
    },
  ];
  private issueKey?: string;
  private destroy$ = new Subject<void>();


  constructor(private activateRoute: ActivatedRoute,
              private issueService: IssueService,
              private messageService: MessageService,
              private confirmationService: ConfirmationService,
              private datePipe: DatePipe,
              private dialogService: DialogService,
              private router: Router) {
    this.issueFormGroup = new FormGroup({
      title: new FormControl('', Validators.compose([
        Validators.required
      ])),
      description: new FormControl('', Validators.compose([
        Validators.required
      ])),
      estimation: new FormControl('', Validators.compose([
        specialCharacterValidator
      ])),
      createdDate: new FormControl('')
    });
  }

  ngOnInit(): void {
    this.activateRoute.params
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        this.issueKey = params['issueKey'];
        this.loadIssue();
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadIssue() {
    this.issueService.getIssueByKey(this.issueKey!)
      .subscribe({
        next: (issueDto) => {
          this.issueDto = issueDto;
          this.initFormGroup();
        }
      })
  }

  initFormGroup() {
    this.issueFormGroup?.setValue({
      title: this.issueDto?.title!,
      description: this.issueDto?.description!,
      estimation: this.issueDto?.issueEstimation ? this.issueDto.issueEstimation.estimation : '',
      createdDate: this.datePipe.transform(this.issueDto?.createdDateTime, 'fullDate')
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

  onSaveIssueChanges() {
    if (!this.issueFormGroup.valid) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Invalid issue form',
        detail: 'Check form validity',
      });
      return;
    }

    this.issueDto!.title = control(this.issueFormGroup, 'title').value;
    this.issueDto!.description = control(this.issueFormGroup, 'description').value;
    if (control(this.issueFormGroup, 'estimation').value) {
      this.issueDto!.issueEstimation = {
        estimation: control(this.issueFormGroup, 'estimation').value
      }
    }

    this.issueService.updateIssue(this.issueDto?.id!, this.issueDto!)
      .subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Issue updated',
            detail: 'Issue changes successfully saved',
          });
          this.issueFormGroup.markAsUntouched();
        }
      });
  }

  deleteIssue() {
    const issue = this.issueDto!;
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
              this.router.navigate([ '/app/backlog/issues' ], {
                queryParams: {
                  backlogId: issue.backlogId
                }
              })
            }
          });
      }
    });
  }

  onExportIssue() {
    const ref = this.dialogService.open(ExportIssueDialogComponent, {
      width: '45vw',
      breakpoints: {
        '1199px': '45vw',
        '575px': '90vw'
      },
      header: 'Export issue',
      draggable: true,
      modal: true,
      data: {
        issue: this.issueDto!,
        organizationId: this.organizationId!
      }
    });
    ref.onClose
      .pipe(filter(Boolean))
      .subscribe({
        next: (res) => {
          if (res) {
            this.issueDto = res;
          }
        }
      })
  }

  toggleEditing() {
    this.isEditing = true;
    this.control(this.issueFormGroup, 'title').setValue(this.issueDto?.title!);
  }

  onCancelEditing() {
    this.isEditing = false;
    this.control(this.issueFormGroup, 'title').reset();
  }

  onAcceptEditing() {
    this.isEditing = false;
    if (this.control(this.issueFormGroup, 'title').valid) {
      this.onSaveIssueChanges();
    }
  }

  getDate(date: string): Date {
    return new Date(date);
  }

  protected readonly control = control;
  protected readonly toDate = toDate;
}
