import { Injectable, OnDestroy } from '@angular/core';
import { wsEndpoint } from '../../websocket/websocket.service';
import { environment } from '../../../../enviroments/enviroment';
import {
  BehaviorSubject,
  catchError,
  filter,
  interval,
  map,
  retry,
  Subject,
  Subscription,
  takeUntil, throwError,
  timeout
} from 'rxjs';
import { ActivityRoom, ActivityTimerState } from '../model/activity-room.model';
import { switchMap } from 'rxjs/operators';
import { ActivityRoomService } from './activity-room.service';
import { WebsocketActivityRoomService } from '../../websocket/websocket-activity-room.service';
import { ActivityRoomReport } from '../model/activity-room-report.model';
import { OnActivityRoomClosed } from '../model/events.model';

export enum ActivityWsEvents {
  ON_ROOM_CREATED = '/topic/activity/{activityId}/room/created',
  ON_ROOM_CLOSED = '/topic/activity/{activityId}/room/closed',
  ON_ROOM_CHANGE = '/user/{userId}/activity/{activityId}/room/change',
  ON_TIMER_CHANGE = '/user/{userId}/activity/{activityId}/room/timer',
  ON_USER_CHANGE = '/user/{userId}/activity/{activityId}/room/user'
}

export enum UserConnectionStatus {
  CONNECTED="CONNECTED",
  DISCONNECTED="DISCONNECTED",
  KICKED="KICKED"
}

@Injectable()
export class ActivityRoomReactiveService implements OnDestroy {
  private activityRoomCreatedSubject: BehaviorSubject<ActivityRoom | undefined> = new BehaviorSubject<ActivityRoom | undefined>(undefined);
  onActivityRoomCreated = this.activityRoomCreatedSubject.asObservable();

  private activityRoomClosedSubject: BehaviorSubject<OnActivityRoomClosed | undefined> = new BehaviorSubject<OnActivityRoomClosed | undefined>(undefined);
  onActivityRoomClosed = this.activityRoomClosedSubject.asObservable();

  private activityRoomChangedSubject: BehaviorSubject<ActivityRoom | undefined> = new BehaviorSubject<ActivityRoom | undefined>(undefined);
  onActivityRoomChanged = this.activityRoomChangedSubject.asObservable();

  private activityRoomLostConnectionSubject: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  activityRoomLostConnection = this.activityRoomLostConnectionSubject.asObservable();

  private activityTimerChangedSubject: BehaviorSubject<ActivityTimerState | undefined> = new BehaviorSubject<ActivityTimerState | undefined>(undefined);
  onActivityTimerChanged = this.activityTimerChangedSubject.asObservable();

  private userConnectionChangedSubject: BehaviorSubject<UserConnectionStatus | undefined> = new BehaviorSubject<UserConnectionStatus | undefined>(undefined);
  onUserConnectionChanged = this.userConnectionChangedSubject.asObservable();

  private destroy$: Subject<void> = new Subject();
  private destroyWs$: Subject<void> = new Subject();

  private activityId?: string;
  private userId?: string;

  private readonly HEART_BEAT_INTERVAL = 10000;
  private readonly HEART_BEAT_TIMEOUT = 5000;
  private readonly MAX_RETRIES = 3;
  private heartbeatSubscription: Subscription | null = null;

  constructor(private wsService: WebsocketActivityRoomService,
              private activityRoomService: ActivityRoomService) {

  }

  initService(activityId: string, userId: string) {
    if (!activityId || !userId) {
      throw new Error('Could not init activity reactive service')
    }
    this.activityId = activityId;
    this.userId = userId;
    this.wsService.connect({
      url: environment.WS.room_service,
      reconnectAttempts: 5,
      reconnectInterval: 1000
    });
    this.wsService.status
      .pipe(filter(Boolean), takeUntil(this.destroy$))
      .subscribe((status) => {
        if (status) {
          this.destroyWs$ = new Subject();
          this.initializeHandlers();
          this.activityRoomLostConnectionSubject.next(false);
        } else {
          this.destroyWs$.next();
          this.destroyWs$.complete();
          this.activityRoomLostConnectionSubject.next(true);
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();

    this.destroyWs$.next();
    this.destroyWs$.complete();

    this.wsService.disconnect();
    this.destroyHeartbeat();
  }

  initializeHandlers() {
    this.wsService
      .on(wsEndpoint(ActivityWsEvents.ON_ROOM_CREATED, { activityId: this.activityId! }))
      .pipe(takeUntil(this.destroyWs$), filter(Boolean), map(value => value as ActivityRoom))
      .subscribe({
        next: (room) => {
          console.log('ON ROOM CREATED:', room);
          this.activityRoomCreatedSubject.next(room);
        }
      });
    this.wsService
      .on(wsEndpoint(ActivityWsEvents.ON_ROOM_CLOSED, { activityId: this.activityId! }))
      .pipe(takeUntil(this.destroyWs$), filter(Boolean), map(value => value as OnActivityRoomClosed))
      .subscribe({
        next: (room) => {
          console.log('ON ROOM CLOSED:', room);
          this.activityRoomClosedSubject.next(room);
        }
      });
    this.wsService
      .on(wsEndpoint(ActivityWsEvents.ON_ROOM_CHANGE, {
        userId: this.userId!,
        activityId: this.activityId!
      }))
      .pipe(takeUntil(this.destroyWs$), filter(Boolean), map(value => value as ActivityRoom))
      .subscribe({
        next: (room) => {
          console.log('ON ROOM CHANGE:', room);
          this.activityRoomChangedSubject.next(room);
        }
      });
    this.wsService
      .on(wsEndpoint(ActivityWsEvents.ON_TIMER_CHANGE, {
        userId: this.userId!,
        activityId: this.activityId!
      }))
      .pipe(takeUntil(this.destroyWs$), filter(Boolean), map(value => value as ActivityTimerState))
      .subscribe({
        next: (value) => {
          console.log('ON TIMER CHANGE:', value);
          this.activityTimerChangedSubject.next(value);
        }
      });
    this.wsService
      .on(wsEndpoint(ActivityWsEvents.ON_USER_CHANGE, {
        userId: this.userId!,
        activityId: this.activityId!
      }))
      .pipe(takeUntil(this.destroyWs$), map(value => value as UserConnectionStatus))
      .subscribe({
        next: (value) => {
          console.log('ON USER CHANGE:', value);
          this.userConnectionChangedSubject.next(value);
        }
      });
    // this.initializeHeartbeat();
  }

  initializeHeartbeat() {
    if (this.heartbeatSubscription) {
      console.warn('Heartbeat is already running.');
      return;
    }
    this.heartbeatSubscription = interval(this.HEART_BEAT_INTERVAL)
      .pipe(switchMap(() =>
        this.activityRoomService.getActivityRoom(this.activityId!)
          .pipe(timeout(this.HEART_BEAT_TIMEOUT), retry(this.MAX_RETRIES), filter(Boolean), catchError((error) => {
            console.error('Heartbeat failed:', error);
            return throwError(() => new Error('Heartbeat request failed.'));
          }))))
      .subscribe({
        next: (room) => {
          this.activityRoomChangedSubject.next(room);
        },
        error: (error) => {
          console.error('Final Heartbeat error:', error)
        },
      });
  }

  destroyHeartbeat() {
    if (this.heartbeatSubscription) {
      this.heartbeatSubscription.unsubscribe();
      this.heartbeatSubscription = null;
      console.log('Heartbeat stopped.');
    }
  }
}
