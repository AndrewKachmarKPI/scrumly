import { Component, Input } from '@angular/core';
import {
  ActivityBlockReport,
  ActivityQuestionBlockReport
} from '../../../model/activity-room-report.model';

@Component({
  selector: 'question-block-report',
  templateUrl: './question-block-report.component.html',
  styleUrl: './question-block-report.component.css'
})
export class QuestionBlockReportComponent {
  @Input('block') set questionBlock(block: ActivityBlockReport) {
    this.block = block as ActivityQuestionBlockReport;
  }

  block!: ActivityQuestionBlockReport;


}
