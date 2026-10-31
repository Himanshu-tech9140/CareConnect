import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { sessionInterceptor } from './app/core/session.interceptor';

bootstrapApplication(AppComponent, {
  providers: [provideHttpClient(withFetch(), withInterceptors([sessionInterceptor])), provideRouter(routes, withComponentInputBinding())]
}).catch((error: unknown) => console.error('CareConnect bootstrap failed', error));
