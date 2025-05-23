import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import {
  CreateActivityBlockRQ,
  CreateEstimateBlockRQ,
  EstimationMethod
} from "../../../../../events/model/activity.model";
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { OnBlockUpdateEvent } from "../create-activity-block/create-activity-block.component";
import { control, specialCharacterValidator } from "../../../../../../../ui-components/services/utils";

@Component({
  selector: 'create-estimate-block',
  templateUrl: './create-estimate-block.component.html',
  styleUrl: './create-estimate-block.component.css'
})
export class CreateEstimateBlockComponent implements OnInit, OnChanges {
  blockRQ?: CreateEstimateBlockRQ;
  estimateBlockForm: FormGroup;
  isInitialized: boolean = false;

  estimationMethods = [
    {
      label: 'Planning Poker',
      method: EstimationMethod.PLANNING_POKER
    },
    {
      label: 'Bucket System',
      method: EstimationMethod.BUCKET_SYSTEM
    }
  ]

  @Input() isEditMode: boolean = false;

  @Input() set block(value: CreateActivityBlockRQ) {
    this.blockRQ = value as CreateEstimateBlockRQ;
    this.initForm();
  }

  @Output() onBlockUpdate: EventEmitter<OnBlockUpdateEvent<CreateEstimateBlockRQ>> = new EventEmitter<OnBlockUpdateEvent<CreateEstimateBlockRQ>>();

  constructor() {
    this.estimateBlockForm = new FormGroup({
      estimateMethod: new FormControl("", Validators.compose([
        Validators.required,
      ])),
      name: new FormControl("", Validators.compose([
        Validators.required, Validators.maxLength(200),
        specialCharacterValidator
      ])),
      scale: new FormControl([], Validators.compose([
        Validators.required, Validators.maxLength(30)
      ])),
    });
    this.listenFormChanges();
  }

  ngOnInit(): void {

  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['block'] && changes['block'].currentValue && this.isEditMode) {
      this.initForm();
    }
  }

  private listenFormChanges() {
    this.estimateBlockForm.valueChanges.subscribe(value => {
      this.sendUpdateRqValue(value);
    })
  }

  private sendUpdateRqValue(value: any) {
    const rq: CreateEstimateBlockRQ = {
      description: this.blockRQ?.description!,
      name: this.blockRQ?.name!,
      type: this.blockRQ?.type!,
      createScaleRQ: {
        name: value.name,
        scale: value.scale
      },
      estimateMethod: value.estimateMethod
    };
    this.onBlockUpdate.emit({
      formGroup: this.estimateBlockForm,
      blockRQ: rq
    });
  }

  initForm() {
    if (this.isInitialized) {
      return;
    }
    this.control(this.estimateBlockForm, 'estimateMethod').setValue(this.blockRQ?.estimateMethod);
    this.control(this.estimateBlockForm, 'name').setValue(this.blockRQ?.createScaleRQ?.name);
    this.control(this.estimateBlockForm, 'scale').setValue(this.blockRQ?.createScaleRQ?.scale!);

    this.isInitialized = true;
  }

  protected readonly control = control;
}
