import {HttpInterceptorFn} from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    // Get the authentication token from local storage
    let token = localStorage.getItem('AuthToken');
    // Exclude http://localhost:8080/auth/authenticate request from interceptor
    if (req.url === 'http://localhost:8080/auth/authenticate') {
      return next(req);
    }
    // Clone the request and add the authorization header
    if (token) {
      let request = req.clone({
        setHeaders: {
          "Authorization": token,
        }
      });
      // Pass the cloned request with the authorization header to the next handler
      return next(request);
    }
    // If there's no token, pass the original request as is
    return next(req);
}
