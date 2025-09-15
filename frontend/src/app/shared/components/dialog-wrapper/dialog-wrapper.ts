import {Component, output} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { TranslocoDirective } from '@ngneat/transloco';

@Component({
  selector: 'app-dialog-wrapper',
  standalone: true,
  templateUrl: './dialog-wrapper.html',
  styleUrl: './dialog-wrapper.scss',
  imports: [MatButtonModule, MatIconModule, TranslocoDirective],
})
export class CameraDialogWrapper {
  close = output()
}
