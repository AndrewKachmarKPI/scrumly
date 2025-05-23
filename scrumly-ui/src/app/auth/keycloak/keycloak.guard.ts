import {ActivatedRouteSnapshot, CanActivate, CanActivateFn, RouterStateSnapshot, UrlTree} from '@angular/router';
import {Observable} from "rxjs";
import {KeycloakService} from "keycloak-angular";
import {inject, Injectable} from "@angular/core";
import {AuthService} from "../auth.service";

export const KeycloakGuard: CanActivateFn = (route, state): boolean => {
  const authService = inject(AuthService);
  if (authService.isLoggedIn()) {
    return true;
  }
  authService.login();
  return false;
};
