import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoginRequest } from '../models/requests/login.request';
import { AuthenticationResponse } from '../models/responses/login.response';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private authenticateApiUrl = "http://localhost:8080/auth/authenticate";

  constructor(private http: HttpClient) {}
  

  login(loginRequest: LoginRequest): Observable<any> {
    return this.http.post<AuthenticationResponse>(this.authenticateApiUrl, loginRequest);
   }
}