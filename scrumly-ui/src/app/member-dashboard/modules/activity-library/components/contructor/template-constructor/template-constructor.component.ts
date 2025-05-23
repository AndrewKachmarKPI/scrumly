import { Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild } from '@angular/core';
import { ActivityTemplateService } from '../../../../events/services/activity-template.service';
import { ActivityScope, ActivityTemplateDto, CreateActivityTemplateRQ } from '../../../../events/model/activity.model';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { TemplateDetailsFormComponent } from '../template-details-form/template-details-form.component';
import { TemplateTagsComponent } from '../template-tags/template-tags.component';
import { TemplateAgendaFormComponent } from '../template-agenda-form/template-agenda-form.component';
import { OrganizationService } from '../../../../organizations/services/organization.service';

@Component({
  selector: 'template-constructor',
  templateUrl: './template-constructor.component.html',
  styleUrl: './template-constructor.component.css'
})
export class TemplateConstructorComponent implements OnInit, OnChanges {
  @Input() activityId?: string;
  @Input() isEditMode: boolean = false;
  @Input() activityTemplateDto?: ActivityTemplateDto;
  previewImg?: File;
  activeTab: number = 0;
  ownerId?: string;
  createActivityTemplateRQ?: CreateActivityTemplateRQ

  @ViewChild('templateDetailsForm') templateDetailsForm?: TemplateDetailsFormComponent;
  @ViewChild('templateTagsForm') templateTagsForm?: TemplateTagsComponent;
  @ViewChild('templateAgendaFormComponent') templateAgendaForm?: TemplateAgendaFormComponent;


  constructor(private activityTemplateService: ActivityTemplateService,
              private organizationService: OrganizationService,
              private messageService: MessageService,
              private router: Router) {
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['activityTemplateDto'] && changes['activityTemplateDto'].currentValue) {
      if (this.isEditMode) {
        this.initTemplateForm();
        this.ownerId = this.activityTemplateDto?.owner?.ownerId!;
      }
    }
  }


  onNextAction(nextCallback: any) {
    if (!this.isStepValid(this.activeTab)) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Invalid details',
        detail: `Check form validity`,
      });
      this.markStepAsTouched(this.activeTab);
      return;
    }
    nextCallback.emit();
  }

  isStepValid(stepIndex: number): boolean {
    if (stepIndex == 0) {
      return this.templateDetailsForm?.templateDetailsForm.valid!;
    } else if (stepIndex == 1) {
      return this.templateTagsForm?.templateTagsFormGroup.valid!;
    } else if (stepIndex == 2) {
      return this.templateAgendaForm?.checkIsBlocksValid()!;
    } else if (stepIndex == 3) {
      return true;
    }
    return false;
  }

  markStepAsTouched(stepIndex: number) {
    if (stepIndex == 0) {
      this.templateDetailsForm?.templateDetailsForm.markAllAsTouched();
    } else if (stepIndex == 1) {
      this.templateTagsForm?.templateTagsFormGroup.markAllAsTouched();
    } else if (stepIndex == 2) {
      this.templateAgendaForm?.markAllAsTouched();
    }
  }

  isNextStepValid(currentIndex: number): boolean {
    return this.isStepValid(currentIndex + 1);
  }

  isPreviousStepValid(currentIndex: number): boolean {
    if (currentIndex == 0) {
      return true;
    }
    return this.isStepValid(currentIndex - 1);
  }

  onActiveStepChange() {
    if (this.activeTab != 3) {
      return;
    }
    this.collectDataForSubmit();
  }

  private collectDataForSubmit() {
    const details = this.templateDetailsForm?.currentValue;
    const tags: string[] = this.templateTagsForm?.currentValue!;
    this.previewImg = details.previewImage as File;
    this.createActivityTemplateRQ = {
      name: details.name,
      description: details.description,
      tags: tags,
      activityType: details.activityType,
      ownerId: details.ownerId,
      scope: ActivityScope.PRIVATE,
      blocks: this.templateAgendaForm?.blocks!
    };
    if (!this.activityTemplateDto) {
      this.activityTemplateDto = { };
    }
    const blocks = this.templateAgendaForm?.dtoBlockList!;
    const newBlocks = this.templateAgendaForm?.newBlockList!;
    this.activityTemplateDto.name = details.name;
    this.activityTemplateDto.description = details.description;
    this.activityTemplateDto.tags = tags;
    this.activityTemplateDto.type = this.templateDetailsForm?.selectedType!;
    this.activityTemplateDto.blocks = blocks;
    this.activityTemplateDto.newBlocks = newBlocks;
  }

  onEditActivityTemplate() {
    console.log("this.activityTemplateDto:", this.activityTemplateDto);
    this.activityTemplateService.updateActivityTemplate(this.activityId!, this.activityTemplateDto!, this.previewImg!).subscribe({
      next: (template) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Template update',
          detail: `Your custom activity template successfully updated`,
        });
        this.router.navigate([ '/app/activity/my-templates' ])
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Failed to update activity template',
          detail: err.error.message,
        });
      }
    });
  }


  onCreateActivityTemplate() {
    this.activityTemplateService.createActivityTemplate(this.createActivityTemplateRQ!, this.previewImg!).subscribe({
      next: (template) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Template created',
          detail: `Your custom activity template successfully created`,
        });
        this.router.navigate([ '/app/activity/my-templates' ])
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Failed to create activity',
          detail: err.error.message,
        });
      }
    });
  }

  initTemplateForm() {
    if (!this.activityTemplateDto) {
      return;
    }
    this.templateDetailsForm?.initValue(this.activityTemplateDto);
    this.templateTagsForm?.initValue(this.activityTemplateDto);
    this.templateAgendaForm?.initValue(this.activityTemplateDto);
  }
}
