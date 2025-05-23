import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { OrganizationService } from '../../../organizations/services/organization.service';
import { MessageService } from 'primeng/api';
import { Router } from '@angular/router';
import { control, specialCharacterValidator } from '../../../../../ui-components/services/utils';
import { CreateOrganizationRQ } from '../../../organizations/model/organization.model';
import { BacklogService } from '../../services/backlog.service';
import { BacklogDto } from '../../model/backlog.model';

@Component({
  selector: 'app-create-backlog-dialog',
  templateUrl: './create-backlog-dialog.component.html',
  styleUrl: './create-backlog-dialog.component.css'
})
export class CreateBacklogDialogComponent {
  teamId?: string;
  public formGroup!: FormGroup;

  constructor(public ref: DynamicDialogRef,
              public config: DynamicDialogConfig,
              public backlogService: BacklogService,
              private messageService: MessageService,
              private router: Router) {
    this.teamId = this.config.data.teamId;
    this.formGroup = new FormGroup({
      backlogName: new FormControl('', Validators.compose([
        specialCharacterValidator, Validators.required, Validators.maxLength(200)
      ])),
      issueIdentifier: new FormControl('', Validators.compose([
        specialCharacterValidator, Validators.maxLength(2)
      ])),
    })
  }


  onSubmitForm() {
    if (!this.formGroup.valid) {
      this.messageService.add({
        severity: 'warning',
        summary: 'Invalid form',
        detail: 'Check form validity',
      })
      return;
    }
    const rq: BacklogDto = {
      name: control(this.formGroup, 'backlogName').value,
      issueIdentifier: control(this.formGroup, 'issueIdentifier').value,
    };
    this.backlogService.createBacklog(this.teamId!, rq).subscribe({
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
