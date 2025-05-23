import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { BacklogService } from '../../services/backlog.service';
import { MessageService } from 'primeng/api';
import { Router } from '@angular/router';
import { control, specialCharacterValidator } from '../../../../../ui-components/services/utils';
import { IssueService } from '../../services/issue.service';
import { BacklogDto, IssueDto } from '../../model/backlog.model';

@Component({
  selector: 'app-backlog-issue-dialog',
  templateUrl: './backlog-issue-dialog.component.html',
  styleUrl: './backlog-issue-dialog.component.css'
})
export class BacklogIssueDialogComponent {
  backlogId?: string;
  teamId?: string;
  isCreateMode: boolean = true;
  public formGroup!: FormGroup;

  constructor(public ref: DynamicDialogRef,
              public config: DynamicDialogConfig,
              private issueService: IssueService,
              public backlogService: BacklogService,
              private messageService: MessageService,
              private router: Router) {
    this.backlogId = this.config.data.backlogId;
    this.teamId = this.config.data.teamId;
    this.isCreateMode = this.config.data.isCreateMode;
    this.formGroup = new FormGroup({
      title: new FormControl('', Validators.compose([
        specialCharacterValidator, Validators.required, Validators.maxLength(200)
      ])),
      description: new FormControl('', Validators.compose([
        Validators.maxLength(10000)
      ])),
      status: new FormControl('', Validators.compose([
        Validators.required
      ])),
      type: new FormControl('', Validators.compose([
        Validators.required
      ])),
      assignee: new FormControl('', Validators.compose([
        Validators.required
      ]))
    });
  }


  onSubmitForm() {
    if (!this.formGroup.valid) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Invalid form',
        detail: 'Check form validity',
      })
      return;
    }
    const rq: IssueDto = {
      title: control(this.formGroup, 'title').value,
      description: control(this.formGroup, 'description').value,
      backlogId: this.backlogId!,
      teamId: this.teamId!,
      status: control(this.formGroup, 'status').value,
      issueType: control(this.formGroup, 'type').value,
      assignee: control(this.formGroup, 'assignee').value,
    };
    this.issueService.createIssue(rq).subscribe({
      next: (response) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Backlog created',
          detail: 'You have successfully created new organization',
        });
        this.ref.close(response);
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Failed backlog created',
          detail: 'Error while creating backlog check form validity',
        })
      }
    })
  }

  closeDialog() {
    this.ref.close(false);
  }


  protected readonly control = control;

}
