import { Component, Input } from '@angular/core';
import { ActivityTemplateDto } from "../../../events/model/activity.model";
import { control } from "../../../../../ui-components/services/utils";

@Component({
  selector: 'template-details-card',
  templateUrl: './template-details-card.component.html',
  styleUrl: './template-details-card.component.css'
})
export class TemplateDetailsCardComponent {
  @Input() activityTemplateDto?: ActivityTemplateDto;
  protected readonly control = control;
}
