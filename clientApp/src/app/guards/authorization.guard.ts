import {inject} from "@angular/core";
import {UserService} from "../services/user.service";
import {CanActivateFn} from "@angular/router";
import {CurrentUserResponse} from "../models/responses/users/current.user.response";

export const authGuard: CanActivateFn = (route) => {
    const userService: UserService = inject(UserService);
    const currentUser: CurrentUserResponse = userService.getCurrentUser();
    if (currentUser.authorities.includes(route.data['requiredAuthority'])) {
        return true;
    } else {
        return false;
    }
}
