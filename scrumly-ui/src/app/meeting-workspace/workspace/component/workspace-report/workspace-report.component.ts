import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { ActivityRoomReport } from '../../model/activity-room-report.model';
import { ActivityRoomReportService } from '../../services/activity-room-report.service';
import { formatDuration } from '../../../../ui-components/services/utils';
import { ActivityBlockType } from '../../../../member-dashboard/modules/events/model/activity.model';
import { Router } from '@angular/router';
import { ActivityRoomService } from '../../services/activity-room.service';
import { MessageService } from 'primeng/api';
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';
import { SpinnerService } from '../../../../ui-components/services/spinner.service';
import { AuthService } from '../../../../auth/auth.service';


@Component({
  selector: 'workspace-report',
  templateUrl: './workspace-report.component.html',
  styleUrl: './workspace-report.component.css'
})
export class WorkspaceReportComponent implements OnInit {
  @Input() activityId?: string;
  @Input() roomReport?: ActivityRoomReport;
  @Input() isFacilitator: boolean = false;

  @Output() onViewWorkspace: EventEmitter<boolean> = new EventEmitter<boolean>();

  @Input() meetingNotesState: boolean = false;
  @Output() meetingNotesStateChange: EventEmitter<boolean> = new EventEmitter<boolean>();

  isLoad: boolean = false;
  isGeneratingReport: boolean = false;

  constructor(private activityRoomReportService: ActivityRoomReportService,
              private activityRoomService: ActivityRoomService,
              private messageService: MessageService,
              private router: Router) {
  }

  ngOnInit(): void {
  }

  test() {
    this.activityRoomReportService.test(this.activityId!).subscribe({
      next: (report) => {
        this.roomReport = report;
      }
    });
    this.activityRoomService.test(this.activityId!).subscribe({
      next: (report) => {
        // this.roomReport = report;
      }
    });
  }

  viewWorkspace() {
    this.onViewWorkspace.emit();
  }

  returnHome() {
    this.router.navigate([ '/app/home' ]);
  }

  downloadReport() {
    this.isGeneratingReport = true;
    setTimeout(() => {
      const data = document.getElementById('report');
      if (!data) {
        return;
      }

      this.isLoad = true;
      const fileName = this.roomReport?.activityName + '_' + this.roomReport?.timeMetadata?.finishDateTime + '.pdf';

      data.style.width = `${ data.scrollWidth }px`;
      data.style.height = `${ data.scrollHeight }px`;


      html2canvas(data, {
        scale: 3,
        useCORS: true,
        allowTaint: true,
        windowWidth: data.scrollWidth,
        windowHeight: data.scrollHeight
      })
        .then(canvas => {
          const imgData = canvas.toDataURL('image/png');
          const pdf = new jsPDF('p', 'mm', 'a4');
          const imgWidth = 210;
          const pageHeight = 297; // A4 height in mm
          const imgHeight = (canvas.height * imgWidth) / canvas.width;

          let heightLeft = imgHeight;
          let position = 0;

          pdf.addImage(imgData, 'PNG', 0, 0, imgWidth, imgHeight);
          heightLeft -= pageHeight;

          while (heightLeft > 0) {
            position -= pageHeight;
            pdf.addPage();
            pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight);
            heightLeft -= pageHeight;
          }

          pdf.save(fileName);
          this.messageService.add({
            severity: 'info',
            summary: 'Report saved',
            detail: 'Your report successfully saved',
          });
        })
        .catch(reason => {
          console.error(reason)
        })
        .finally(() => {
          this.isLoad = false;
          this.isGeneratingReport = false;
        });
    }, 100)
  }


  viewMeetingNotes() {
    this.meetingNotesState = true;
    this.meetingNotesStateChange.emit(this.meetingNotesState);
  }

  protected readonly reportError = reportError;
  protected readonly formatDuration = formatDuration;
  protected readonly ActivityBlockType = ActivityBlockType;
}
