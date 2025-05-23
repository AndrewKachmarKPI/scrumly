import { Component, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from "primeng/dynamicdialog";
import { CreateActivityBlockConfigRQ } from "../../../../../events/model/activity.model";

@Component({
  selector: 'app-order-agenda-blocks-dialog',
  templateUrl: './order-agenda-blocks-dialog.component.html',
  styleUrl: './order-agenda-blocks-dialog.component.css'
})
export class OrderAgendaBlocksDialogComponent implements OnInit {

  blocks: CreateActivityBlockConfigRQ[] = [];

  constructor(public ref: DynamicDialogRef,
              public config: DynamicDialogConfig) {
    this.blocks = this.config.data.blocks;
  }

  ngOnInit(): void {
  }


  onReorder() {
    this.blocks = this.blocks.map((value, index) => {
      return {
        ...value,
        order: index + 1
      }
    });
  }
}
