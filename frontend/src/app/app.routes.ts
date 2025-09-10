import { Routes } from '@angular/router';
import {LoginPage} from './feat/login/login.page';
import {authGuard} from './core/auth/auth.guard';
import {HomePage} from './feat/home/home.page';


export const routes: Routes = [
  { path: 'auth/login', component: LoginPage },
  {
    path: 'home',
    component: HomePage,
    canActivate: [authGuard]
  },
  { path: '', redirectTo: 'auth/login', pathMatch: 'full' }
]
