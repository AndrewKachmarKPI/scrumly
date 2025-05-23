import { Component, EventEmitter, Input, OnDestroy, OnInit, Output, ViewChild } from '@angular/core';
import {
  BroadcastingService,
  BroadcastingStatus, ParticipantAbstractModel,
  ParticipantService,
  RecordingService,
  RecordingStatus, TokenModel, VideoconferenceComponent
} from 'openvidu-angular';
import { Router } from '@angular/router';
import { catchError, filter, from, map, Subject, Subscription, takeUntil, tap } from 'rxjs';
import { ConferenceConfigDto, ConferenceRoomDto } from '../../model/conference.model';
import { ConferenceService } from '../../../services/conference.service';
import { ConnectionEvent, Session } from 'openvidu-browser';
import { parseConnectionProfileDtoData } from '../../../../ui-components/services/utils';
import { DeviceSetup } from '../pre-join-video-configure/pre-join-video-configure.component';
import { MenuItem, Message } from 'primeng/api';
import { WsEvent } from '../../../websocket/websocket.interfaces';
import { wsEndpoint } from '../../../websocket/websocket.service';
import { environment } from '../../../../../enviroments/enviroment';
import { ConferenceLayoutService } from '../../service/conference-layout.service';
import { WebsocketConferenceRoomService } from '../../../websocket/websocket-conference-room.service';

@Component({
  selector: 'prime-ov-video-conference',
  templateUrl: './prime-ov-video-conference.component.html',
  styleUrl: './prime-ov-video-conference.component.scss'
})
export class PrimeOvVideoConferenceComponent implements OnInit, OnDestroy {
  @Input() conferenceId?: string;
  @Input() activityId?: string;
  @Input() workspaceId?: string;
  @Input() deviceSetup?: DeviceSetup;

  @Output() onSessionCreatedEmit = new EventEmitter();
  @Output() onSessionRemovedEmit = new EventEmitter();

  @ViewChild('videoconferenceComponent') videoconferenceComponent?: VideoconferenceComponent;

  conferenceRoom?: ConferenceRoomDto;
  tokens?: TokenModel;

  localParticipant!: ParticipantAbstractModel;
  remoteParticipants!: ParticipantAbstractModel[];
  destroy$: Subject<void> = new Subject<void>();

  messages: Message[] = [];
  loading: boolean = true;

  session?: Session;
  isDisconnected?: boolean = false;

  private wsConnectionSubscription?: Subscription;
  private conferenceSubscription?: Subscription;
  public conferenceConfig?: ConferenceConfigDto;

  menuItems: MenuItem[] = [];

  constructor(private conferenceService: ConferenceService,
              private participantService: ParticipantService,
              private recordingService: RecordingService,
              private broadcastingService: BroadcastingService,
              private wsService: WebsocketConferenceRoomService,
              private layoutService: ConferenceLayoutService,
              private router: Router) {
  }

  ngOnInit() {
    this.conferenceConfig = {
      showWorkspace: false,
      conferenceId: this.conferenceId,
      workspaceId: this.workspaceId,
      pinConnectionId: undefined
    }
    this.initializeConference()
      .pipe(
        catchError((error: any) => {
          this.messages = [ {
            severity: 'error',
            detail: error?.error?.message || error?.statusText
          } ]
          return [];
        })
      )
      .subscribe(() => {
        this.loading = false;
      });
    this.initializeConferenceConfig();
    this.setMenuItems();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
    if (!this.isDisconnected) {
      this.closeConnection();
    }

    this.wsService.disconnect();
    if (this.conferenceSubscription) {
      this.wsService.unsubscribe(WsEvent.CONFERENCE_CONFIG);
      this.conferenceSubscription.unsubscribe();
    }
    if (this.wsConnectionSubscription) {
      this.wsConnectionSubscription.unsubscribe();
    }

    this.closeWorkspace();
  }

  onNodeCrashed() {
    this.initializeConference().subscribe();
  }

  initializeConference() {
    return from(this.conferenceService.joinConference(this.conferenceId!))
      .pipe(
        catchError((error: any) => {
          this.messages = [ {
            severity: 'error',
            detail: error?.error?.message || error?.statusText
          } ]
          return []; // Return empty or handle errors as needed
        })
      )
      .pipe(
        tap(conferenceRoom => {
          this.conferenceRoom = conferenceRoom;
          this.tokens = {
            webcam: this.conferenceRoom.cameraToken,
            screen: this.conferenceRoom.screenToken
          };

          if (this.conferenceRoom.isRecordingActive) {
            this.recordingService.updateStatus(RecordingStatus.STARTED);
          }
          if (this.conferenceRoom.isBroadcastingActive) {
            this.broadcastingService.updateStatus(BroadcastingStatus.STARTED);
          }
        })
      );
  }

  onSessionCreated(session: Session) {
    this.session = session;
    session.on('connectionCreated', (data) => {
      this.onConnectionCreatedCallback(session, data);
    });
    session.on('sessionDisconnected', () => {
      this.onSessionDisconnectedCallback();
    });
    this.subscribeToParticipants();
  }

  onSessionDisconnectedCallback() {
    this.isDisconnected = true;
    this.onSessionRemovedEmit.emit();
  }

  onConnectionCreatedCallback(session: Session, connectionEvent: ConnectionEvent) {
    if (session.connection.data) {
      this.onSessionCreatedEmit.emit(session.connection.data);
      const profileDto = parseConnectionProfileDtoData(session.connection.data)!;
      if (profileDto && profileDto.firstName) {
        const nickname = profileDto.firstName + ' ' + profileDto.lastName || 'Member';
        this.participantService.setMyNickname(nickname);
        this.participantService.setRemoteNickname(connectionEvent.connection.connectionId, nickname);
        this.participantService.updateLocalParticipant();
        this.participantService.updateRemoteParticipants();
      }
    }
  }

  onLeaveButtonClicked() {
    this.closeConnection();
  }

  closeConnection() {
    this.conferenceService.exitConference(this.conferenceId!).subscribe({
      next: () => {
      }
    });
  }

  subscribeToParticipants() {
    this.participantService.localParticipantObs
      .pipe(takeUntil(this.destroy$))
      .subscribe((p) => {
        this.localParticipant = p;
      });
    this.participantService.remoteParticipantsObs
      .pipe(takeUntil(this.destroy$))
      .subscribe((participants) => {
        this.remoteParticipants = participants;
      });
  }

  initializeConferenceConfig() {
    this.wsService.connect({
      url: environment.WS.conference_service
    });

    this.wsConnectionSubscription = this.wsService.status
      .pipe(filter(Boolean))
      .subscribe(() => {
        this.conferenceSubscription = this.wsService
          .on(wsEndpoint(WsEvent.CONFERENCE_CONFIG, { conferenceId: this.conferenceId! }))
          .pipe(filter(Boolean), map(value => value as ConferenceConfigDto))
          .subscribe({
            next: (data) => {
              this.conferenceConfig = data;
            }
          });
      });
  }

  setMenuItems() {
    const items: MenuItem[] = [];
    if (this.conferenceConfig?.showWorkspace) {
      items.push({
        label: 'Close workspace',
        icon: 'pi pi-times',
        command: () => this.closeWorkspace()
      });
    } else {
      items.push({
        label: 'Open workspace',
        icon: 'pi pi-objects-column',
        command: () => this.openWorkspace()
      });
    }
    this.menuItems = items;
  }

  openWorkspace() {
    if (!this.conferenceConfig) {
      return;
    }
    this.conferenceConfig.showWorkspace = true;
    this.layoutService.changeLayout('workspace');
    this.setMenuItems();
  }

  closeWorkspace() {
    if (!this.conferenceConfig) {
      return;
    }
    this.conferenceConfig.showWorkspace = false;
    this.layoutService.changeLayout('conference');
    this.setMenuItems();
  }
}
