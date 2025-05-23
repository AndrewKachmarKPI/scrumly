import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { AbstractControl, FormControl } from '@angular/forms';
import { IssueStatusDto } from '../../model/backlog.model';
import { IssueStatusService } from '../../services/issue-status.service';
import { DropdownChangeEvent } from 'primeng/dropdown';
import { UserProfileDto } from '../../../../../auth/auth.model';
import { TeamService } from '../../../organizations/services/team.service';
import { BacklogService } from '../../services/backlog.service';

@Component({
  selector: 'issue-assignee-dropdown',
  templateUrl: './issue-assignee-dropdown.component.html',
  styleUrl: './issue-assignee-dropdown.component.css'
})
export class IssueAssigneeDropdownComponent implements OnChanges {
  @Input() backlogId?: string;
  @Input() label: string = '';
  @Input() placeholder: string = '';
  @Input() disabled: boolean = false;
  @Input() readonly: boolean = false;
  @Input() showClear: boolean = false;
  @Input() selectFirst: boolean = false;
  @Input() styleClass: string = '';
  @Input() control = new FormControl();
  @Input() hideAvatar: boolean = true;
  @Input() hideEmail: boolean = true;

  @Output() onSelectAssignee: EventEmitter<UserProfileDto> = new EventEmitter<UserProfileDto>();

  @Input() data: UserProfileDto[] = [];
  @Input() defaultValue?: UserProfileDto;
  issueStatusRow?: UserProfileDto;

  constructor(private backlogService: BacklogService) {
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
      this.setDefaultValue();
    }
    if (changes['backlogId'] && changes['backlogId'].currentValue) {
      this.findAssignee();
    }
  }


  private setDefaultValue() {
    const user = this.data.find(value => value.userId === this.defaultValue?.userId);
    this.issueStatusRow = user;
    this.control.setValue(user);
  }

  ngOnInit(): void {
  }


  private findAssignee() {
    if (this.data.length > 0) {
      return;
    }
    this.backlogService.getAllAssignee(this.backlogId!).subscribe({
      next: (data) => {
        this.data = data;
        if (this.selectFirst && this.data) {
          this.issueStatusRow = this.data[0];
          this.control.setValue(this.data[0]);
        }
        if (this.defaultValue) {
          this.setDefaultValue();
        }
      }
    })
  }

  onChangeSelection(event: DropdownChangeEvent) {
    const selected = event.value as IssueStatusDto;
    const selectedData = this.data
      .find(temp => temp.id === selected.id);
    this.issueStatusRow = selectedData;
    this.onSelectAssignee.emit(selectedData);
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
