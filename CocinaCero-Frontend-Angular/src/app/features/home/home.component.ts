import { ChangeDetectionStrategy, Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { CocinaService } from '../../core/services/cocina.service';
import { SessionService } from '../../core/services/session.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HomeComponent implements OnInit {
  readonly authOnline = signal<boolean | null>(null);
  readonly cocinaOnline = signal<boolean | null>(null);

  constructor(
    private readonly auth: AuthService,
    private readonly cocina: CocinaService,
    public readonly session: SessionService
  ) {}

  ngOnInit(): void {
    this.auth.health().subscribe({ next: () => this.authOnline.set(true), error: () => this.authOnline.set(false) });
    this.cocina.health().subscribe({ next: () => this.cocinaOnline.set(true), error: () => this.cocinaOnline.set(false) });
  }
}
