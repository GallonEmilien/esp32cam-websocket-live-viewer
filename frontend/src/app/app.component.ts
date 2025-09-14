import { Component } from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {Header} from './shared/components/header/header';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  imports: [
    RouterOutlet,
    Header
  ],
  styleUrl: './app.component.scss'
})
export class AppComponent {
  protected title = 'frontend';
}
