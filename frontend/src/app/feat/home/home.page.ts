import {
  Component,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import {Dashboard} from '../../shared/components/dashboard/dashboard';

@Component({
  imports: [CommonModule, MatProgressSpinnerModule, Dashboard],
  templateUrl: './home.page.html',
  standalone: true,
  styleUrls: ['./home.page.scss']
})
export class HomePage {}
