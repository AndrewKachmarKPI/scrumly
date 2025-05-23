import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import {
  control,
  controlArray,
  joinBy,
  specialCharacterValidator
} from "../../../../../../ui-components/services/utils";
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { ActivityTypeService } from "../../../../events/services/activity-type.service";
import {
  ActivityTemplateDto,
  ActivityTypeDto,
  CreateActivityTemplateRQ
} from "../../../../events/model/activity.model";

interface UploadEvent {
  originalEvent: Event;
  files: File[];
}


@Component({
  selector: 'template-details-form',
  templateUrl: './template-details-form.component.html',
  styleUrl: './template-details-form.component.css'
})
export class TemplateDetailsFormComponent implements OnInit {
  @Input() isEditMode: boolean = false;
  activityTypes: ActivityTypeDto[] = [];
  public templateDetailsFormGroup!: FormGroup;

  constructor(private activityTypeService: ActivityTypeService) {
  }

  ngOnInit(): void {
    this.templateDetailsFormGroup = new FormGroup({
      name: new FormControl<string>("", Validators.compose([
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(100),
        specialCharacterValidator
      ])),
      description: new FormControl<string>("", Validators.compose([
        Validators.required
      ])),
      activityType: new FormControl<string>("", Validators.compose([
        Validators.required,
        specialCharacterValidator
      ])),
      ownerId: new FormControl<string>("", Validators.compose([
        Validators.required
      ])),
      previewImage: new FormControl()
    });
    this.loadTypes();
  }


  get templateDetailsForm() {
    return this.templateDetailsFormGroup;
  }

  loadTypes() {
    this.activityTypeService.getAllActivityTypes().subscribe({
      next: (types) => {
        this.activityTypes = types;
      }
    })
  }

  get currentValue() {
    return this.templateDetailsFormGroup.value;
  }

  get selectedType() {
    return this.activityTypes.find(value => value.type ===
      this.control(this.templateDetailsFormGroup, 'activityType').value);
  }

  initValue(dto: ActivityTemplateDto) {
    this.templateDetailsFormGroup.setValue({
      name: dto.name,
      description: dto.description,
      activityType: dto.type?.type,
      previewImage: dto.previewImageId,
      ownerId: dto.owner?.ownerId
    });
    this.control(this.templateDetailsFormGroup, 'ownerId').disable();
  }

  protected readonly control = control;
  protected readonly joinBy = joinBy;
}
