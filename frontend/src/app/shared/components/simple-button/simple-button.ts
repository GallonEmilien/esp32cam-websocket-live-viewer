import { Component, input, output } from '@angular/core';
import { MatIcon } from '@angular/material/icon';

export type ButtonType = 'primary' | 'danger' | 'info'

@Component({
  selector: 'app-simple-button',
  standalone: true,
  templateUrl: './simple-button.html',
  imports: [MatIcon],
  styleUrl: './simple-button.scss',
})
export class SimpleButton {
  title = input.required<string>();
  icon = input<string | undefined>(undefined);
  link = input<string | undefined>(undefined);
  disabled = input<boolean>(false);
  buttonType = input<ButtonType>('primary');

  action = output();
}
