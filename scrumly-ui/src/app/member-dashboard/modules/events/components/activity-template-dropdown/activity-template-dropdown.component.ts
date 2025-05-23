import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { AbstractControl, FormControl } from '@angular/forms';
import { SelectItemGroup } from 'primeng/api';
import { ActivityTemplateService } from '../../services/activity-template.service';
import { ActivityTemplateDto, ActivityTemplateGroupDto } from '../../model/activity.model';
import { DropdownChangeEvent } from 'primeng/dropdown';

@Component({
  selector: 'activity-template-dropdown',
  templateUrl: './activity-template-dropdown.component.html',
  styleUrl: './activity-template-dropdown.component.css'
})
export class ActivityTemplateDropdownComponent implements OnChanges {
  @Input() ownerId?: string;
  @Input() label: string = '';
  @Input() placeholder: string = '';
  @Input() disabled: boolean = false;
  @Input() showClear: boolean = false;
  @Input() control = new FormControl('');

  @Output() onSelectTemplate: EventEmitter<ActivityTemplateDto> = new EventEmitter<ActivityTemplateDto>();

  templateGroups: ActivityTemplateGroupDto[] = [];
  groupedTemplates: SelectItemGroup[] = [];

  constructor(private activityTemplateService: ActivityTemplateService) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['disabled']) {
      if (changes['disabled'].currentValue) {
        this.control.disable();
      } else {
        this.control.enable();
      }
    }
    this.findTemplates();
  }


  ngOnInit(): void {
  }


  private findTemplates() {
    if (!this.ownerId) {
      return;
    }
    this.activityTemplateService.findMyActivityTemplatesGroup(this.ownerId).subscribe({
      next: (group) => {
        this.templateGroups = group;
        this.groupedTemplates = group.map(group => {
          return {
            label: group.group,
            value: group.group,
            items: group.templates.map(template => {
              return {
                label: template.name,
                value: template.templateId
              }
            })
          }
        })
      }
    })
  }

  onChangeSelection(event: DropdownChangeEvent) {
    const template = this.templateGroups
      .flatMap(group => group.templates)
      .find(temp => temp.templateId === event.value);
    this.onSelectTemplate.emit(template);
  }

  get hasError() {
    return this.control.errors && (this.control.touched || this.control.dirty);
  }

  get isRequired() {
    if (!this.control.validator) {
      return;
    }
    const validator = this.control.validator!({} as AbstractControl);
    return validator && validator['required'];
  }
}
