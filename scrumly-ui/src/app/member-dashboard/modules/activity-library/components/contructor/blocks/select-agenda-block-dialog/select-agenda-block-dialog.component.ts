import { Component, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from "primeng/dynamicdialog";
import { HttpClient } from "@angular/common/http";
import { ActivityBlockTypeDto } from "../../../../../events/model/activity.model";


@Component({
  selector: 'select-agenda-block-dialog',
  templateUrl: './select-agenda-block-dialog.component.html',
  styleUrl: './select-agenda-block-dialog.component.css'
})
export class SelectAgendaBlockDialogComponent implements OnInit {
  blocks: ActivityBlockTypeDto[] = [];

  constructor(public ref: DynamicDialogRef,
              public config: DynamicDialogConfig,
              private http: HttpClient) {
  }

  ngOnInit(): void {
    this.loadBlocks();
  }

  loadBlocks(): void {
    this.http.get<ActivityBlockTypeDto[]>('assets/data/block-types.json').subscribe(data => {
      this.blocks = data;
    });
  }

  onSelectBlockType(block: ActivityBlockTypeDto) {
    this.ref.close(block);
  }

}
