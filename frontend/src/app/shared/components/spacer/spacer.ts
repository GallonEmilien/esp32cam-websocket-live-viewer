import { Component, input } from '@angular/core';
import { size } from '@ngneat/transloco';

@Component({
  selector: 'app-spacer',
  standalone: true,
  templateUrl: './spacer.html',
  imports: [],
  styleUrl: './spacer.scss',
})
export class Spacer {
  pxSpace = input<number>(16);
  protected readonly size = size;
}
