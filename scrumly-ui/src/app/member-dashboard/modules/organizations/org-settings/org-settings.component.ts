import { Component, OnInit } from '@angular/core';
import { OrganizationService } from "../services/organization.service";
import { ActivatedRoute, Router } from "@angular/router";
import {
  OrganizationDto,
  OrganizationInfoDto,
  OrganizationMemberRole,
  OrganizationStatus
} from "../model/organization.model";
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { control, joinBy, specialCharacterValidator } from "../../../../ui-components/services/utils";
import { ConfirmationService, MessageService } from "primeng/api";

@Component({
  selector: 'app-org-settings',
  templateUrl: './org-settings.component.html',
  styleUrl: './org-settings.component.css'
})
export class OrgSettingsComponent implements OnInit {
  public orgId?: string;
  public organization?: OrganizationInfoDto;
  public organizationFormGroup: FormGroup;

  constructor(private organizationService: OrganizationService,
              private messageService: MessageService,
              private confirmationService: ConfirmationService,
              private router: Router,
              private activateRoute: ActivatedRoute) {
    this.organizationFormGroup = new FormGroup({
      name: new FormControl<string>("", Validators.compose([
        specialCharacterValidator, Validators.required, Validators.maxLength(200)
      ])),
      about: new FormControl<string>("", Validators.compose([
        Validators.maxLength(300),
        specialCharacterValidator
      ])),
      logo: new FormControl(),
      isRemoveLogo: new FormControl(false),
    });
  }

  ngOnInit(): void {
    this.activateRoute.params.subscribe(params => {
      this.orgId = params['orgId'];
      this.loadOrganization();
    });
  }


  private loadOrganization() {
    this.organizationService.findOrganizationById(this.orgId!).subscribe({
      next: (org) => {
        this.organization = org;
        this.updateOrgValue(org);
        if (!this.isOrganizationAdmin() || this.organization.status != OrganizationStatus.ACTIVE) {
          this.organizationFormGroup.disable();
        } else {
          this.organizationFormGroup.enable();
        }
      }
    });
  }

  private updateOrgValue(org: OrganizationDto | OrganizationInfoDto) {
    this.organizationFormGroup.setValue({
      name: org.name,
      about: org.about || '',
      logo: org.logo || '',
      isRemoveLogo: false
    })
  }

  updateOrganization() {
    if (this.organizationFormGroup?.invalid) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Invalid form',
        detail: 'Check form validity',
      });
      this.organizationFormGroup.markAllAsTouched();
      return;
    }

    const logo: File = this.control(this.organizationFormGroup!, 'logo').value;
    const orgInfo = this.organizationFormGroup.value;
    this.organizationService.updateOrganization(this.organization?.organizationId!, orgInfo, logo).subscribe({
      next: (org) => {
        this.organization = org;
        this.organizationService.onOrganizationEventSubject.next(true);
        this.updateOrgValue(org);
        this.messageService.add({
          severity: 'success',
          summary: 'Organization updated',
          detail: 'Your organization has been updated',
        });
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Failed to update organization',
          detail: err.error.message,
        });
      }
    })
  }

  archiveOrganization() {
    this.confirmationService.confirm({
      message: 'Are you sure that you want to archive organization?',
      header: 'Archive organization',
      icon: 'pi pi-exclamation-triangle',
      acceptIcon: "none",
      rejectIcon: "none",
      acceptButtonStyleClass: 'p-button-success',
      rejectButtonStyleClass: "p-button-text",
      accept: () => {
        this.organizationService.archiveOrganization(this.organization?.organizationId!).subscribe({
          next: (org) => {
            this.messageService.add({
              severity: 'success',
              summary: 'Organization archived',
              detail: `${ org.name } is archived`,
            });
            this.organization = org;
            if (!this.isOrganizationAdmin() || !this.organization.isActive) {
              this.organizationFormGroup.disable();
            }
            this.organizationService.onOrganizationEventSubject.next(true);
          },
          error: (err) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Failed to Archive organization',
              detail: err.error.message,
            });
          },
        });
      }
    });
  }

  deleteOrganization() {
    if (this.organization?.status != OrganizationStatus.ARCHIVED) {
      return;
    }
    this.confirmationService.confirm({
      message: 'Are you sure that you want to delete organization?',
      header: 'Archive organization',
      icon: 'pi pi-exclamation-triangle',
      acceptIcon: "none",
      rejectIcon: "none",
      acceptButtonStyleClass: 'p-button-success',
      rejectButtonStyleClass: "p-button-text",
      accept: () => {
        this.organizationService.deleteOrganization(this.organization?.organizationId!).subscribe({
          next: (org) => {
            this.messageService.add({
              severity: 'success',
              summary: 'Organization deleted',
              detail: `Your organization is deleted`,
            });
            this.router.navigate(['app/org/list'])
            this.organizationService.onOrganizationEventSubject.next(true);
          },
          error: (err) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Failed to Archive organization',
              detail: err.error.message,
            });
          },
        });
      }
    });
  }

  onFileSelect(file: File) {
    this.control(this.organizationFormGroup!, 'logo').setValue(file);
    this.control(this.organizationFormGroup!, 'isRemoveLogo').setValue(false);
    this.updateOrganization();
  }

  onRemoveFile() {
    this.control(this.organizationFormGroup!, 'isRemoveLogo').setValue(true);
    this.updateOrganization();
  }

  isOrganizationAdmin() {
    return this.organization?.orgMember.role === OrganizationMemberRole.ORGANIZATION_ADMIN
  }

  getOrgSeverity() {
    if (this.organization?.status === OrganizationStatus.ARCHIVED) {
      return 'secondary';
    }
    return 'success';
  }


  protected readonly control = control;
  protected readonly joinBy = joinBy;
  protected readonly OrganizationStatus = OrganizationStatus;
}
