import { Component, Input, SimpleChanges } from '@angular/core';
import {
  ActivityBlockReport,
  ActivityItemBoardBlockReport
} from '../../../model/activity-room-report.model';

@Component({
  selector: 'item-board-block-report',
  templateUrl: './item-board-block-report.component.html',
  styleUrl: './item-board-block-report.component.css'
})
export class ItemBoardBlockReportComponent {
  @Input() isGeneratingReport: boolean = false;

  @Input('block') set itemBoardBlock(block: ActivityBlockReport) {
    this.block = block as ActivityItemBoardBlockReport;
  }

  block!: ActivityItemBoardBlockReport;
  data: any[] = [];

  constructor() {
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['itemBoardBlock'] && changes['itemBoardBlock'].currentValue) {
      this.createReportData();
    }
  }


  private createReportData() {
    const flatData: any[] = [];
    this.block.boardColumnIssues.forEach(card => {
      const flatCard = {
        ...card.columnMetadata
      };
      card.userColumnIssues.forEach(userCard => {
        const flatUserCard = {
          ...userCard,
          ...flatCard
        };
        flatData.push(flatUserCard);
      });
    });
    this.data = this.mergeIssues(flatData);
  }

  mergeIssues(flatData: any[]) {
    const mergedMap = new Map<string, any>();
    for (const issue of flatData) {
      const key = `${ issue.columnId }-${ issue.issueMetadata.issueKey }`;
      if (!mergedMap.has(key)) {
        mergedMap.set(key, {
          ...issue,
          users: [ issue.userMetadata ],
        });
      } else {
        const existing = mergedMap.get(key)!;
        if (!existing.users.some((user: { userId: any; }) => user.userId === issue.userMetadata.userId)) {
          existing.users.push(issue.userMetadata);
        }
      }
    }
    return Array.from(mergedMap.values());
  }

  calculateTotalReflect(id: number) {
    let total = 0;
    if (this.block) {
      total = this.block.boardColumnIssues.find(card => card.columnMetadata.id === id)
        ?.userColumnIssues.length || 0;
    }
    return total;
  }

}
