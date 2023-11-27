import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import {CurrentUserResponse, getCurrentUser} from '../models/responses/current.user.response';
import {map} from "rxjs/internal/operators/map";
import {ServerErrorResponse} from "../models/error/server.error.response";
import {catchError} from "rxjs/internal/operators/catchError";
import {throwError} from "rxjs/internal/observable/throwError";
import {parseJson} from "@angular/cli/src/utilities/json-file";

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private getCurrentUserApiUrl = "http://localhost:8080/user/currentUser";

  constructor(private http: HttpClient) {}


  retrieveConnectedUser(): void {
    const token = localStorage.getItem("AuthToken")!;

    this.http.get<any>(this.getCurrentUserApiUrl, {
      headers: {
        'Authorization' : `${token}`,
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
            response.role
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
    )
      .subscribe({
        next: (response) => {
          if(response instanceof CurrentUserResponse){
            localStorage.setItem("ConnectedUserDetails", response.toJSON());
            console.log(getCurrentUser().toJSON());
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
}
