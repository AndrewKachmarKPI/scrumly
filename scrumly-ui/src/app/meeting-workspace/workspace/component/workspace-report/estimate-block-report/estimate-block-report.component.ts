import { Component, Input } from '@angular/core';
import { ActivityBlockReport, ActivityEstimateBlockReport } from '../../../model/activity-room-report.model';

@Component({
  selector: 'estimate-block-report',
  templateUrl: './estimate-block-report.component.html',
  styleUrl: './estimate-block-report.component.css'
})
export class EstimateBlockReportComponent {
  @Input('block') set estimateBlock(block: ActivityBlockReport) {
    this.block = block as ActivityEstimateBlockReport;
  }

  block!: ActivityEstimateBlockReport;
}
