import { Component } from '@angular/core';
import {CameraSettings} from '../../../feat/camera/settings/camera-settings';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
  imports: [
    CameraSettings
  ],
  standalone: true
})
export class Dashboard {}
