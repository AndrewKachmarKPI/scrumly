import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { AbstractControl, FormControl } from '@angular/forms';
import { IssueStatusDto, IssueTypeDto } from '../../model/backlog.model';
import { IssueStatusService } from '../../services/issue-status.service';
import { DropdownChangeEvent } from 'primeng/dropdown';
import { IssueTypeService } from '../../services/issue-type.service';

@Component({
  selector: 'issue-type-dropdown',
  templateUrl: './issue-type-dropdown.component.html',
  styleUrl: './issue-type-dropdown.component.css'
})
export class IssueTypeDropdownComponent implements OnChanges {
  @Input() backlogId?: string;
  @Input() label: string = '';
  @Input() placeholder: string = '';
  @Input() disabled: boolean = false;
  @Input() showClear: boolean = false;
  @Input() selectFirst: boolean = false;
  @Input() control = new FormControl();

  @Output() onSelectIssueType: EventEmitter<IssueTypeDto> = new EventEmitter<IssueTypeDto>();

  @Input() data: IssueTypeDto[] = [];
  issueStatusRow?: IssueTypeDto;

  constructor(private issueTypeService: IssueTypeService) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['disabled']) {
      if (changes['disabled'].currentValue) {
        this.control.disable();
      } else {
        this.control.enable();
      }
    }
    if (changes['backlogId'] && changes['backlogId'].currentValue) {
      this.findStatuses();
    }
  }


  ngOnInit(): void {
  }


  private findStatuses() {
    this.issueTypeService.getAllIssueTypes(this.backlogId!).subscribe({
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
    const selectedData = this.data
      .find(temp => temp.id === event.value);
    this.issueStatusRow = selectedData;
    this.onSelectIssueType.emit(selectedData);
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
