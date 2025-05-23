import { Component } from '@angular/core';
import {
  control,
  joinBy,
  lineEmailValidator,
  specialCharacterValidator
} from "../../../../../ui-components/services/utils";
import { DynamicDialogConfig, DynamicDialogRef } from "primeng/dynamicdialog";
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { OrganizationService } from "../../services/organization.service";
import { CreateOrganizationRQ } from "../../model/organization.model";
import { Router } from "@angular/router";
import { MessageService } from "primeng/api";

@Component({
  selector: 'org-create-dialog',
  templateUrl: './org-create-dialog.component.html',
  styleUrl: './org-create-dialog.component.css'
})
export class OrgCreateDialogComponent {
  public createOrganizationGroup!: FormGroup;

  constructor(public ref: DynamicDialogRef,
              public config: DynamicDialogConfig,
              public organizationService: OrganizationService,
              private messageService: MessageService,
              private router: Router) {
    this.createOrganizationGroup = new FormGroup({
      organizationName: new FormControl('', Validators.compose([
        specialCharacterValidator, Validators.required, Validators.maxLength(200)
      ])),
      teamName: new FormControl('', Validators.compose([
        specialCharacterValidator, Validators.maxLength(200)
      ])),
      inviteMembers: new FormControl([], Validators.compose([
        Validators.maxLength(10)
      ])),
      logo: new FormControl(),
      selectedLogo: new FormControl("")
    })
  }


  onSubmitForm() {
    if (!this.createOrganizationGroup.valid) {
      this.messageService.add({
        severity: 'warning',
        summary: 'Invalid form',
        detail: 'Check form validity',
      })
      return;
    }
    const rq: CreateOrganizationRQ = {
      organizationName: this.control(this.createOrganizationGroup, 'organizationName').value,
      teamName: this.control(this.createOrganizationGroup, 'teamName').value,
      inviteMembers: this.control(this.createOrganizationGroup, 'inviteMembers').value
    };
    const logo = this.control(this.createOrganizationGroup, 'logo').value;
    this.organizationService.createOrganization(rq, logo).subscribe({
      next: (response) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Organization created',
          detail: 'You have successfully created new organization',
        });
        this.ref.close(true)
        this.router.navigate([`/app/org/${ response.organizationId }/dashboard`]);
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Failed organization created',
          detail: 'Error while creating organization check form validity',
        })
      }
    })
  }

  closeDialog() {
    this.ref.close(false);
  }

  onLogoUpload(file: File) {
    this.control(this.createOrganizationGroup, 'logo').setValue(file);
    const reader = new FileReader();
    reader.onload = () => {
      const image = reader.result as string;
      this.control(this.createOrganizationGroup, 'selectedLogo').setValue(image);
    };
    reader.readAsDataURL(file);
  }

  protected readonly control = control;
  protected readonly joinBy = joinBy;
}
