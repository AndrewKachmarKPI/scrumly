import { ElementRef, Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, filter, Subject, takeUntil } from 'rxjs';


export interface LayoutConfig {
  scenary: ElementRef,
  conference: ElementRef,
  dish: ElementRef,
  screen: ElementRef
}

type ConferenceLayoutType = 'conference' | 'screen' | 'workspace';


@Injectable({
  providedIn: 'root'
})
export class ConferenceLayoutService implements OnDestroy {
  private _ratios = [ '16:9', '4:3', '1:1', '1:2' ];
  private _scenary: HTMLElement | null = null;
  private _dish: HTMLElement | null = null;
  private _screen: HTMLElement | null = null;
  private _conference: HTMLElement | null = null;
  private _margin = 10;
  private _aspect = 0;
  private _ratio = this.ratio();
  private _width = 0;
  private _height = 0;
  private _minCameraSize = 150;

  private _layoutType: ConferenceLayoutType = 'conference';
  private destroy$: Subject<void> = new Subject();
  private screenDisplayLayoutState: BehaviorSubject<ConferenceLayoutType | undefined> = new BehaviorSubject<ConferenceLayoutType | undefined>(undefined);
  onScreenDisplayLayoutChange = this.screenDisplayLayoutState.asObservable();

  constructor() {
    this.onScreenDisplayLayoutChange
      .pipe(filter(Boolean), takeUntil(this.destroy$))
      .subscribe(layout => {
        if (layout != this._layoutType) {
          this.onChangeLayout(layout);
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  init(config: LayoutConfig): void {
    this._scenary = config.scenary.nativeElement;
    this._conference = config.conference.nativeElement;
    this._dish = config.dish.nativeElement;
    this._screen = config.screen.nativeElement;

    if (!this._conference || !this._dish) {
      console.error('Error: .Conference or .Dish elements not found in the component.');
      return;
    }

    this.resize();
  }

  destroy() {
    this._scenary = null;
  }

  private dimensions() {
    if (this._dish) {
      this._width = this._dish.offsetWidth - this._margin * 2;
      this._height = this._dish.offsetHeight - this._margin * 2;
    }
  }

  resize() {
    this.dimensions();
    let max = 0;
    let i = this._minCameraSize;
    while (i < 5000) {
      let area = this.area(i);
      if (!area) {
        max = i - 1;
        break;
      }
      i++;
    }
    max = Math.max(max - this._margin * 2, this._minCameraSize);
    this.resizer(max);
  }

  private resizer(width: number) {
    if (!this._dish) return;

    const cameras = this._dish.querySelectorAll('.Camera');
    cameras.forEach((element: Element) => {
      const camera = element as HTMLElement;
      camera.style.margin = `${ this._margin }px`;
      camera.style.width = `${ width }px`;
      camera.style.height = `${ width * this._ratio }px`;
      camera.setAttribute('data-aspect', this._ratios[this._aspect]);
    });
  }


  private ratio() {
    let ratio = this._ratios[this._aspect].split(':');
    return parseFloat(ratio[1]) / parseFloat(ratio[0]);
  }

  private area(increment: number) {
    if (!this._dish) return false;

    let i = 0,
      w = 0;
    let h = increment * this._ratio + this._margin * 2;
    const cameras = this._dish.querySelectorAll('.Camera');

    while (i < cameras.length) {
      if (w + increment > this._width) {
        w = 0;
        h += increment * this._ratio + this._margin * 2;
      }
      w += increment + this._margin * 2;
      i++;
    }
    return h > this._height || increment > this._width ? false : increment;
  }

  setAspect(i: number) {
    this._aspect = i;
    this._ratio = this.ratio();
    this.resize();
  }

  changeLayout(layout: ConferenceLayoutType) {
    this.screenDisplayLayoutState.next(layout);
  }

  toggleCamera(cameraId: string) {
    if (!this._dish) return;
    const cameras = this._dish.querySelectorAll('#' + cameraId);
    cameras.forEach(element => {
      const camera = element as HTMLElement;
      camera.classList.toggle('d-block');
      camera.classList.toggle('d-none');
    });
  }


  get layoutType(): ConferenceLayoutType {
    return this._layoutType;
  }

  private onChangeLayout(layout: ConferenceLayoutType) {
    this._layoutType = layout;
    if (layout === 'screen' || layout === 'workspace') {
      this._screen?.classList.add('d-block');
      this._screen?.classList.remove('d-none');
    } else {
      this._screen?.classList.add('d-none');
      this._screen?.classList.remove('d-block');
    }
    this.resize();
  }
}
