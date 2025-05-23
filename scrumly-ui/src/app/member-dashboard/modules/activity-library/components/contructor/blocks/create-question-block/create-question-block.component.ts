import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import {
  CreateActivityBlockRQ,
  CreateQuestionBlockRQ,
  CreateQuestionRQ, QuestionDto
} from "../../../../../events/model/activity.model";
import { FormArray, FormControl, FormGroup, Validators } from "@angular/forms";
import {
  asControl,
  control,
  controlArray,
  specialCharacterValidator
} from "../../../../../../../ui-components/services/utils";
import { OnBlockUpdateEvent } from "../create-activity-block/create-activity-block.component";

@Component({
  selector: 'create-question-block',
  templateUrl: './create-question-block.component.html',
  styleUrl: './create-question-block.component.css'
})
export class CreateQuestionBlockComponent implements OnInit, OnChanges {
  blockRQ?: CreateQuestionBlockRQ;
  questionBlockForm: FormGroup;
  isInitialized: boolean = false;
  @Input() isEditMode: boolean = false;

  @Input() set block(value: CreateActivityBlockRQ) {
    this.blockRQ = value as CreateQuestionBlockRQ;
  }

  @Output() onBlockUpdate: EventEmitter<OnBlockUpdateEvent<CreateQuestionBlockRQ>> = new EventEmitter<OnBlockUpdateEvent<CreateQuestionBlockRQ>>();

  constructor() {
    this.questionBlockForm = new FormGroup({
      name: new FormControl(''),
      description: new FormControl(''),
      answerTimeLimit: new FormControl(null),
      questions: new FormArray([], Validators.compose([
        Validators.required,
        Validators.minLength(1),
        Validators.maxLength(5)
      ]))
    });
    this.listenFormChanges();
  }


  ngOnInit(): void {
    if (!this.isEditMode) {
      this.addQuestion();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['block'] && changes['block'].currentValue && this.isEditMode && !this.isInitialized) {
      this.initForm();
    }
  }

  private listenFormChanges() {
    this.questionBlockForm.valueChanges.subscribe(value => {
      this.sendUpdateRqValue(value);
    })
  }

  private sendUpdateRqValue(value: any) {
    const questionBlockRQ: CreateQuestionBlockRQ = value as CreateQuestionBlockRQ;
    const rq: CreateQuestionBlockRQ = {
      description: this.blockRQ?.description!,
      name: this.blockRQ?.name!,
      questions: questionBlockRQ.questions,
      type: this.blockRQ?.type!,
    };
    this.onBlockUpdate.emit({
      formGroup: this.questionBlockForm,
      blockRQ: rq
    });
  }

  get questionsArray(): FormArray {
    return controlArray(this.questionBlockForm, 'questions');
  }

  createQuestion(question?: CreateQuestionRQ): FormGroup {
    return new FormGroup({
      question: new FormControl(question?.question || '', Validators.compose([
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(200),
        specialCharacterValidator
      ])),
      answerOptions: new FormArray([])
    });
  }

  addQuestion() {
    this.questionsArray.push(this.createQuestion());
  }

  removeQuestion(index: number) {
    this.questionsArray.removeAt(index);
    this.sendUpdateRqValue(this.questionBlockForm.value);
  }

  questionOptionsArray(index: number): FormArray {
    const formGroup = this.questionsArray.controls[index] as FormGroup;
    return controlArray(formGroup, 'answerOptions');
  }

  addAnswerOption(index: number, value?: string) {
    const options = this.questionOptionsArray(index);
    options.push(new FormControl(value || '', Validators.compose([
      Validators.required,
      Validators.minLength(2),
      Validators.maxLength(200),
      specialCharacterValidator
    ])));
  }

  removeQuestionOption(index: number, removeAt: number) {
    this.questionOptionsArray(index).removeAt(removeAt);
    this.sendUpdateRqValue(this.questionBlockForm.value);
  }

  initForm() {
    if (this.isInitialized) {
      return;
    }
    control(this.questionBlockForm, 'name').setValue(this.blockRQ?.name || '');
    control(this.questionBlockForm, 'description').setValue(this.blockRQ?.description || '');
    control(this.questionBlockForm, 'answerTimeLimit').setValue(this.blockRQ?.answerTimeLimit || '');
    if (this.blockRQ?.questions) {
      this.blockRQ?.questions.forEach((value, index) => {
        this.questionsArray.push(this.createQuestion(value));
        value.answerOptions?.forEach(option => this.addAnswerOption(index, option));
      });
    }
    this.isInitialized = true;
  }


  protected readonly control = control;
  protected readonly asControl = asControl;
}
