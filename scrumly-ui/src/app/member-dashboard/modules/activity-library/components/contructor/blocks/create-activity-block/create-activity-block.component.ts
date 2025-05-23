import { Component, EventEmitter, Input, Output } from '@angular/core';
import {
  ActivityBlockType,
  CreateActivityBlockRQ,
} from "../../../../../events/model/activity.model";
import { FormGroup } from "@angular/forms";

export interface OnBlockUpdateEvent<T> {
  formGroup: FormGroup,
  blockRQ: T
}


@Component({
  selector: 'create-activity-block',
  templateUrl: './create-activity-block.component.html',
  styleUrl: './create-activity-block.component.css'
})
export class CreateActivityBlockComponent {
  @Input() isEditMode: boolean = false;
  @Input() block?: CreateActivityBlockRQ;
  @Input() ownerId?: string;
  @Output() onBlockUpdate = new EventEmitter();


  protected readonly ActivityBlockType = ActivityBlockType;
}
