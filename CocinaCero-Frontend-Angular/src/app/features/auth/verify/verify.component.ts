import { ChangeDetectionStrategy, Component, OnDestroy, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { apiErrorMessage } from '../../../core/utils/api-error';

@Component({
  selector: 'app-verify',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './verify.component.html',
  styleUrl: './verify.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class VerifyComponent implements OnDestroy {
  readonly loading = signal(false);
  readonly resending = signal(false);
  readonly error = signal('');
  readonly success = signal('');
  readonly seconds = signal(0);
  private timer: ReturnType<typeof setInterval> | null = null;

  readonly form = this.fb.group({
    email: [this.route.snapshot.queryParamMap.get('email') ?? '', [Validators.required, Validators.email]],
    codigo: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]]
  });

  constructor(
    private readonly fb: NonNullableFormBuilder,
    private readonly auth: AuthService,
    private readonly route: ActivatedRoute
  ) {
    if (this.route.snapshot.queryParamMap.get('sent') === '1') this.startCountdown();
  }

  ngOnDestroy(): void {
    if (this.timer) clearInterval(this.timer);
  }

  verify(): void {
    this.error.set('');
    this.success.set('');
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    const raw = this.form.getRawValue();
    this.auth.verify({ email: raw.email.trim(), codigo: raw.codigo.trim() })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (response) => this.success.set(response.mensaje),
        error: (err) => this.error.set(apiErrorMessage(err, 'No se pudo verificar el código.'))
      });
  }

  resend(): void {
    if (this.seconds() > 0 || this.form.controls.email.invalid) {
      this.form.controls.email.markAsTouched();
      return;
    }
    this.error.set('');
    this.success.set('');
    this.resending.set(true);
    this.auth.resend(this.form.controls.email.value.trim())
      .pipe(finalize(() => this.resending.set(false)))
      .subscribe({
        next: (response) => {
          this.success.set(response.mensaje + ' Revisa tu bandeja de entrada.');
          this.startCountdown();
        },
        error: (err) => this.error.set(apiErrorMessage(err, 'No se pudo reenviar el código.'))
      });
  }

  private startCountdown(): void {
    this.seconds.set(60);
    if (this.timer) clearInterval(this.timer);
    this.timer = setInterval(() => {
      const next = this.seconds() - 1;
      this.seconds.set(Math.max(0, next));
      if (next <= 0 && this.timer) {
        clearInterval(this.timer);
        this.timer = null;
      }
    }, 1000);
  }
}
