import { HttpErrorResponse } from '@angular/common/http';
import { ApiErrorBody } from '../models/api.models';

export function apiErrorMessage(error: unknown, fallback = 'No se pudo completar la operación.'): string {
  if (error instanceof HttpErrorResponse) {
    const body = error.error as ApiErrorBody | string | null;
    if (typeof body === 'string' && body.trim()) return body;
    if (body && typeof body === 'object' && body.error) return body.error;
    if (error.status === 0) return 'No se pudo conectar con el backend. Comprueba que ambas API estén iniciadas.';
    if (error.status === 401) return 'La sesión no es válida o ha vencido.';
    if (error.status === 403) return 'No tienes permiso para realizar esta operación.';
    if (error.status === 404) return 'El recurso solicitado ya no existe.';
    if (error.status === 409) return 'La operación entra en conflicto con datos existentes.';
    if (error.status === 413) return 'El archivo supera el tamaño permitido.';
    if (error.status === 429) return 'Has realizado demasiados intentos. Espera un momento.';
    if (error.status === 502) return 'No se pudo enviar el correo de verificación.';
    if (error.status === 503) return 'El servicio de autenticación no está disponible.';
  }
  return fallback;
}
