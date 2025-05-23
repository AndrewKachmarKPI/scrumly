import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import {
  CreateActivityBlockRQ, CreateQuestionBlockRQ,
  CreateReflectBlockRQ, ReflectColumnDto
} from "../../../../../events/model/activity.model";
import { FormArray, FormControl, FormGroup, Validators } from "@angular/forms";
import {
  asControl,
  control,
  controlArray, randomHexColor,
  specialCharacterValidator
} from "../../../../../../../ui-components/services/utils";
import { OnBlockUpdateEvent } from "../create-activity-block/create-activity-block.component";

@Component({
  selector: 'create-reflect-block',
  templateUrl: './create-reflect-block.component.html',
  styleUrl: './create-reflect-block.component.css'
})
export class CreateReflectBlockComponent implements OnInit, OnChanges {
  blockRQ?: CreateReflectBlockRQ;
  reflectBlockForm: FormGroup;
  isInitialized: boolean = false;

  @Input() isEditMode: boolean = false;

  @Input() set block(value: CreateActivityBlockRQ) {
    this.blockRQ = value as CreateReflectBlockRQ;
    this.initForm();
  }

  @Output() onBlockUpdate: EventEmitter<OnBlockUpdateEvent<CreateReflectBlockRQ>> = new EventEmitter<OnBlockUpdateEvent<CreateReflectBlockRQ>>();

  constructor() {
    this.reflectBlockForm = new FormGroup({
      name: new FormControl(''),
      description: new FormControl(''),
      answerTimeLimit: new FormControl(null),
      reflectColumns: new FormArray([], Validators.compose([
        Validators.required,
        Validators.minLength(1),
        Validators.maxLength(10)
      ])),
      // maxReflectionsPerColumnPerUser: new FormControl(10, Validators.compose([
      //   Validators.required,
      //   Validators.min(1)
      // ])),
      // timePerColumn: new FormControl(1, Validators.compose([
      //   Validators.required,
      //   Validators.min(1)
      // ])),
      // reflectTimeLimit: new FormControl(1, Validators.compose([
      //   Validators.required,
      //   Validators.min(1)
      // ]))
    });
    this.listenFormChanges();
  }


  ngOnInit(): void {
    if (!this.isEditMode) {
      this.addColumn();
    }
  }


  ngOnChanges(changes: SimpleChanges): void {
    if (changes['block'] && changes['block'].currentValue && this.isEditMode) {
      this.initForm();
    }
  }

  private listenFormChanges() {
    this.reflectBlockForm.valueChanges.subscribe(value => {
      this.sendUpdateRqValue(value);
    })
  }

  private sendUpdateRqValue(value: any) {
    const blockRQ: CreateReflectBlockRQ = value as CreateReflectBlockRQ;
    const rq: CreateReflectBlockRQ = {
      description: this.blockRQ?.description!,
      name: this.blockRQ?.name!,
      type: this.blockRQ?.type!,
      reflectColumns: blockRQ.reflectColumns,
      // maxReflectionsPerColumnPerUser: blockRQ.maxReflectionsPerColumnPerUser,
      // timePerColumn: blockRQ.timePerColumn,
      // reflectTimeLimit: blockRQ.reflectTimeLimit
    };
    this.onBlockUpdate.emit({
      formGroup: this.reflectBlockForm,
      blockRQ: rq
    });
  }

  get columnsArray(): FormArray {
    return controlArray(this.reflectBlockForm, 'reflectColumns');
  }

  createColumn(col?: ReflectColumnDto): FormGroup {
    return new FormGroup({
      title: new FormControl(col?.title || "", Validators.compose([
        Validators.required,
        Validators.maxLength(200)
      ])),
      color: new FormControl(col?.color || randomHexColor(), Validators.compose([
        Validators.required
      ])),
      instruction: new FormControl(col?.instruction || "", Validators.compose([
        Validators.required,
        Validators.maxLength(1000)
      ])),
      columnOrder: new FormControl(col?.columnOrder || this.columnsArray.length + 1)
    });
  }

  addColumn() {
    this.columnsArray.push(this.createColumn());
  }

  removeColumn(index: number) {
    this.columnsArray.removeAt(index);
    this.sendUpdateRqValue(this.reflectBlockForm.value);
  }

  moveColumn(index: number, direction: number) {
    const newIndex = index + direction;
    if (newIndex < 0 || newIndex >= this.columnsArray.length) {
      return;
    }
    const formArray = this.columnsArray.controls;
    const itemToMove = formArray[index];
    formArray.splice(index, 1);
    formArray.splice(newIndex, 0, itemToMove);
    this.columnsArray.updateValueAndValidity();
    this.columnsArray.controls.forEach((group:any, position) => {
      control(group, 'columnOrder').setValue(position + 1);
    })
  }


  initForm() {
    if (this.isInitialized) {
      return;
    }
    control(this.reflectBlockForm, 'name').setValue(this.blockRQ?.name || '');
    control(this.reflectBlockForm, 'description').setValue(this.blockRQ?.description || '');
    control(this.reflectBlockForm, 'answerTimeLimit').setValue(this.blockRQ?.reflectTimeLimit || '');
    // control(this.reflectBlockForm, 'maxReflectionsPerColumnPerUser').setValue(this.blockRQ?.maxReflectionsPerColumnPerUser || '');
    // control(this.reflectBlockForm, 'timePerColumn').setValue(this.blockRQ?.timePerColumn || '');
    // control(this.reflectBlockForm, 'reflectTimeLimit').setValue(this.blockRQ?.reflectTimeLimit || '');
    if (this.blockRQ?.reflectColumns) {
      this.blockRQ?.reflectColumns.forEach(value => {
        this.columnsArray.push(this.createColumn(value))
      })
    }
    this.isInitialized = true;
  }

  protected readonly control = control;
  protected readonly asControl = asControl;
}
