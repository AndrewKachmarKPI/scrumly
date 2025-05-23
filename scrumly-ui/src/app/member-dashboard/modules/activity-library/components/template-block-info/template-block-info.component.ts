import { Component, Input, OnInit } from '@angular/core';
import { getActivityBlockDto } from "../../../../../ui-components/services/activity-utils";
import { ActivityBlockConfigDto, ActivityBlockType } from "../../../events/model/activity.model";

@Component({
  selector: 'prime-template-block-info',
  templateUrl: './template-block-info.component.html',
  styleUrl: './template-block-info.component.css'
})
export class TemplateBlockInfoComponent implements OnInit {
  @Input() blockStyle: string = "";
  @Input() block?: ActivityBlockConfigDto;

  constructor() {
  }

  ngOnInit(): void {
  }


  protected readonly ActivityBlockType = ActivityBlockType;
  protected readonly getActivityBlockDto = getActivityBlockDto;
}
