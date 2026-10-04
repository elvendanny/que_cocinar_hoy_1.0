import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { provideZoneChangeDetection } from '@angular/core';
import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { apiInterceptor } from './app/core/interceptors/api.interceptor';

bootstrapApplication(AppComponent, {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([apiInterceptor]))
  ]
}).catch((error) => console.error(error));
