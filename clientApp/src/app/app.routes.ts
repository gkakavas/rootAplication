import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { DashboardComponent } from './user/dashboard.component';

export const routes: Routes = [
   {path: '', redirectTo: '/login', pathMatch: 'full' },
   {path: 'login', component:LoginComponent},
   {path: 'user',component:DashboardComponent},
   {path: 'manager',component:DashboardComponent},
   {path: 'hr',component:DashboardComponent},
   {path: 'admin',component:DashboardComponent},
];
