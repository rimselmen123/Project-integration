import { Injectable } from "@angular/core";
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpErrorResponse,
} from "@angular/common/http";
import { catchError, throwError } from "rxjs";
import { AuthService } from "./auth.service";
import { environment } from "../../environments/environment";
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private auth: AuthService) {}
  intercept(request: HttpRequest<unknown>, next: HttpHandler) {
    const api = request.url.startsWith(environment.apiUrl + "/");
    const token = this.auth.token;
    if (api && token && !environment.useMock)
      request = request.clone({
        setHeaders: { Authorization: "Bearer " + token },
      });
    return next.handle(request).pipe(
      catchError((e: HttpErrorResponse) => {
        if (
          api &&
          e.status === 401 &&
          !request.url.endsWith("/login") &&
          !request.url.endsWith("/register")
        )
          this.auth.logout();
        return throwError(() => e);
      }),
    );
  }
}
