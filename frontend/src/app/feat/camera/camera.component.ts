import {
  Component,
  inject,
  OnDestroy,
  AfterViewInit,
  ViewChild,
  ElementRef,
  computed,
  signal, input, effect
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { CameraService } from '../../core/services/camera.service';
import { AuthService } from '../../core/auth/auth.service';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import {TranslocoDirective} from '@ngneat/transloco';
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from '@angular/material/card';

@Component({
  selector: 'app-camera',
  standalone: true,
  imports: [CommonModule, MatProgressSpinnerModule, TranslocoDirective, MatCardContent, MatCardTitle, MatCardHeader, MatCard],
  templateUrl: './camera.component.html',
  styleUrls: ['./camera.component.scss']
})
export class CameraComponent implements AfterViewInit, OnDestroy {
  private cameraService = inject(CameraService);
  private authService = inject(AuthService);

  cameraId = input<string | undefined>(undefined);

  latestFrame = computed(() => {
    const cameraId = this.cameraId();
    if (!cameraId) return undefined;
    return this.cameraService.frames.get(cameraId)?.();
  })

  isActive = signal(false);

  lastPing = computed(() => {
    if(this.latestFrame()) {
      return Date.now();
    }
    return 0;
  });

  @ViewChild('canvas') canvasRef!: ElementRef<HTMLCanvasElement>;
  private ctx!: CanvasRenderingContext2D;

  private animationId!: number;

  constructor() {
    effect(() => {
      const token = this.authService.getToken();
      const cameraId = this.cameraId();
      if (token && cameraId) {
        console.log("NOUVELLE")
        this.cameraService.connect(token, cameraId);
      }
    });
  }

  ngAfterViewInit() {
    this.ctx = this.canvasRef.nativeElement.getContext('2d')!;
    this.drawLoop();
  }

  private drawLoop() {
    const frame = this.latestFrame();
    this.isActive.set(Date.now() - this.lastPing() < 2000)
    if (frame && this.ctx) {
      const canvas = this.canvasRef.nativeElement;
      if (canvas.width !== frame.width) canvas.width = frame.width;
      if (canvas.height !== frame.height) canvas.height = frame.height;
      this.ctx.drawImage(frame, 0, 0, frame.width, frame.height);
    }
    this.animationId = requestAnimationFrame(() => this.drawLoop());
  }


  ngOnDestroy() {
    cancelAnimationFrame(this.animationId);
    this.cameraService.disconnect('0');
  }
}
