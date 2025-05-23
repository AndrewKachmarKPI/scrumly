import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from "@angular/router";
import { ActivityTemplateService } from "../../../events/services/activity-template.service";
import { ConfirmationService, MenuItem, MessageService } from "primeng/api";
import { AuthService } from "../../../../../auth/auth.service";
import { ActivityBlockType, ActivityTemplateDto } from "../../../events/model/activity.model";
import { EventService } from "../../../events/services/event.service";
import { CopyTemplateDialogComponent } from "../../dialogs/copy-template-dialog/copy-template-dialog.component";
import { filter } from "rxjs";
import { DialogService } from "primeng/dynamicdialog";
import { getBlockColor, getBlockIcon } from '../../../../../ui-components/services/utils';

@Component({
  selector: 'activity-template-details',
  templateUrl: './activity-template-details.component.html',
  styleUrl: './activity-template-details.component.css'
})
export class ActivityTemplateDetailsComponent implements OnInit {
  activityId?: string;
  activityTemplateDto?: ActivityTemplateDto;

  constructor(private activateRoute: ActivatedRoute,
              private router: Router,
              public authService: AuthService,
              private messageService: MessageService,
              private confirmationService: ConfirmationService,
              private eventService: EventService,
              private dialogService: DialogService,
              private activityTemplateService: ActivityTemplateService) {
  }

  ngOnInit(): void {
    this.activateRoute.params.subscribe(params => {
      this.activityId = params['activityId'];
      if (this.activityId) {
        this.loadTemplate();
      }
    });
  }

  loadTemplate() {
    this.activityTemplateService.findActivityTemplateById(this.activityId!).subscribe({
      next: (data) => {
        this.activityTemplateDto = data;
      },
      error: () => {
        this.router.navigate(['/app/activity/my-templates'])
      }
    })
  }


  onScheduleActivity() {
    this.eventService.scheduleEventSidebarState.next(true);
    this.eventService.templateSelectionState.next({
      templateId: this.activityId!
    })
  }

  onStartActivity() {

  }

  getMenuItems(): MenuItem[] {
    const items = [
      {
        label: 'Copy template',
        icon: 'pi pi-copy',
        command: () => this.copyTemplate(this.activityTemplateDto!)
      }
    ];
    if (this.authService.isCurrentUser(this.activityTemplateDto?.owner?.createdById!)) {
      items.push({
        label: 'Edit template',
        icon: 'pi pi-file-edit',
        command: () => this.editTemplate(this.activityTemplateDto!)
      })
      items.push({
        label: 'Delete template',
        icon: 'pi pi-trash',
        command: () => this.onDeleteTemplate(this.activityTemplateDto!)
      })
    }
    return items;
  }


  copyTemplate(template: ActivityTemplateDto) {
    const ref = this.dialogService.open(CopyTemplateDialogComponent, {
      width: '25vw',
      breakpoints: {
        '1199px': '45vw',
        '575px': '90vw'
      },
      resizable: true,
      draggable: false,
      header: 'Copy activity template',
      data: {
        templateId: template.templateId
      }
    });
    ref.onClose
      .pipe(filter(Boolean))
      .subscribe({
        next: (res) => {
          this.editTemplate(res);
        }
      })
  }

  onDeleteTemplate(template: ActivityTemplateDto) {
    this.confirmationService.confirm({
      message: `Are you sure that you want to delete this template ?`,
      header: 'Disconnect service',
      icon: 'pi pi-exclamation-triangle',
      acceptIcon: "none",
      rejectIcon: "none",
      acceptButtonStyleClass: 'p-button-success',
      rejectButtonStyleClass: "p-button-text",
      accept: () => {
        this.deleteTemplate(template);
      }
    });
  }

  deleteTemplate(template: ActivityTemplateDto) {
    this.activityTemplateService.deleteActivityTemplate(template?.templateId!, template.owner?.ownerId!).subscribe({
      next: (template) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Successfully deleted',
          detail: 'Your template successfully deleted',
        });
        this.router.navigate(['/app/activity/my-templates'])
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Failed to delete activity template',
          detail: err.error.message,
        });
      }
    })
  }

  editTemplate(template: ActivityTemplateDto) {
    this.router.navigate([`/app/activity/${ template.templateId! }/edit`])
  }

  protected readonly getBlockColor = getBlockColor;
  protected readonly getBlockIcon = getBlockIcon;
}
