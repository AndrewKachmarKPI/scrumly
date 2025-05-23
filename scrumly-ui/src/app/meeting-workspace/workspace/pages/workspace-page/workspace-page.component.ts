import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'workspace-page',
  templateUrl: './workspace-page.component.html',
  styleUrl: './workspace-page.component.css'
})
export class WorkspacePageComponent implements OnInit {
  workspaceId?: string;
  activityId?: string;

  constructor(private activateRoute: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.activateRoute.queryParams.subscribe(params => {
      this.activityId = params['activityId'];
      this.workspaceId = params['workspaceId'];
    });
  }

}
