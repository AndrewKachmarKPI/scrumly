import { Component, OnInit } from '@angular/core';
import { PrimeNGConfig } from "primeng/api";
import { TranslateService } from "@ngx-translate/core";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  #scale = 14;

  constructor(private config: PrimeNGConfig,
              private translateService: TranslateService) {
  }

  ngOnInit() {
    this.config.ripple = true;
    document.documentElement.style.fontSize = `${ this.#scale }px`;
    this.translateService.addLangs(['en'])
    this.translateService.setDefaultLang('en');
    this.translateService.get('primeng').subscribe(res => this.config.setTranslation(res));
  }

  translate(lang: string) {
    this.translateService.use(lang);
    this.translateService.get('primeng').subscribe(res => this.config.setTranslation(res));
  }
}
