import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../services/auth.service';
import { RouterOutlet } from '@angular/router';
import { LoginRequest } from '../models/requests/login.request';
import { CommonModule } from '@angular/common';
import { map } from 'rxjs/internal/operators/map';
import { AuthenticationResponse } from '../models/responses/login.response';
import { LoginErrorResponse } from '../models/error/login.error.response';
import { throwError } from 'rxjs/internal/observable/throwError';
import { catchError } from 'rxjs/internal/operators/catchError';
import { ServerErrorResponse } from '../models/error/server.error.response';
import { UserService } from '../services/user.service';

@Component({
    standalone: true,
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css'],
    imports: [RouterOutlet, ReactiveFormsModule,CommonModule]
})
export class LoginComponent implements OnInit {

  @Output() successfulLogin = new EventEmitter<string>();

  loginForm!: FormGroup;

  submitted: boolean = false;

  constructor(
    private authService: AuthService,
    private userService: UserService
  ) {
  }

  errorResponse: LoginErrorResponse | null = null;

  ngOnInit(): void {
    this.loginForm = new FormGroup({
      username: new FormControl(
        null,
        [
          Validators.required,
          Validators.email
        ]
      ),
      password: new FormControl(
        null,
        [
          Validators.required,
          Validators.pattern(new RegExp('^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$'))
        ]
      )
    })
  }

  login(): void {
    this.submitted = true;
    if (this.loginForm.valid) {
      const loginRequest: LoginRequest = {
        email: this.loginForm.value.username,
        password: this.loginForm.value.password
      };
      this.authService.login(loginRequest).pipe(
        map((response) => {
          if (response.token) {
            return new AuthenticationResponse(response.token);
          } else {
            return new ServerErrorResponse(response.message, response.status);
          }
        }),
        catchError((error: any) => {
          console.error('Error during authentication response mapping:', error);
          this.errorResponse = new LoginErrorResponse(error);
          return throwError(() => new Error(error));
        })
      ).subscribe({
        next: (response: AuthenticationResponse | ServerErrorResponse) => {
          if (response instanceof AuthenticationResponse) {
            if (localStorage.getItem("AuthToken")) {
              localStorage.removeItem("AuthToken");
            }
            localStorage.setItem('AuthToken', 'Bearer ' + response.token);
            console.info("Login successful: " + "The token is: " + localStorage.getItem('AuthToken'))
            this.userService.retrieveConnectedUser();
          } else {
            console.error(response.message, response.status);
          }},
        error: (error: Error) => {
          console.error('Login failed:', error);
        }
      });
    }
  }
}
