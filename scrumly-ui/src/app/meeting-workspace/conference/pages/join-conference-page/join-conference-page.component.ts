import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ConferenceService } from '../../../services/conference.service';
import { ActivityService } from '../../../../member-dashboard/modules/events/services/activity.service';
import { ActivityDto } from '../../../../member-dashboard/modules/events/model/activity.model';
import { catchError, filter, from, map, Subscription, tap } from 'rxjs';
import { Location } from '@angular/common';
import { Message, MessageService } from 'primeng/api';
import { environment } from '../../../../../enviroments/enviroment';
import { wsEndpoint } from '../../../websocket/websocket.service';
import { UserProfileDto } from '../../../../auth/auth.model';
import { ExitConferenceData, JoinConferenceData } from '../../../websocket/events.model';
import { AuthService } from '../../../../auth/auth.service';
import {
  DeviceSetup,
  PreJoinVideoConfigureComponent
} from '../../components/pre-join-video-configure/pre-join-video-configure.component';
import { WsEvent } from '../../../websocket/websocket.interfaces';
import { WebsocketConferenceRoomService } from '../../../websocket/websocket-conference-room.service';

@Component({
  selector: 'app-join-conference-page',
  templateUrl: './join-conference-page.component.html',
  styleUrl: './join-conference-page.component.css'
})
export class JoinConferencePageComponent implements OnInit, OnDestroy {
  conferenceId?: string;
  activityId?: string;
  workspaceId?: string;

  activity?: ActivityDto;
  messages?: Message[];

  isJoined: boolean = false;

  showConference: boolean = false;
  showJoin: boolean = true;
  showLoadSpinner: boolean = false;


  private wsConnectionSubscription?: Subscription;
  private joinSubscription?: Subscription;
  private exitSubscription?: Subscription;

  activeSessions: UserProfileDto[] = [];
  deviceSetup?: DeviceSetup;

  @ViewChild('preJoinConfigurator') preJoinConfigurator?: PreJoinVideoConfigureComponent;

  constructor(private activateRoute: ActivatedRoute,
              private activityService: ActivityService,
              private conferenceService: ConferenceService,
              private authService: AuthService,
              private location: Location,
              private wsService: WebsocketConferenceRoomService,
              private messageService: MessageService,
              private router: Router) {
  }

  ngOnInit(): void {
    this.activateRoute.params.subscribe(params => {
      this.conferenceId = params['conferenceId'];
      this.initialize();
    });

    this.activateRoute.queryParams.subscribe(params => {
      this.activityId = params['activityId'];
      this.workspaceId = params['workspaceId'];
      this.loadActivity();

      from(this.conferenceService.isJoined(this.conferenceId!))
        .pipe(
          catchError(error => {
            console.error('Error checking if joined:', error);
            return [ false ]; // Default to not joined in case of error
          }),
          tap(isJoined => {
            this.isJoined = isJoined;
            if (this.isJoined) {
              this.messages = [ {
                severity: 'warn',
                detail: 'You are already connected to this conference, your old session will be terminated!'
              } ];
            }
          })
        )
        .subscribe();
    });
  }

  private initialize() {
    this.wsService.connect({
      url: environment.WS.conference_service
    });

    this.conferenceService.getActiveSessions(this.conferenceId!)
      .pipe(filter(Boolean))
      .subscribe({
        next: (sessions) => {
          this.activeSessions = sessions;
        }
      })

    this.wsConnectionSubscription = this.wsService.status
      .pipe(filter(Boolean))
      .subscribe(() => {
        this.joinSubscription = this.wsService
          .on(wsEndpoint(WsEvent.JOIN_CONFERENCE, { conferenceId: this.conferenceId! }))
          .pipe(filter(Boolean), map(value => value as JoinConferenceData))
          .subscribe({
            next: (data) => {
              console.log(data);
              this.activeSessions.push(data.profileDto);
            }
          });
        this.exitSubscription = this.wsService
          .on(wsEndpoint(WsEvent.EXIT_CONFERENCE, { conferenceId: this.conferenceId! }))
          .pipe(filter(Boolean), map(value => value as ExitConferenceData))
          .subscribe({
            next: (data) => {
              const idx = this.activeSessions.findIndex(value => value.userId === data.profileDto.userId);
              this.activeSessions.splice(idx, 1);
            }
          });
      });
  }

  ngOnDestroy(): void {
    this.wsService.disconnect();
    if (this.joinSubscription) {
      this.wsService.unsubscribe(WsEvent.JOIN_CONFERENCE);
      this.joinSubscription.unsubscribe();
    }
    if (this.exitSubscription) {
      this.wsService.unsubscribe(WsEvent.EXIT_CONFERENCE);
      this.exitSubscription.unsubscribe();
    }
    if (this.wsConnectionSubscription) {
      this.wsConnectionSubscription.unsubscribe();
    }
  }


  onJoinClick() {
    this.showLoadSpinner = true;
    this.deviceSetup = this.preJoinConfigurator?.getDeviceSetup();
    from(this.conferenceService.isJoined(this.conferenceId!))
      .pipe(
        catchError(error => {
          this.showLoadSpinner = false;
          console.error('Error checking if joined:', error);
          return [ false ];
        })
      )
      .subscribe(isJoined => {
        this.isJoined = isJoined;
        if (this.isJoined) {
          this.onSwitchConference();
        } else {
          this.showConference = true;
        }
      });
  }

  onReturnBackClick() {
    this.location.back();
  }

  onSwitchConference() {
    this.conferenceService.exitConference(this.conferenceId!).subscribe({
      next: () => {
        this.showConference = true;
      }
    })
  }

  loadActivity() {
    this.activityService.getActivityById(this.activityId!).subscribe({
      next: (activity) => {
        this.activity = activity;
        const isInvited = this.activity.scheduledEvent.attendees
          .some(value => this.authService.isCurrentUser(value.userId));
        if (!isInvited) {
          this.location.back();
          this.messageService.add({
            severity: 'warn',
            summary: 'Conference error',
            detail: 'You are not invited to this conference!',
          });
        }
      }
    })
  }

  onSessionCreated() {
    this.showJoin = false;
    this.showLoadSpinner = false;
    this.showConference = true;
  }

  onSessionRemoved() {
    this.router.navigate([ `/app/workspace/conference/${ this.conferenceId }/disconnected` ], {
      queryParams: {
        activityId: this.activityId,
        workspaceId: this.workspaceId
      }
    });
  }

}
