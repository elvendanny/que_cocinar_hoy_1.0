import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessionService } from '../services/session.service';

export const adminGuard: CanActivateFn = () => {
  const session = inject(SessionService);
  const router = inject(Router);
  if (!session.isLoggedIn()) return router.createUrlTree(['/login']);
  return session.isAdmin() ? true : router.createUrlTree(['/recetas']);
};
