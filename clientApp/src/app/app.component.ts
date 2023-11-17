import { Component, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { LoginComponent } from "./login/login.component";

@Component({
    selector: 'app-root',
    standalone: true,
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css'],
    imports: [CommonModule, RouterOutlet, MatIconModule, LoginComponent]
})
export class AppComponent {
  title = 'clientApp';
  constructor(private router: Router) {}

  isCenteredLayout() {
    const route = this.router.url;
    return route.includes('/login') || route.includes('/change-password') || route.includes('/register');
  }
  
} 
