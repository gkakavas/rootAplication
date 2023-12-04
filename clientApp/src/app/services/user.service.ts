import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import {CurrentUserResponse} from '../models/responses/users/current.user.response';
import {map} from "rxjs/internal/operators/map";
import {ServerErrorResponse} from "../models/error/server.error.response";
import {catchError} from "rxjs/internal/operators/catchError";
import {throwError} from "rxjs/internal/observable/throwError";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private getCurrentUserApiUrl = "http://localhost:8080/user/currentUser";
  private getUserByIdApiUrl = "http://localhost:8080/user/";
  private token:string ='';

  constructor(private http: HttpClient) {
  }

  retrieveConnectedUser(): void {
    this.token = localStorage.getItem("AuthToken")!;
    this.http.get<any>(this.getCurrentUserApiUrl, {
      headers: {
        'Authorization' : this.token /*`${token}`*/,
      }}).pipe(
      map((response) => {
        if (response.userId) {
          return new CurrentUserResponse(
            response.userId,
            response.firstname,
            response.lastname,
            response.email,
            response.specialization,
            response.currentProject,
            response.groupName,
            response.role,
            response.authorities
          );
        }
        else {
          return new ServerErrorResponse(response.message,response.status)
        }
      }),
      catchError((error: any) => {
        console.error('Error during mapping current User object:', error);
        return throwError(() => new Error(error));
      })
    ).subscribe({
        next: (response) => {
          if(response instanceof CurrentUserResponse){
            const connectedUser = {
              userId: response.userId,
              firstname:response.firstname,
              lastname:response.lastname,
              email:response.email,
              specialization: response.specialization,
              currentProject: response.currentProject,
              groupName:response.groupName,
              role:response.role,
              authorities: response.authorities
            }
            localStorage.setItem("ConnectedUserDetails", JSON.stringify(connectedUser));
          }
          else {
            console.error(response.message,response.status);
          }
        },
        error: (error: Error) => {
          console.error('Current user retrieving failed: ', error);
        }
      });
   }

   retrieveUserDetailsById(userId: string): Observable<any>  {
    return this.http.get<any>(this.getUserByIdApiUrl.concat(userId));
   }

    getCurrentUser(): CurrentUserResponse {
    const currentUser: any = JSON.parse(localStorage.getItem('ConnectedUserDetails')!);
    return new CurrentUserResponse(
      currentUser.userId,
      currentUser.firstname,
      currentUser.lastname,
      currentUser.email,
      currentUser.specialization,
      currentUser.currentProject,
      currentUser.groupName,
      currentUser.role,
      currentUser.authorities
    );
  }
}
