import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from './core/auth.service';

@Component({
  selector: 'cc-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly loginRoute = signal(false);

  constructor() {
    this.loginRoute.set(this.router.url === '/login');
    this.router.events.pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd)).subscribe((event) => this.loginRoute.set(event.urlAfterRedirects === '/login'));
  }

  get isPatient() { return this.auth.user()?.role === 'PATIENT'; }
  get isProvider() { return this.auth.user()?.role !== 'PATIENT'; }
  get isAdmin() { return this.auth.user()?.role === 'ADMIN'; }
}
