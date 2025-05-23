import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from "@angular/router";
import { ActivityTemplateDto } from "../../../events/model/activity.model";
import { ActivityTemplateService } from "../../../events/services/activity-template.service";

@Component({
  selector: 'app-create-activity-template',
  templateUrl: './create-activity-template.component.html',
  styleUrl: './create-activity-template.component.css'
})
export class CreateActivityTemplateComponent implements OnInit {
  activityId?: string;
  isEditMode: boolean = false;
  activityTemplateDto?: ActivityTemplateDto;

  constructor(private activateRoute: ActivatedRoute,
              private router: Router,
              private activityTemplateService: ActivityTemplateService) {
  }

  ngOnInit(): void {
    this.isEditMode = this.router.url.includes("edit");
    this.activateRoute.params.subscribe(params => {
      this.activityId = params['activityId'];
      if (this.activityId) {
        this.loadTemplate();
      }
    });
  }

  loadTemplate() {
    this.activityTemplateService.findActivityTemplateById(this.activityId!).subscribe({
      next: (data) => {
        this.activityTemplateDto = data;
      },
      error: () => {
        this.router.navigate(['/app/activity/my-templates'])
      }
    })
  }


}
