import {Component, OnInit} from '@angular/core';
import {ThemeService} from "../../../ui-components/services/theme.service";

export interface MenuOption {
  url: string,
  name: string
}

@Component({
  selector: 'app-footer-menu',
  templateUrl: './footer-menu.component.html',
  styleUrl: './footer-menu.component.css'
})
export class FooterMenuComponent implements OnInit {

  menuOptions: MenuOption[] = [];

  constructor(private readonly themeService: ThemeService) {
  }

  ngOnInit(): void {
    this.menuOptions = [
      {
        name: 'Brand Policy',
        url: '/'
      },
      {
        name: 'Privacy Policy',
        url: '/'
      },
      {
        name: 'Terms of Service',
        url: '/'
      },
      {
        name: 'FAQ',
        url: '/faq'
      }
    ]
  }

  get year() {
    return new Date().getFullYear();
  }

}
