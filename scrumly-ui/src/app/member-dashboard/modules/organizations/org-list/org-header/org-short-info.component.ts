import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { OrganizationDto, OrganizationInfoDto } from "../../model/organization.model";
import { Subject, takeUntil } from "rxjs";
import { OrganizationService } from "../../services/organization.service";

@Component({
  selector: 'org-short-info',
  templateUrl: './org-short-info.component.html',
})
export class OrgShortInfoComponent implements OnInit, OnDestroy {
  @Input() avatarSize: 'normal' | 'large' | 'xlarge' = 'normal';
  @Input() orgInfo: OrganizationInfoDto | OrganizationDto | null = null;

  private destroy$ = new Subject<void>();

  constructor(private readonly organizationService: OrganizationService) {
  }


  ngOnInit(): void {
    if (!this.orgInfo) {
      this.organizationService.onOrganizationSelectionChange
        .pipe(takeUntil(this.destroy$))
        .subscribe(org => {
          this.orgInfo = org?.org!;
        })
    }
  }


  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

}
