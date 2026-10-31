import { HttpInterceptorFn } from '@angular/common/http';

export const sessionInterceptor: HttpInterceptorFn = (request, next) => {
  if (!request.url.startsWith('/api/')) return next(request);
  const token=typeof localStorage === 'undefined' ? null : localStorage.getItem('careconnect.token');
  return token ? next(request.clone({ setHeaders: { Authorization: 'Bearer ' + token } })) : next(request);
};
