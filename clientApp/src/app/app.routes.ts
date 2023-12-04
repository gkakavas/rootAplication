import { Routes } from '@angular/router';

import { LoginComponent } from './login/login.component';

import {Dashboard} from "./dashboard/dashboard.component";
import {MyProfile} from "./dashboard/myprofile/my.profile.component";
import {Authority} from "./models/responses/users/authority";
import {authGuard} from "./guards/authorization.guard";


export const routes: Routes = [
   {path: '', redirectTo: '/login', pathMatch: 'full'},
   {path: 'login', component:LoginComponent},
   {
     path: 'dashboard',
     component: Dashboard,
     children:[
       {
         path: 'my-profile',
         component:MyProfile,
         data:{
            requiredAuthority: Authority.USER_CREATE
         },
         canActivate:[authGuard],
       },
       /*{
         path: 'profile',
       },
       {
         path: 'leaves',
       },
       {
         path: 'files',
       },
       {
         path: 'group',
       },*/

     ]
   },
];
