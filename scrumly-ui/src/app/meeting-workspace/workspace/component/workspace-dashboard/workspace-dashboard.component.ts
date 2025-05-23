import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { Message } from 'primeng/api';
import { filter, forkJoin, lastValueFrom, Subject, takeUntil } from 'rxjs';
import { ActivityRoomService } from '../../services/activity-room.service';
import { ActivityService } from '../../../../member-dashboard/modules/events/services/activity.service';
import { AuthService } from '../../../../auth/auth.service';
import { ActivityRoom, ActivityTimerState } from '../../model/activity-room.model';
import { ActivityRoomReactiveService, UserConnectionStatus } from '../../services/activity-room-reactive.service';
import { Router } from '@angular/router';
import { ActivityRoomActionsWsService } from '../../services/activity-room-actions-ws.service';
import { ActivityRoomReport } from '../../model/activity-room-report.model';
import { ActivityRoomReportService } from '../../services/activity-room-report.service';

@Component({
  selector: 'workspace-dashboard',
  templateUrl: './workspace-dashboard.component.html',
  styleUrl: './workspace-dashboard.component.css',
  providers: [ ActivityRoomReactiveService ]
})
export class WorkspaceDashboardComponent implements OnInit, OnChanges, OnDestroy {
  @Input() workspaceId?: string;
  @Input() activityId?: string;
  @Input() redirectOnDestroy?: boolean = true;
  @Output() onExitWorkspace: EventEmitter<boolean> = new EventEmitter<boolean>();

  activityRoom?: ActivityRoom;
  activityRoomReport?: ActivityRoomReport;

  activityTimerState?: ActivityTimerState;
  isFacilitator: boolean = false;
  isRoomCreated: boolean = false;
  isRoomActive: boolean = false;
  isLostConnection: boolean = false;

  showLoadSpinner: boolean = true;
  sideBarState: boolean = true;
  selectedBlockId?: string;
  selectedNestedBlockId?: string;

  zoomLevel: number = 1;
  destroy$: Subject<void> = new Subject();
  messages: Message[] = [];

  showFacilitatorActions: boolean = true;
  viewMode: 'workspace' | 'report' = 'workspace';

  meetingNotesState: boolean = false;


  constructor(private activityRoomService: ActivityRoomService,
              private activityRoomReportService: ActivityRoomReportService,
              private activityRoomReactiveService: ActivityRoomReactiveService,
              private activityRoomActionsWsService: ActivityRoomActionsWsService,
              private activityService: ActivityService,
              private authService: AuthService,
              private router: Router) {
  }

  async ngOnInit() {
    if (!this.activityId) {
      throw new Error('Activity id is required');
    }
    this.showLoadSpinner = true;
    this.initializeActivityRoom();
  }

  ngOnChanges(changes: SimpleChanges): void {
  }

  ngOnDestroy(): void {
    this.exitRoom();
    this.destroy$.next();
    this.destroy$.complete();
  }


  async initializeActivityRoom() {
    const user = await lastValueFrom(this.authService.loadUserProfile().pipe(filter(Boolean)));
    const activity = await lastValueFrom(this.activityService.getActivityById(this.activityId!));
    const room = await lastValueFrom(this.activityRoomService.getActivityRoom(this.activityId!));

    this.isRoomCreated = !!room;
    this.isRoomActive = this.isRoomCreated && room.statusMetadata.isActive;
    this.isFacilitator = user.userId === activity.createdBy;


    if (!this.isRoomCreated || this.isRoomActive) {
      this.changeViewMode('workspace');

      this.activityRoomReactiveService.initService(this.activityId!, user.userId);
      this.listenActivityRoomChanges();

      if (this.isRoomCreated) {
        this.joinRoom();
      } else if (this.isFacilitator) {
        this.createActivityRoom();
      } else {
        this.joinActivityRoomWhenCreated()
      }
    } else {
      this.activityRoomReport = await lastValueFrom(this.activityRoomReportService.getActivityRoomReport(this.activityId!));
      this.activityRoom = room;
      this.showLoadSpinner = false;
      this.changeViewMode('report');
    }
  }

  joinRoom() {
    this.activityRoomService.joinRoom(this.activityId!)
      .pipe(takeUntil(this.destroy$), filter(Boolean))
      .subscribe({
        next: () => {
          this.isRoomCreated = true;
          this.showLoadSpinner = false;
        },
        error: () => {

        }
      })
  }

  exitRoom() {
    if (!this.activityRoom?.statusMetadata.isActive) {
      if (this.redirectOnDestroy) {
        this.router.navigate([ '/app/home' ]);
      }
      return;
    }
    this.activityRoomService.exitRoom(this.activityId!)
      .subscribe({
        next: () => {
          if (this.redirectOnDestroy) {
            this.router.navigate([ '/app/home' ]);
          }
          this.onExitWorkspace.emit(true);
        },
        error: () => {

        }
      })
  }

  changeViewMode(mode: 'workspace' | 'report') {
    this.viewMode = mode;
  }

  joinActivityRoomWhenCreated() {
    this.activityRoomReactiveService.onActivityRoomCreated
      .pipe(takeUntil(this.destroy$), filter(Boolean))
      .subscribe({
        next: () => {
          this.joinRoom();
        }
      });
  }

  createActivityRoom() {
    this.activityRoomService.createActivityRoom(this.activityId!)
      .pipe(filter(Boolean), takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.joinRoom();
        }
      });
  }

  listenActivityRoomChanges() {
    this.activityRoomReactiveService.onActivityRoomChanged
      .pipe(filter(Boolean), takeUntil(this.destroy$))
      .subscribe({
        next: (activityRoom) => {
          this.showLoadSpinner = false;
          this.activityRoom = activityRoom;
          this.selectedBlockId = activityRoom.blockNavigationMetadata.userNavigations
            .find(block => this.authService.isCurrentUser(block.userId))
            ?.activeBlockId || activityRoom.blockNavigationMetadata.activeBlockId;
        }
      });
    this.activityRoomReactiveService.onActivityTimerChanged
      .pipe(filter(Boolean), takeUntil(this.destroy$))
      .subscribe({
        next: (timer) => {
          this.activityTimerState = timer;
        }
      });
    this.activityRoomReactiveService.onUserConnectionChanged
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (status) => {
          if (status === UserConnectionStatus.KICKED) {
            this.router.navigate([ `/app/workspace/dashboard/kicked` ])
          }
        }
      });

    this.activityRoomReactiveService.activityRoomLostConnection
      .pipe(filter(Boolean), takeUntil(this.destroy$))
      .subscribe({
        next: (isLostConnection) => {
          this.isLostConnection = isLostConnection;
          this.showLoadSpinner = isLostConnection;
        }
      });

    this.activityRoomReactiveService.onActivityRoomClosed
      .pipe(takeUntil(this.destroy$), filter(Boolean))
      .subscribe({
        next: (event) => {
          this.activityRoom = JSON.parse(event?.activityRoom);
          this.activityRoomReport = JSON.parse(event?.activityRoomReport);
          this.changeViewMode('report');
        }
      });

  }

  onActivityTimerEnd() {
    this.activityTimerState = {
      state: false
    };
    this.activityRoomActionsWsService.onChangeTimerState(this.activityId!, this.activityTimerState);
  }

  toggleFacilitatorActions() {
    this.showFacilitatorActions = !this.showFacilitatorActions;
  }

  onKickUser(userId: string) {
    this.activityRoomActionsWsService.onKickUser(this.activityId!, userId);
  }

  onViewWorkspace() {
    this.viewMode = 'workspace';
  }
}
