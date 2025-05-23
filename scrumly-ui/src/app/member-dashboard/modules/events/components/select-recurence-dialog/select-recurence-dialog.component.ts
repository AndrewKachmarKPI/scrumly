import { Component } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from "primeng/dynamicdialog";

@Component({
  selector: 'app-select-recurence-dialog',
  templateUrl: './select-recurence-dialog.component.html',
  styleUrl: './select-recurence-dialog.component.css'
})
export class SelectRecurenceDialogComponent {
  selectedRule: string = "";
  startDate: Date = new Date();


  constructor(public ref: DynamicDialogRef,
              public config: DynamicDialogConfig) {
    this.selectedRule = this.config.data.selectedRule;
    this.startDate = this.config.data.startDate;
  }

  onSelectRule(rule: string) {
    this.selectedRule = rule;
  }

  closeDialog() {
    this.ref.close();
  }

  submit() {
    this.ref.close(this.selectedRule);
  }
}
