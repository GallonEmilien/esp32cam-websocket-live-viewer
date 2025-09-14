import { Component, inject, model, signal } from '@angular/core';
import { MatListOption, MatSelectionList } from '@angular/material/list';
import { Camera } from '../../../../../core/model/Camera';
import { CameraService } from '../../../../../core/services/camera.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-pick-camera',
  standalone: true,
  templateUrl: './pick-camera.html',
  styleUrl: './pick-camera.scss',
  imports: [MatSelectionList, MatListOption, FormsModule],
})
export class PickCamera {
  selectedCamera = signal<Camera[]>([]);
  modelCamera = model<Camera | undefined>(undefined);
  private cameraService = inject(CameraService);

  cameras = this.cameraService.camerasSignal;

  change(cameras: Camera[]) {
    this.modelCamera.set(cameras?.[0] || undefined);
    this.selectedCamera.set(cameras);
  }
}
