import { Component, OnInit } from '@angular/core';
import { UserProfileDto, UserProfileRQ } from "../../../../../auth/auth.model";
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { AuthService } from "../../../../../auth/auth.service";
import { TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { control, joinBy, specialCharacterValidator } from "../../../../../ui-components/services/utils";

@Component({
  selector: 'app-member-profile',
  templateUrl: './member-profile.component.html',
  styleUrl: './member-profile.component.css'
})
export class MemberProfileComponent implements OnInit {
  public profile!: UserProfileDto;
  public profileFormGroup!: FormGroup;
  public maxDate: Date = new Date();

  constructor(public readonly authService: AuthService,
              private readonly translateService: TranslateService,
              private readonly messageService: MessageService) {
  }

  ngOnInit(): void {
    this.profileFormGroup = new FormGroup({
      email: new FormControl<string>("", Validators.compose([
        Validators.email, Validators.required
      ])),
      firstName: new FormControl<string>("", Validators.compose([
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(100),
        specialCharacterValidator
      ])),
      lastName: new FormControl<string>("", Validators.compose([
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(100),
        specialCharacterValidator
      ])),
      dateOfBirth: new FormControl<string>("", Validators.compose([
        Validators.required
      ])),
      bio: new FormControl<string>("", Validators.compose([
        Validators.maxLength(300),
        specialCharacterValidator
      ])),
      phoneNumber: new FormControl<string>("", Validators.compose([
        Validators.maxLength(15)
      ])),
      skills: new FormControl([], Validators.compose([
        specialCharacterValidator, Validators.maxLength(10)
      ])),
      avatarFile: new FormControl(),
      isRemoveAvatar: new FormControl(false)
    });

    this.control(this.profileFormGroup, 'email').disable();

    this.authService.onProfileUpdate.subscribe(profile => {
      if (profile) {
        this.updateProfileData(profile);
      }
    })
  }


  private updateProfileData(profile: UserProfileDto) {
    this.profile = profile;
    this.profileFormGroup?.setValue({
      email: profile.email,
      firstName: profile.firstName,
      lastName: profile.lastName,
      dateOfBirth: new Date(profile.dateOfBirth),
      bio: profile.bio,
      phoneNumber: profile.phoneNumber,
      avatarFile: profile.avatarId,
      isRemoveAvatar: false,
      skills: profile.skills
    })
  }

  updateProfile() {
    if (this.profileFormGroup?.invalid) {
      this.messageService.add({
        severity: 'error',
        summary: this.translateService.instant('messages.error.invalidForm.summary'),
        detail: this.translateService.instant('messages.error.invalidForm.detail'),
      });
      this.profileFormGroup.markAllAsTouched();
      return;
    }

    const profile: UserProfileRQ = this.profileFormGroup?.value;
    profile.dateOfBirth = new Date(profile.dateOfBirth).toJSON();

    const file: File = this.control(this.profileFormGroup!, 'avatarFile').value;
    this.authService.updateProfile(profile, file).subscribe({
      next: (profile) => {
        this.authService.updateProfileData(profile);
        this.messageService.add({
          severity: 'success',
          summary: this.translateService.instant('messages.success.profile.summary'),
          detail: this.translateService.instant('messages.success.profile.detail', {
            username: profile.email
          }),
        });
        this.updateProfileData(profile);
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: this.translateService.instant('messages.error.profile.summary'),
          detail: this.translateService.instant('messages.error.profile.detail'),
        });
      }
    })
  }

  onFileSelect(file: File) {
    this.control(this.profileFormGroup!, 'avatarFile').setValue(file);
    this.control(this.profileFormGroup!, 'isRemoveAvatar').setValue(false);
    this.updateProfile();
  }

  onRemoveFile() {
    this.control(this.profileFormGroup!, 'isRemoveAvatar').setValue(true);
  }


  protected readonly control = control;
  protected readonly joinBy = joinBy;
}
