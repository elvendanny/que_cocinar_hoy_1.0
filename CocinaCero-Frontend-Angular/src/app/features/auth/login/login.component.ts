import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { SessionService } from '../../../core/services/session.service';
import { apiErrorMessage } from '../../../core/utils/api-error';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoginComponent {
  readonly loading = signal(false);
  readonly error = signal('');
  readonly info = signal('');
  readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.maxLength(72)]]
  });

  constructor(
    private readonly fb: NonNullableFormBuilder,
    private readonly auth: AuthService,
    private readonly session: SessionService,
    private readonly router: Router,
    private readonly route: ActivatedRoute
  ) {
    if (this.route.snapshot.queryParamMap.get('sesion') === 'expirada') {
      this.info.set('Tu sesión terminó o fue revocada. Inicia sesión nuevamente.');
    }
  }

  submit(): void {
    this.error.set('');
    this.info.set('');
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    const { email, password } = this.form.getRawValue();
    this.auth.login(email.trim(), password).pipe(finalize(() => this.loading.set(false))).subscribe({
      next: (response) => {
        this.session.setLogin(response);
        const requested = this.route.snapshot.queryParamMap.get('returnUrl');
        const destination = requested?.startsWith('/') && !requested.startsWith('//') ? requested : '/recetas';
        void this.router.navigateByUrl(destination);
      },
      error: (err) => this.error.set(apiErrorMessage(err, 'No se pudo iniciar sesión.'))
    });
  }
}
