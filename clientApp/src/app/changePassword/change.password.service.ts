import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ChangePasswordRequest } from './change.password.request';

@Injectable({
  providedIn: 'root'
})
export class ChangePasswordService {
  private changePasswordApiUrl = 'localhost:8080/user/changePassword';
  
  constructor(private http: HttpClient) {}
  
  login(changePasswordRequest: ChangePasswordRequest): Observable<any> {
     return this.http.patch<any>(this.changePasswordApiUrl, { changePasswordRequest });
   }
}