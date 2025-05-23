import { Component, OnInit } from '@angular/core';
import { MenuItem } from "primeng/api";
import { AuthService } from "../../../auth/auth.service";
import { Router } from "@angular/router";
import { ThemeService } from "../../../ui-components/services/theme.service";
import { Observable } from "rxjs";
import { KeycloakProfile } from "keycloak-js/dist/keycloak";
import { UserProfileDto } from "../../../auth/auth.model";

@Component({
  selector: 'app-header-menu',
  templateUrl: './header-menu.component.html',
  styleUrl: './header-menu.component.css'
})
export class HeaderMenuComponent implements OnInit {
  items: MenuItem[] | undefined;

  constructor(private readonly authService: AuthService,
              private readonly themeService: ThemeService,
              private readonly router: Router) {
  }

  ngOnInit() {
    this.items = [
      {
        label: 'Features',
        icon: 'pi pi-star',
        items: [
          {
            label: 'Core',
            icon: 'pi pi-bolt',
            shortcut: '⌘+S'
          },
          {
            label: 'Blocks',
            icon: 'pi pi-server',
            shortcut: '⌘+B'
          },
          {
            label: 'UI Kit',
            icon: 'pi pi-pencil',
            shortcut: '⌘+U'
          },
          {
            separator: true
          },
          {
            label: 'Templates',
            icon: 'pi pi-palette',
            items: [
              {
                label: 'Apollo',
                icon: 'pi pi-palette',
                badge: '2'
              },
              {
                label: 'Ultima',
                icon: 'pi pi-palette',
                badge: '3'
              }
            ]
          }
        ]
      },
      {
        label: 'Contact',
        icon: 'pi pi-envelope'
      }
    ];
  }

  login() {
    this.authService.login({
      redirectUri: window.location.origin + '/app/home'
    })
  }

  logout() {
    this.authService.logout();
  }

  register() {
    this.router.navigate(['/register'])
  }

  toggleTheme() {
    this.themeService.toggleTheme();
  }

  isLoggedIn() {
    return this.authService.isLoggedIn()
  }
}
