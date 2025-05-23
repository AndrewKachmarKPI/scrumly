import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { IssueStatusService } from '../../services/issue-status.service';
import { THREE } from '@angular/cdk/keycodes';
import { IssueStatusDto } from '../../model/backlog.model';
import { MessageService } from 'primeng/api';
import { control, specialCharacterValidator } from '../../../../../ui-components/services/utils';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { OverlayPanel } from 'primeng/overlaypanel';

@Component({
  selector: 'app-backlog-issue-statuses',
  templateUrl: './backlog-issue-statuses.component.html',
  styleUrl: './backlog-issue-statuses.component.css'
})
export class BacklogIssueStatusesComponent implements OnInit, OnDestroy {
  backlogId?: string;
  statuses: IssueStatusDto[] = [];
  clonedProducts: { [s: number]: IssueStatusDto } = {};

  public formGroup: FormGroup;

  private destroy$ = new Subject<void>();


  constructor(private activateRoute: ActivatedRoute,
              private issueStatusesService: IssueStatusService,
              private messageService: MessageService) {
    this.formGroup = new FormGroup({
      status: new FormControl('', Validators.compose([
        Validators.required, specialCharacterValidator, Validators.maxLength(200)
      ])),
      color: new FormControl('', Validators.compose([
        Validators.required
      ]))
    })
  }

  ngOnInit(): void {
    this.activateRoute.queryParams
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        this.backlogId = params['backlogId'];
        this.loadIssueStatuses();
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadIssueStatuses() {
    this.issueStatusesService.getAllIssueStatuses(this.backlogId!).subscribe({
      next: (statuses) => {
        this.statuses = statuses;
      }
    })
  }

  onCreateNewIssueStatus(op: OverlayPanel) {
    if (!this.formGroup.valid) {
      return;
    }
    const rq: IssueStatusDto = {
      status: control(this.formGroup, 'status').value,
      color: control(this.formGroup, 'color').value,
      backlogId: this.backlogId!
    }
    this.issueStatusesService.createIssueStatus(rq).subscribe({
      next: (status) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Status created',
          detail: 'Your status successfully created!',
        });
        op.hide();
        this.formGroup.reset();
        this.formGroup.markAsUntouched();
        this.statuses.push(status);
      }
    })
  }


  deleteIssueStatus(statusDto: IssueStatusDto) {
    this.issueStatusesService.deleteIssueStatus(statusDto.id!)
      .subscribe({
        next: () => {
          const idx = this.statuses.indexOf(statusDto);
          this.statuses.splice(idx, 1);
          this.messageService.add({
            severity: 'success',
            summary: 'Status removed',
            detail: 'Your answer successfully removed!',
          });
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error status',
            detail: 'Your answer not removed!',
          });
        }
      });
  }

  onRowEditInit(statusDto: IssueStatusDto) {
    this.clonedProducts[statusDto.id as number] = { ...statusDto };
  }

  onRowEditSave(status: IssueStatusDto) {
    if (status.status && status.color) {
      this.issueStatusesService.updateIssueStatus(status.id!, status)
        .subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Status updated',
              detail: 'Your status successfully updated!',
            });
          }
        })
    }
  }

  onRowEditCancel(status: IssueStatusDto, index: number) {
    this.statuses[index] = this.clonedProducts[status.id as number];
    delete this.clonedProducts[status.id as number];
  }


  protected readonly control = control;
}
