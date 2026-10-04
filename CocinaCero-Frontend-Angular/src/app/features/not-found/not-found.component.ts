import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="page-narrow">
      <div class="empty-state card" style="padding:72px 24px">
        <div style="font-size:3rem">🍽️</div>
        <strong style="font-size:1.5rem;margin-top:12px">Página no encontrada</strong>
        <span>La dirección no existe dentro de Cocina Cero.</span>
        <a class="btn btn-primary" routerLink="/" style="margin-top:20px">Volver al inicio</a>
      </div>
    </section>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class NotFoundComponent {}
