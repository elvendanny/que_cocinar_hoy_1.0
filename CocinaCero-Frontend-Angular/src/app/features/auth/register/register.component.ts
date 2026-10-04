import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { apiErrorMessage } from '../../../core/utils/api-error';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RegisterComponent {
  readonly loading = signal(false);
  readonly error = signal('');
  readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(80)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(254)]],
    password: ['', [
      Validators.required,
      Validators.minLength(10),
      Validators.maxLength(72),
      Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).*$/)
    ]]
  });

  constructor(
    private readonly fb: NonNullableFormBuilder,
    private readonly auth: AuthService,
    private readonly router: Router
  ) {}

  submit(): void {
    this.error.set('');
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    const raw = this.form.getRawValue();
    if (new TextEncoder().encode(raw.password).length > 72) {
      this.loading.set(false);
      this.error.set('La contraseña supera el máximo de 72 bytes permitido por el backend.');
      return;
    }
    this.auth.register({ nombre: raw.nombre.trim(), email: raw.email.trim(), password: raw.password })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => void this.router.navigate(['/verificar'], { queryParams: { email: raw.email.trim(), sent: '1' } }),
        error: (err) => this.error.set(apiErrorMessage(err, 'No se pudo crear la cuenta.'))
      });
  }
}
