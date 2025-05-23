import { Component, OnInit } from '@angular/core';
import { Message } from 'primeng/api';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-conference-disconnected',
  templateUrl: './conference-disconnected.component.html',
  styleUrl: './conference-disconnected.component.css'
})
export class ConferenceDisconnectedComponent implements OnInit {
  messages?: Message[];
  conferenceId?: string;
  activityId?: string;
  workspaceId?: string;

  constructor(private activateRoute: ActivatedRoute,
              private router: Router) {
  }

  ngOnInit(): void {
    this.activateRoute.params.subscribe(params => {
      this.conferenceId = params['conferenceId'];
    });
    this.activateRoute.queryParams.subscribe(params => {
      this.activityId = params['activityId'];
      this.workspaceId = params['workspaceId'];
    });
  }

  returnHome() {
    this.router.navigate([ '/app/events/meetings' ]);
  }

  reconnect() {
    this.router.navigate([ `/app/workspace/conference/${ this.conferenceId }` ], {
      queryParams: {
        activityId: this.activityId,
        workspaceId: this.workspaceId
      }
    });
  }
}
