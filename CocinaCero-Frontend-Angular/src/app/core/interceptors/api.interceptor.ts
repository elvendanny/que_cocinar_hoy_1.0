import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SessionService } from '../services/session.service';

export const apiInterceptor: HttpInterceptorFn = (request, next) => {
  const session = inject(SessionService);
  const router = inject(Router);
  const token = session.token();
  const isApi = request.url.startsWith(environment.authUrl) || request.url.startsWith(environment.cocinaUrl);

  const outgoing = token && isApi
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : request;

  return next(outgoing).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401 && token) {
        session.clear();
        void router.navigate(['/login'], { queryParams: { sesion: 'expirada' } });
      }
      return throwError(() => error);
    })
  );
};
