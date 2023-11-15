import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoginRequest } from './login.request';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private authenticateApiUrl = 'localhost:8080/auth/authenticate';
  
  constructor(private http: HttpClient) {}
  
  login(loginRequest: LoginRequest): Observable<any> {
     return this.http.post<any>(this.authenticateApiUrl, { loginRequest });
   }
}