import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { AbstractControl, FormControl } from '@angular/forms';
import { DropdownChangeEvent } from 'primeng/dropdown';
import { IssueStatusDto } from '../../model/backlog.model';
import { IssueStatusService } from '../../services/issue-status.service';

@Component({
  selector: 'issue-status-dropdown',
  templateUrl: './issue-status-dropdown.component.html',
  styleUrl: './issue-status-dropdown.component.css'
})
export class IssueStatusDropdownComponent implements OnChanges {
  @Input() backlogId?: string;
  @Input() label: string = '';
  @Input() placeholder: string = '';
  @Input() disabled: boolean = false;
  @Input() showClear: boolean = false;
  @Input() selectFirst: boolean = false;
  @Input() styleClass: string = '';
  @Input() control = new FormControl();

  @Output() onSelectIssueStatus: EventEmitter<IssueStatusDto> = new EventEmitter<IssueStatusDto>();

  @Input() data: IssueStatusDto[] = [];
  @Input() defaultValue?: IssueStatusDto;
  issueStatusRow?: IssueStatusDto;

  constructor(private issueStatusService: IssueStatusService) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['disabled']) {
      if (changes['disabled'].currentValue) {
        this.control.disable();
      } else {
        this.control.enable();
      }
    }
    if (changes['defaultValue'] && changes['defaultValue'].currentValue) {
      this.issueStatusRow = this.defaultValue;
      this.control.setValue(this.defaultValue);
    }
    if (changes['backlogId'] && changes['backlogId'].currentValue) {
      this.findStatuses();
    }
  }


  ngOnInit(): void {
  }


  private findStatuses() {
    if (this.data.length > 0) {
      return;
    }
    this.issueStatusService.getAllIssueStatuses(this.backlogId!).subscribe({
      next: (data) => {
        this.data = data;
        if (this.selectFirst && this.data) {
          this.issueStatusRow = this.data[0];
          this.control.setValue(this.data[0]);
        }
      }
    })
  }

  onChangeSelection(event: DropdownChangeEvent) {
    const selected = event.value as IssueStatusDto;
    const selectedData = this.data
      .find(temp => temp.id === selected.id);
    this.issueStatusRow = selectedData;
    this.onSelectIssueStatus.emit(selectedData);
  }

  get hasError() {
    return this.control.errors && (this.control.touched || this.control.dirty);
  }

  get isRequired() {
    if (!this.control.validator) {
      return;
    }
    const validator = this.control.validator!({} as AbstractControl);
    return validator && validator['required'];
  }
}
