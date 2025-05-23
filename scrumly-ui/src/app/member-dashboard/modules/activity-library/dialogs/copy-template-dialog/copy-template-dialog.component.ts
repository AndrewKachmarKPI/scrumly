import { Component } from '@angular/core';
import { ActivityTemplateService } from "../../../events/services/activity-template.service";
import { DynamicDialogConfig, DynamicDialogRef } from "primeng/dynamicdialog";
import { FormControl } from "@angular/forms";
import { Message, MessageService } from "primeng/api";

@Component({
  selector: 'app-copy-template-dialog',
  templateUrl: './copy-template-dialog.component.html',
  styleUrl: './copy-template-dialog.component.css'
})
export class CopyTemplateDialogComponent {
  templateId?: string;
  ownerIdControl = new FormControl("");

  constructor(public ref: DynamicDialogRef,
              public config: DynamicDialogConfig,
              private messageService: MessageService,
              private activityTemplateService: ActivityTemplateService) {
    this.templateId = this.config.data.templateId;
  }


  copyTemplate() {
    if (!this.ownerIdControl.valid) {
      this.ownerIdControl.markAllAsTouched();
      return;
    }
    this.activityTemplateService.copyActivityTemplate(this.templateId!, this.ownerIdControl.value!).subscribe({
      next: (template) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Successfully copied',
          detail: 'Your template successfully copied',
        });
        this.ref.close(template);
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Failed to copy activity template',
          detail: err.error.message,
        });
      }
    })
  }

  closeDialog() {
    this.ref.close();
  }
}
