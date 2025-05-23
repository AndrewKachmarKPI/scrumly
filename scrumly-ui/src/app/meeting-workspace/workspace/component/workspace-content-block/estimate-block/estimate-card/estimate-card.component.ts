import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { animate, state, style, transition, trigger } from '@angular/animations';

@Component({
  selector: 'estimate-card',
  templateUrl: './estimate-card.component.html',
  styleUrl: './estimate-card.component.css',
  animations: [
    trigger('flip', [
      state('front', style({ transform: 'rotateY(0deg)' })),
      state('back', style({ transform: 'rotateY(180deg)' })),
      transition('front <=> back', [
        animate('500ms ease-in-out')
      ])
    ])
  ]
})
export class EstimateCardComponent implements OnChanges {
  @Input() value?: string;
  @Input() isBack?: boolean = false;
  @Input() styleClass: string = '';
  flipState: 'front' | 'back' = 'front';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['isBack']) {
      this.flipState = this.isBack ? 'back' : 'front';
    }
  }

  getCardBackgroundColor(): string {
    const numberValue = parseInt(this.value!);

    // Define bounds based on Fibonacci sequence
    if (numberValue <= 3) {
      return 'bg-blue-200 border-blue-500'; // Light Blue for Fibonacci numbers in range 0 - 3
    } else if (numberValue <= 13) {
      return 'bg-green-200 border-green-500'; // Light Green for Fibonacci numbers in range 5 - 13
    } else if (numberValue <= 34) {
      return 'bg-yellow-200 border-yellow-500'; // Light Yellow for Fibonacci numbers in range 21 - 34
    } else if (numberValue <= 89) {
      return 'bg-indigo-200 border-indigo-500'; // Light Indigo for Fibonacci numbers in range 55 - 89
    } else if (numberValue <= 233) {
      return 'bg-teal-200 border-teal-500'; // Light Teal for Fibonacci numbers in range 144 - 233
    } else {
      return 'bg-green-200 border-green-500'; // Light Purple for Fibonacci numbers greater than 233 (e.g., 377, 610)
    }
  }
}
