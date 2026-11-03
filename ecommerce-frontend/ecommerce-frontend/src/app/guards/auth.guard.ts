import { Injectable } from "@angular/core";
import {
  CanActivate,
  Router,
  RouterStateSnapshot,
  ActivatedRouteSnapshot,
} from "@angular/router";
import { AuthService } from "../services/auth.service";
@Injectable({ providedIn: "root" })
export class AuthGuard implements CanActivate {
  constructor(
    private auth: AuthService,
    private router: Router,
  ) {}
  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    return this.auth.user
      ? true
      : this.router.createUrlTree(["/login"], {
          queryParams: { returnUrl: state.url },
        });
  }
}
