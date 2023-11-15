import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from './auth.service';
import { RouterOutlet } from '@angular/router';
import { LoginRequest } from './login.request';
import { CommonModule } from '@angular/common';

@Component({
    standalone: true,
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css'],
    imports: [RouterOutlet, ReactiveFormsModule,CommonModule]
})
export class LoginComponent {

  loginForm = new FormGroup({
    username: new FormControl('',[Validators.required, Validators.email]),
    password: new FormControl('',[Validators.required, Validators.pattern(new RegExp('^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$'))]),
  });
  
  constructor(
    private authService: AuthService
    ) {}

  login(): void {
    
    if (this.loginForm.valid) {
      const loginRequest =  new LoginRequest(
        this.loginForm.value.username!,
        this.loginForm.value.password!
        );

      this.authService.login(loginRequest).subscribe({
        next: (response) => {
          const jwtToken = response.jwtToken;
          localStorage.setItem('jwtToken', jwtToken);
          // Optionally, navigate to a different route or perform other actions
        },
        error: (error) => {
          console.error('Login failed:', error);
        }
      });
    }
    else {
      console.error('Invalid form submission:', this.loginForm.errors);
    }
  }
}

