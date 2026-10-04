import { ChangeDetectionStrategy, Component, Input, OnChanges, OnDestroy, signal } from '@angular/core';
import { CocinaService } from '../../../core/services/cocina.service';

@Component({
  selector: 'app-protected-image',
  standalone: true,
  templateUrl: './protected-image.component.html',
  styleUrl: './protected-image.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProtectedImageComponent implements OnChanges, OnDestroy {
  @Input({ required: true }) path = '';
  @Input() alt = 'Imagen de receta';

  readonly src = signal<string | null>(null);
  readonly loading = signal(false);
  readonly failed = signal(false);
  private objectUrl: string | null = null;

  constructor(private readonly cocina: CocinaService) {}

  ngOnChanges(): void {
    this.load();
  }

  ngOnDestroy(): void {
    this.revoke();
  }

  private load(): void {
    this.revoke();
    this.src.set(null);
    this.failed.set(false);
    if (!this.path) {
      this.failed.set(true);
      return;
    }
    this.loading.set(true);
    this.cocina.imageBlob(this.path).subscribe({
      next: (blob) => {
        this.objectUrl = URL.createObjectURL(blob);
        this.src.set(this.objectUrl);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.failed.set(true);
      }
    });
  }

  private revoke(): void {
    if (this.objectUrl) {
      URL.revokeObjectURL(this.objectUrl);
      this.objectUrl = null;
    }
  }
}
