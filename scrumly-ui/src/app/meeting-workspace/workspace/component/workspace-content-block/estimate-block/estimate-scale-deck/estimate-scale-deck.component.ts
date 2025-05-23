import { Component, EventEmitter, Input, Output } from '@angular/core';
import { EstimateScaleMetadata, UserEstimateMetadata } from '../../../../model/activity-room.model';
import {
  control,
  specialCharacterValidator,
  trackById,
  trackByValue
} from '../../../../../../ui-components/services/utils';
import { FormControl, Validators } from '@angular/forms';
import { OverlayPanel } from 'primeng/overlaypanel';

@Component({
  selector: 'estimate-scale-deck',
  templateUrl: './estimate-scale-deck.component.html',
  styleUrl: './estimate-scale-deck.component.css'
})
export class EstimateScaleDeckComponent {
  @Input() scaleMetadata?: EstimateScaleMetadata;
  @Input() userEstimate?: UserEstimateMetadata;
  @Output() onSelectEstimate: EventEmitter<string> = new EventEmitter<string>();

  maxAngle = 20; // Spread out more
  spread = 60; // Controls horizontal spacing
  hoverLift = 20; // Moves up on hover
  hoverPush = 20; // Moves outward on hover
  hoveredIndex: number | null = null; // Track hovered card

  isCustomEstimate: boolean = false;
  estimateControl = new FormControl('', Validators.compose([
    Validators.required, specialCharacterValidator, Validators.maxLength(3)
  ]));

  getTransform(index: number, card: string): string {
    const isSelected = this.userEstimate?.estimate === card;
    const isHovered = this.hoveredIndex === index;
    let totalCards = this.scaleMetadata?.scale.length || 1;
    totalCards = totalCards + 1;
    const startAngle = -this.maxAngle / 2;
    const angle = startAngle + (index * (this.maxAngle / (totalCards - 1)));
    const offsetX = index * this.spread - ((totalCards - 1) * this.spread) / 2;

    let translateY = 0;
    let extraX = 0;
    let scale = 1; // Default scale
    let zIndex = isHovered ? 10 : index; // Default z-index

    if (this.hoveredIndex === index) {
      translateY = -this.hoverLift; // Lift up on hover
      extraX = Math.sin((angle * Math.PI) / 180) * this.hoverPush; // Push outward on hover
    }

    // If the card is selected, apply scaling, lift, and bring it to the front
    if (isSelected) {
      scale = 1.1; // Scale up when selected
      translateY = -20; // Lift the selected card upwards
      zIndex = index + 1; // Bring the selected card to the front
    }

    return `transform: rotate(${ angle }deg) translateX(${ offsetX + extraX }px) translateY(${ translateY }px) scale(${ scale }); zIndex: ${ zIndex };`;
  }


  onHover(index: number, isHovering: boolean): void {
    this.hoveredIndex = isHovering ? index : null;
  }

  onClick(scale: string): void {
    this.onSelectEstimate.emit(scale);
  }

  onOpenCustomEstimate(event: Event, op: OverlayPanel) {
    op.toggle(event);
    this.isCustomEstimate = true;
  }

  onProvideCustomEstimate(event: Event, op: OverlayPanel) {
    if (!this.estimateControl.valid) {
      return;
    }
    op.toggle(event);
    this.onSelectEstimate.emit(this.estimateControl.value!);
  }


  protected readonly control = control;
  protected readonly trackById = trackById;
  protected readonly trackByValue = trackByValue;
}
