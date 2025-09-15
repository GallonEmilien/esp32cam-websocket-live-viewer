import { Component, inject, signal } from '@angular/core';
import { CreateCameraResponse } from '../../../../../core/services/http/camera.http.service';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogRef } from '@angular/material/dialog';
import { TranslocoDirective } from '@ngneat/transloco';
import {CameraService} from '../../../../../core/services/camera.service';
import {DialogService} from '../../../../../core/services/dialog.service';
import {FormsModule} from '@angular/forms';
import {CameraDialogWrapper} from '../../../../../shared/components/dialog-wrapper/dialog-wrapper';
import {SimpleButton} from '../../../../../shared/components/simple-button/simple-button';

@Component({
  selector: 'app-camera-creation-popin',
  standalone: true,
  templateUrl: './camera-creation-popin.html',
  styleUrl: './camera-creation-popin.scss',
  imports: [
    MatFormFieldModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    TranslocoDirective,
    FormsModule,
    CameraDialogWrapper,
    SimpleButton,
  ],
})
export class CameraCreationPopin {
  private cameraService = inject(CameraService);
  private dialogService = inject(DialogService);
  matDialogRef = inject(MatDialogRef<CameraCreationPopin>);

  cameraName = signal<string | undefined>(undefined);

  closeModal() {
    this.matDialogRef.close();
  }

  submit() {
    this.cameraService.createCamera().subscribe((res) => {
      this.closeModal()
      this.dialogService.openCameraResponse(res);
    });
  }
}
