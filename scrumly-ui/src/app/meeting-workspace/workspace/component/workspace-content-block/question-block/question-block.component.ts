import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import {
  ActivityBlock,
  ActivityQuestionBlock, ActivityRoomStatus, ActivityRoomStatusMetadata,
  UserMetadata,
  UserQuestionAnswer
} from '../../../model/activity-room.model';
import {
  control,
  controlArray,
  specialCharacterValidator,
  trackById
} from '../../../../../ui-components/services/utils';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { ActivityRoomActionsWsService } from '../../../services/activity-room-actions-ws.service';

@Component({
  selector: 'question-block',
  templateUrl: './question-block.component.html',
  styleUrl: './question-block.component.css'
})
export class QuestionBlockComponent implements OnInit, OnChanges {
  @Input('block') set questionBlock(block: ActivityBlock) {
    this.block = block as ActivityQuestionBlock;
  }

  block!: ActivityQuestionBlock;

  @Input() activityId?: string;
  @Input() userMetadata?: UserMetadata;
  @Input() activityRoomStatus?: ActivityRoomStatusMetadata;

  questionFormGroup?: FormGroup;
  answerFormGroup?: FormGroup;

  isEditing: boolean = false;
  hasAnswer: boolean = false;

  constructor(private activityRoomAction: ActivityRoomActionsWsService,
              private messageService: MessageService) {
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['questionBlock'] && this.block) {
      console.log(this.block);
      this.initializeQuestionFormGroup();
      this.initializeUserResponseForm();
    }
  }

  initializeQuestionFormGroup() {
    this.questionFormGroup = new FormGroup({
      answers: new FormArray([])
    });

    this.block.questionMetadata.forEach(question => {
      const userAnswers = this.block.userQuestionAnswers
        .find(answer => answer.userMetadata.userId === this.userMetadata?.userId)
        ?.answers || [];
      const userAnswer = userAnswers
        .find(answer => answer.questionMetadata.id === question.id);
      this.hasAnswer = !!userAnswer;
      this.questionGroupAnswerArray().push(
        new FormGroup({
          questionId: new FormControl(question.id),
          answer: new FormControl(userAnswer?.answer || '', Validators.compose([
            Validators.required, Validators.maxLength(300), specialCharacterValidator
          ]))
        })
      );
    });
  }

  questionGroupAnswerArray() {
    return controlArray(this.questionFormGroup!, 'answers');
  }

  questionGroupAnswerControl(index: number): FormControl {
    return this.questionGroupAnswerArray().at(index).get('answer') as FormControl;
  }

  initializeUserResponseForm() {
    this.answerFormGroup = new FormGroup({
      userQuestionAnswers: new FormGroup({})
    });
    console.log(this.block);

    console.log(this.block.userQuestionAnswers);
    this.block.userQuestionAnswers.forEach(userAnswer => {
      const userAnswerFormGroup = new FormGroup({
        userMetadata: new FormControl(userAnswer.userMetadata),
        answers: new FormArray(
          userAnswer.answers.map(questionAnswer =>
            new FormGroup({
              questionId: new FormControl(questionAnswer.questionMetadata.id),
              answer: new FormControl(questionAnswer.answer, Validators.required)
            })
          )
        )
      });
      this.userAnswersGroup().addControl(
        userAnswer.userMetadata.userId.toString(),
        userAnswerFormGroup
      );
    });
  }


  userAnswersGroup(): FormGroup {
    return this.answerFormGroup?.get('userQuestionAnswers') as FormGroup;
  }

  getUserFormGroup(userId: number): FormGroup {
    return this.userAnswersGroup().get(userId.toString()) as FormGroup;
  }

  getUserAnswersArray(userId: number): FormArray {
    return this.getUserFormGroup(userId).get('answers') as FormArray;
  }

  getUserAnswerControl(userId: number, questionId: number): FormControl {
    const userAnswers = this.getUserAnswersArray(userId);
    const questionIndex = this.block.questionMetadata.findIndex(q => q.id === questionId);
    return userAnswers.at(questionIndex).get('answer') as FormControl;
  }


  toggleEditing() {
    this.isEditing = !this.isEditing;
  }

  onDeleteAnswer() {
    if (!this.hasAnswer) {
      return;
    }
    this.activityRoomAction.onDeleteQuestion(this.activityId!, this.block.metadata.id)
      .subscribe({
        next: () => {
          this.isEditing = false;
          this.messageService.add({
            severity: 'success',
            summary: 'Answer removed',
            detail: 'Your answer successfully removed!',
          });
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Failed remove',
            detail: 'Your answer is not removed!',
          });
        }
      });
  }

  onSubmitAnswer() {
    if (this.questionFormGroup?.invalid) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Form error',
        detail: 'Fill the form before sending!',
      });
      return;
    }

    const values = this.questionFormGroup?.value.answers as any[];
    const userQuestionAnswer: UserQuestionAnswer = {
      answers: values.map(answerControl => {
        const questionMetadata = this.block.questionMetadata
          .find(question => question.id === answerControl.questionId);
        return {
          answer: answerControl.answer,
          questionMetadata: questionMetadata!
        }
      }),
      userMetadata: this.userMetadata!
    };

    this.activityRoomAction.onAnswerQuestion(this.activityId!, this.block.metadata.id, userQuestionAnswer)
      .subscribe({
        next: () => {
          this.isEditing = false;
          this.messageService.add({
            severity: 'success',
            summary: 'Answer saved',
            detail: 'Your answer successfully recorded!',
          });
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Failed save',
            detail: 'Your answer is not recorded!',
          });
        }
      });
  }

  protected readonly control = control;
  protected readonly controlArray = controlArray;
  protected readonly trackById = trackById;
}
