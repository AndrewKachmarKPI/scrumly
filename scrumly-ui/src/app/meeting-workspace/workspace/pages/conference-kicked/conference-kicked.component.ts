import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-conference-kicked',
  templateUrl: './conference-kicked.component.html',
  styleUrl: './conference-kicked.component.css'
})
export class ConferenceKickedComponent implements OnInit {

  constructor(private router: Router) {
  }

  ngOnInit(): void {
  }

  returnHome() {
    this.router.navigate([ '/app/home' ]);
  }
}
