import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WorkspaceTimerComponent } from './workspace-timer.component';

describe('WorkspaceTimerComponent', () => {
  let component: WorkspaceTimerComponent;
  let fixture: ComponentFixture<WorkspaceTimerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [WorkspaceTimerComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(WorkspaceTimerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
