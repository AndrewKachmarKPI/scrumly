import {
  AfterViewChecked,
  AfterViewInit,
  Component, ElementRef, EventEmitter,
  HostListener,
  Input, OnChanges,
  OnDestroy,
  OnInit, Output, SimpleChanges,
  ViewChild
} from '@angular/core';
import { ParticipantAbstractModel, StreamModel } from 'openvidu-angular';
import { ConferenceLayoutService } from '../../service/conference-layout.service';
import { Dish } from '../../model/layout.model';
import { ConferenceConfigDto } from '../../model/conference.model';
import { filter, Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'prime-ov-video-layout',
  templateUrl: './prime-ov-video-layout.component.html',
  styleUrl: './prime-ov-video-layout.component.css'
})
export class PrimeOvVideoLayoutComponent implements OnDestroy, AfterViewInit, AfterViewChecked, OnInit, OnChanges {
  @Input() conferenceId?: string;
  @Input() activityId?: string;
  @Input() workspaceId?: string;
  @Input() conferenceConfig?: ConferenceConfigDto;
  @Input() localParticipant!: ParticipantAbstractModel;
  @Input() remoteParticipants!: ParticipantAbstractModel[];

  @Output() onExitWorkspace: EventEmitter<boolean> = new EventEmitter<boolean>();

  @ViewChild('scenary', { static: false }) scenary!: ElementRef;
  @ViewChild('conference', { static: false }) conference!: ElementRef;
  @ViewChild('dish', { static: false }) dish!: ElementRef;
  @ViewChild('screen', { static: false }) screen!: ElementRef;

  selectedStream?: StreamModel;
  destroy$: Subject<void> = new Subject();

  constructor(public layoutService: ConferenceLayoutService) {
  }


  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['remoteParticipants'] && changes['remoteParticipants'].currentValue ||
      changes['localParticipant'] && changes['localParticipant'].currentValue) {
      this.layoutService.resize();
    }
    if (changes['conferenceConfig'] && changes['conferenceConfig'].currentValue) {
      this.onConferenceConfigChange();
    }
  }


  ngAfterViewInit(): void {
    if (this.scenary) {
      this.layoutService.init({
        scenary: this.scenary,
        conference: this.conference,
        dish: this.dish,
        screen: this.screen
      });
    }
  }

  ngAfterViewChecked(): void {
    this.layoutService?.resize();
  }


  onPinStream(stream: StreamModel) {
    if (this.selectedStream) {
      this.onUnPinStream();
    }
    this.selectedStream = stream;
    this.layoutService.changeLayout('screen');
    this.layoutService.toggleCamera(stream.connectionId!);
  }

  onUnPinStream() {
    if (!this.selectedStream) {
      return;
    }
    this.layoutService.changeLayout('conference');
    this.layoutService.toggleCamera(this.selectedStream?.connectionId!);
    this.selectedStream = undefined;
  }

  onConferenceConfigChange() {
  }

  ngOnDestroy(): void {
  }
}
