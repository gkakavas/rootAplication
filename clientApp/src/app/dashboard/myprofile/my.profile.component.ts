import {Component, OnInit} from "@angular/core";
import {RouterOutlet} from "@angular/router";
import {ReactiveFormsModule} from "@angular/forms";
import {CommonModule} from "@angular/common";
import {ServerErrorResponse} from "../../models/error/server.error.response";
import {AdminUserResponse} from "../../models/responses/users/admin.user.response";
import {OtherUserResponse} from "../../models/responses/users/other.user.response";
import {UserService} from "../../services/user.service";
import {map} from "rxjs/internal/operators/map";
import {Role} from "../../models/responses/users/Role";
import {catchError} from "rxjs/internal/operators/catchError";
import {throwError} from "rxjs/internal/observable/throwError";


@Component({
  standalone: true,
  selector: 'my-profile',
  templateUrl: './my.profile.component.html',
  styleUrls: ['./my.profile.component.css'],
  imports: [RouterOutlet, ReactiveFormsModule,CommonModule]
})
export class MyProfile implements OnInit{

  userDetails : any = {};
  constructor(private userService: UserService) {
  }

  ngOnInit() {
    this.userDetails = this.fetchUserDetails();
    console.log(this.userDetails);
  }

  fetchUserDetails(): OtherUserResponse | AdminUserResponse | any{
    const currentUser = this.userService.getCurrentUser();
    this.userService.retrieveUserDetailsById(currentUser.userId).pipe(
      map((response)=> {
        if(currentUser.role === Role.ADMIN){
          return new AdminUserResponse(
            response.userId, response.firstname, response.lastname,
            response.email, response.specialization, response.currentProject,
            response.groupName, response.createdBy, response.registerDate,
            response.lastLogin, response.role
          )
        }
        else if(currentUser.role === Role.USER || Role.MANAGER || Role.HR){
          return new OtherUserResponse(
            response.userId, response.firstname, response.lastname,
            response.email, response.specialization, response.currentProject,
            response.groupName
          )
        }
        else{
            return new ServerErrorResponse(response.message, response.status);
        }
      }),
      catchError((error: any) => {
        console.error('Error during mapping user response: ', error);
        return throwError(() => new Error(error));
      })
    ).subscribe({
      next: (response) => {
          return response;
      },
      error: (error: Error) => {
        console.error('User info fetching failed:', error);
      }
    });
  }
}
