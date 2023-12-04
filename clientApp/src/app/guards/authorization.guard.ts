import {inject} from "@angular/core";
import {UserService} from "../services/user.service";
import {Role} from "../models/responses/users/Role";

export function authGuard = () => {
  const userService = inject(UserService);
  return userService.getCurrentUser().role == Role.ADMIN;
}
