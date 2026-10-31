import { Injectable, inject, signal } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { ApiService, SignupRequest } from './api.service';
import { User } from './models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api=inject(ApiService); private readonly router=inject(Router);
  readonly user=signal<User | null>(AuthService.restoreUser()); readonly error=signal(''); readonly busy=signal(false);
  private static restoreUser(): User | null { if (typeof localStorage==='undefined') return null; try { return JSON.parse(localStorage.getItem('careconnect.user') ?? 'null') as User | null; } catch { return null; } }
  login(username:string,password:string) { this.error.set(''); this.busy.set(true); this.api.login(username,password).subscribe({next:response=>this.accept(response),error:error=>{this.busy.set(false); this.error.set(error?.error?.message ?? 'Unable to sign in. Check your credentials.');}}); }
  signup(payload:SignupRequest) { this.error.set(''); this.busy.set(true); this.api.signup(payload).subscribe({next:response=>this.accept(response),error:error=>{this.busy.set(false); this.error.set(error?.error?.message ?? 'Unable to create the account.');}}); }
  private accept(response:{token:string;user:User}) { this.user.set(response.user); localStorage.setItem('careconnect.user',JSON.stringify(response.user)); localStorage.setItem('careconnect.token',response.token); this.busy.set(false); void this.router.navigateByUrl(response.user.role==='PATIENT' ? '/portal' : '/provider/dashboard'); }
  logout() { this.user.set(null); localStorage.removeItem('careconnect.user'); localStorage.removeItem('careconnect.token'); void this.router.navigateByUrl('/login'); }
}

export const providerGuard: CanActivateFn=()=>{const auth=inject(AuthService);const router=inject(Router);return auth.user() && auth.user()?.role!=='PATIENT' ? true : router.createUrlTree(['/login']);};
export const patientGuard: CanActivateFn=()=>{const auth=inject(AuthService);const router=inject(Router);return auth.user()?.role==='PATIENT' ? true : router.createUrlTree(['/login']);};
export const adminGuard: CanActivateFn=()=>{const auth=inject(AuthService);const router=inject(Router);return auth.user()?.role==='ADMIN' ? true : router.createUrlTree(['/login']);};
export const authGuard: CanActivateFn=()=>{const auth=inject(AuthService);const router=inject(Router);return auth.user()?true:router.createUrlTree(['/login']);};
