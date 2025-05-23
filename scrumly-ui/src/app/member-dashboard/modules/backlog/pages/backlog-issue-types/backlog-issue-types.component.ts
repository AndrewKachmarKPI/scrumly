import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { IssueStatusDto, IssueTypeDto } from '../../model/backlog.model';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { IssueStatusService } from '../../services/issue-status.service';
import { MessageService } from 'primeng/api';
import { control, specialCharacterValidator } from '../../../../../ui-components/services/utils';
import { OverlayPanel } from 'primeng/overlaypanel';
import { IssueTypeService } from '../../services/issue-type.service';

@Component({
  selector: 'app-backlog-issue-types',
  templateUrl: './backlog-issue-types.component.html',
  styleUrl: './backlog-issue-types.component.css'
})
export class BacklogIssueTypesComponent implements OnInit, OnDestroy {
  backlogId?: string;
  types: IssueTypeDto[] = [];
  clonedProducts: { [s: number]: IssueTypeDto } = {};

  public formGroup: FormGroup;

  private destroy$ = new Subject<void>();


  constructor(private activateRoute: ActivatedRoute,
              private issueTypeService: IssueTypeService,
              private messageService: MessageService) {
    this.formGroup = new FormGroup({
      type: new FormControl('', Validators.compose([
        Validators.required, specialCharacterValidator, Validators.maxLength(200)
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
    this.issueTypeService.getAllIssueTypes(this.backlogId!).subscribe({
      next: (types) => {
        this.types = types;
      }
    })
  }

  onCreateNewIssueType(op: OverlayPanel) {
    if (!this.formGroup.valid) {
      return;
    }
    const rq: IssueTypeDto = {
      type: control(this.formGroup, 'type').value,
      backlogId: this.backlogId!,
      isDefault: false
    }
    this.issueTypeService.createIssueType(rq).subscribe({
      next: (status) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Issue type created',
          detail: 'Your issue type successfully created!',
        });
        op.hide();
        this.formGroup.reset();
        this.formGroup.markAsUntouched();
        this.types.push(status);
      }
    })
  }


  deleteIssueStatus(issueTypeDto: IssueTypeDto) {
    this.issueTypeService.deleteIssueType(issueTypeDto.id!)
      .subscribe({
        next: () => {
          const idx = this.types.indexOf(issueTypeDto);
          this.types.splice(idx, 1);
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

  onRowEditInit(issueTypeDto: IssueTypeDto) {
    this.clonedProducts[issueTypeDto.id as number] = { ...issueTypeDto };
  }

  onRowEditSave(typeDto: IssueTypeDto) {
    if (typeDto.type && typeDto.iconUrl) {
      this.issueTypeService.updateIssueType(typeDto.id!, typeDto)
        .subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Issue type updated',
              detail: 'Your type successfully updated!',
            });
          }
        })
    }
  }

  onRowEditCancel(status: IssueStatusDto, index: number) {
    this.types[index] = this.clonedProducts[status.id as number];
    delete this.clonedProducts[status.id as number];
  }

  protected readonly control = control;
}
