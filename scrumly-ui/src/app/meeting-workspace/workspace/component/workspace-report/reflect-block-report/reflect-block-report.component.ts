import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import {
  ActivityBlockReport,
  ActivityEstimateBlockReport,
  ActivityReflectBlockReport
} from '../../../model/activity-room-report.model';
import { ActivityReflectBlock, ReflectColumnMetadata } from '../../../model/activity-room.model';

@Component({
  selector: 'reflect-block-report',
  templateUrl: './reflect-block-report.component.html',
  styleUrl: './reflect-block-report.component.css'
})
export class ReflectBlockReportComponent implements OnInit, OnChanges {
  @Input('block') set estimateBlock(block: ActivityBlockReport) {
    this.block = block as ActivityReflectBlockReport;
  }

  block!: ActivityReflectBlockReport;
  data: any[] = [];

  constructor() {
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['estimateBlock'] && changes['estimateBlock'].currentValue) {
      this.createReportData();
    }
  }


  private createReportData() {
    const flatData: any[] = [];
    this.block.columnCards.forEach(card => {
      const flatCard = {
        ...card.columnMetadata
      };
      card.userColumnReflectCards.forEach(userCard => {
        const flatUserCard = {
          ...userCard,
          ...flatCard
        };
        flatData.push(flatUserCard);
      });
    });
    this.data = flatData;
  }

  calculateTotalReflect(id: number) {
    let total = 0;
    if (this.block) {
      total = this.block.columnCards.find(card => card.columnMetadata.id === id)
        ?.userColumnReflectCards.length || 0;
    }
    return total;
  }

}
