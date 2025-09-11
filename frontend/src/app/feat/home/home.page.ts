import {
  Component,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import {CameraComponent} from '../camera/camera.component';

@Component({
  imports: [CommonModule, MatProgressSpinnerModule, CameraComponent],
  templateUrl: './home.page.html',
  styleUrls: ['./home.page.scss']
})
export class HomePage {
}
