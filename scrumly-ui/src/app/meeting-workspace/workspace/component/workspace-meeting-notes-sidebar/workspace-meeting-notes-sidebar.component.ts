import { Component, EventEmitter, Input, OnChanges, OnDestroy, Output, SimpleChanges } from '@angular/core';
import { control } from '../../../../ui-components/services/utils';
import { FormControl } from '@angular/forms';
import { ActivityRoomActionsWsService } from '../../services/activity-room-actions-ws.service';
import { Subject } from 'rxjs';
import { ActivityRoom } from '../../model/activity-room.model';
import { ActivityRoomActionsService } from '../../services/activity-room-actions.service';
import { NgxSpinnerService } from 'ngx-spinner';

@Component({
  selector: 'workspace-meeting-notes-sidebar',
  templateUrl: './workspace-meeting-notes-sidebar.component.html',
  styleUrl: './workspace-meeting-notes-sidebar.component.css',
})
export class WorkspaceMeetingNotesSidebarComponent implements OnChanges, OnDestroy {
  @Input() activityId?: string;
  @Input() activityRoom?: ActivityRoom;

  @Input() meetingNotesState: boolean = false;
  @Output() meetingNotesStateChange: EventEmitter<boolean> = new EventEmitter<boolean>();

  meetingNotes?: string;
  meetingNotesControl?: FormControl;

  private destroy$: Subject<void> = new Subject<void>();

  constructor(private roomActionService: ActivityRoomActionsWsService,
              private activityRoomService: ActivityRoomActionsService,
              private ngxSpinnerService: NgxSpinnerService) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['activityRoom'] && changes['activityRoom'].currentValue) {
      this.initMeetingNotes();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }


  onToggleMeetingNotes() {
    this.meetingNotesState = !this.meetingNotesState;
    this.meetingNotesStateChange.emit(this.meetingNotesState);
  }


  onSidebarHide() {
    this.meetingNotesState = false;
    this.meetingNotesStateChange.emit(false);

    if (!this.meetingNotesControl?.valid) {
      return;
    }

    const value = this.meetingNotesControl?.value;
    if (value !== this.meetingNotes) {
      if (this.activityRoom?.statusMetadata.isActive) {
        this.roomActionService.onUpdateMeetingNotes(this.activityId!, value!);
      } else {
        this.activityRoomService.onSetMeetingNotes(this.activityId!, value!).subscribe({
          next: () => {

          }
        })
      }
    }
  }

  onSidebarShow() {
    this.meetingNotesState = true;
    this.meetingNotesStateChange.emit(true);
  }


  initMeetingNotes() {
    if (this.activityRoom?.notesMetadata) {
      this.meetingNotes = this.activityRoom?.notesMetadata?.notes;
    } else {
      this.meetingNotes = `<h1>Event: ${ this.activityRoom?.activityName }</h1> <br> <p>[your notes]</p>`
    }
    this.meetingNotesControl = new FormControl(this.meetingNotes);
  }

  onGenerateNotes() {
    this.ngxSpinnerService.show('notesSpinner');
    this.activityRoomService.onGenerateNotes(this.activityId!).subscribe({
      next: (notes) => {
        this.meetingNotesControl = new FormControl<any>(notes.summary);
        this.meetingNotes = notes.summary;
        this.ngxSpinnerService.hide('notesSpinner');
      },
      error: () => {
        this.ngxSpinnerService.hide('notesSpinner');
      }
    })
  }

  downloadAsDoc(): void {
    const html = this.meetingNotesControl?.value; // or from formControl
    const header = `
    <html xmlns:o='urn:schemas-microsoft-com:office:office'
          xmlns:w='urn:schemas-microsoft-com:office:word'
          xmlns='http://www.w3.org/TR/REC-html40'>
    <head><meta charset='utf-8'></head><body>`;
    const footer = `</body></html>`;
    const sourceHTML = header + html + footer;

    const blob = new Blob([ '\ufeff', sourceHTML ], {
      type: 'application/msword'
    });

    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = this.activityRoom?.activityName + ' notes.doc';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }

  protected readonly control = control;
}
