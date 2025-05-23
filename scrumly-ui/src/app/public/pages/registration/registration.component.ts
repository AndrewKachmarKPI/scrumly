import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { AuthService } from "../../../auth/auth.service";
import { MessageService } from "primeng/api";
import { TranslateService } from "@ngx-translate/core";
import {
  confirmPasswordValidator,
  control,
  strongPasswordValidator
} from "../../../ui-components/services/utils";

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrl: './registration.component.css'
})
export class RegistrationComponent implements OnInit {
  public registerFormGroup: FormGroup | undefined;
  public maxDate: Date = new Date();

  constructor(private readonly authService: AuthService,
              private readonly translateService: TranslateService,
              private readonly messageService: MessageService) {
  }

  ngOnInit(): void {
    this.registerFormGroup = new FormGroup({
      email: new FormControl<string>("", Validators.compose([
        Validators.email, Validators.required
      ])),
      firstName: new FormControl<string>("", Validators.compose([
        Validators.required, Validators.minLength(2), Validators.maxLength(100)
      ])),
      lastName: new FormControl<string>("", Validators.compose([
        Validators.required, Validators.minLength(2), Validators.maxLength(100)
      ])),
      password: new FormControl<string>("", Validators.compose([
        Validators.required,
        Validators.minLength(10),
        strongPasswordValidator
      ])),
      confirmPassword: new FormControl<string>("", Validators.compose([
        Validators.required, strongPasswordValidator
      ])),
      dateOfBirth: new FormControl<string>("", Validators.compose([
        Validators.required
      ])),
    }, { validators: [confirmPasswordValidator] })
  }

  register() {
    if (this.registerFormGroup?.invalid) {
      this.messageService.add({
        severity: 'error',
        summary: this.translateService.instant('messages.error.invalidForm.summary'),
        detail: this.translateService.instant('messages.error.invalidForm.detail'),
      });
      this.registerFormGroup.markAllAsTouched();
      return;
    }

    this.authService.register(this.registerFormGroup?.value).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: this.translateService.instant('messages.success.registration.summary'),
          detail: this.translateService.instant('messages.success.registration.detail', {
            username: this.control(this.registerFormGroup!, 'email').value
          }),
        });
        setTimeout(() => {
          this.authService.login({
            redirectUri: window.location.origin + '/app/settings'
          })
        }, 1000)
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: this.translateService.instant('messages.error.registration.summary'),
          detail: err.error.message,
        })
      }
    })
  }

  protected readonly control = control;
}
