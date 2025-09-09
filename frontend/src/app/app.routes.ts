import { Routes } from '@angular/router';
import {LoginComponent} from './feat/login/login.component';
import {CameraComponent} from './feat/camera/camera.component';
import {authGuard} from './core/auth/auth.guard';


export const routes: Routes = [
  { path: 'auth/login', component: LoginComponent },
  {
    path: 'camera',
    component: CameraComponent,
    canActivate: [authGuard]
  },
  { path: '', redirectTo: 'auth/login', pathMatch: 'full' }
]
