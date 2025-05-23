import { Component, OnDestroy } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { IssueDto, IssueExportOption, ProjectIssueType } from '../../model/backlog.model';
import { MessageService } from 'primeng/api';
import { Subject } from 'rxjs';
import { IssueService } from '../../services/issue.service';
import { CheckboxChangeEvent } from 'primeng/checkbox';
import { DropdownChangeEvent } from 'primeng/dropdown';


@Component({
  selector: 'export-issue-dialog',
  templateUrl: './export-issue-dialog.component.html',
  styleUrl: './export-issue-dialog.component.css'
})
export class ExportIssueDialogComponent implements OnDestroy {
  issue?: IssueDto;
  exportOptions: IssueExportOption[] = [];
  selectedOptions: IssueExportOption[] = [];

  isLoading: boolean = false;
  destroy$: Subject<void> = new Subject<void>();

  constructor(public ref: DynamicDialogRef,
              public config: DynamicDialogConfig,
              private issueService: IssueService,
              private messageService: MessageService) {
    this.issue = this.config.data.issue;
    this.getExportOptions();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  getExportOptions() {
    this.isLoading = true;
    this.issueService.getIssueExportOptions(this.issue?.issueKey!).subscribe({
      next: (options) => {
        this.isLoading = false;
        const exportedIdx = this.issue?.exportRefs?.map(value => value.projectName);
        this.exportOptions = options.filter(value => !exportedIdx?.includes(value.projectName));
      },
      error: () => {
        this.isLoading = false;
      }
    })
  }

  onChangeSelection(option: IssueExportOption, event: CheckboxChangeEvent) {
    if (event.checked) {
      this.selectedOptions.push(option);
    } else {
      const idx = this.selectedOptions.findIndex(value => value.projectId === option.projectId);
      this.selectedOptions.splice(idx, 1);
    }
  }

  isSelectedOption(option: IssueExportOption) {
    return !!this.selectedOptions.find(value => value.projectId === option.projectId);
  }

  onSelectIssueType(option: IssueExportOption, event: DropdownChangeEvent) {
    const type = event.value as ProjectIssueType;
    option.issueType = type;
  }

  onSubmitForm() {
    if (this.selectedOptions.length === 0) {
      this.messageService.add({
        severity: 'warning',
        summary: 'Invalid form',
        detail: 'Select at least one project',
      })
      return;
    }
    this.isLoading = true;
    const description = this.issue?.description;
    this.issueService.exportIssue(this.issue?.issueKey!, description!, this.selectedOptions)
      .subscribe({
        next: (issue) => {
          this.isLoading = false;
          this.messageService.add({
            severity: 'success',
            summary: 'Issue exported',
            detail: 'You have successfully exported this issue',
          });
          this.ref.close(issue);
        },
        error: () => {
          this.isLoading = false;
          this.messageService.add({
            severity: 'error',
            summary: 'Issue not exported',
            detail: 'Failed to export issue',
          });
        }
      })
  }

  closeDialog() {
    this.ref.close(false);
  }

}
