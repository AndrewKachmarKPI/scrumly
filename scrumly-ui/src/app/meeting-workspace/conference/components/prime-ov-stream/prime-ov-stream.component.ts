import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { StreamModel } from 'openvidu-angular';

@Component({
  selector: 'prime-ov-stream',
  templateUrl: './prime-ov-stream.component.html',
  styleUrl: './prime-ov-stream.component.css'
})
export class PrimeOvStreamComponent implements OnInit {
  @Input() stream?: StreamModel;
  @Input() isPinned?: boolean;
  @Input() displayActions: boolean = true;
  @Output() onPinStreamEmit = new EventEmitter();
  @Output() onUnPinStreamEmit = new EventEmitter();
  isActionsShown: boolean = false;

  ngOnInit(): void {
  }


  onPinStream() {
    this.onPinStreamEmit.emit();
  }

  onUnPinStream() {
    this.onUnPinStreamEmit.emit();
  }
}
