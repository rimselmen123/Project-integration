import { Component, Input, Output, EventEmitter, OnInit } from "@angular/core";
import { AuthService } from "../../services/auth.service";
import { Router, ActivatedRoute } from "@angular/router";
import { finalize } from "rxjs";
import { environment } from "../../../environments/environment";
import { errorMessage } from "../../services/error-message";
@Component({
  selector: "app-login",
  standalone: false,
  templateUrl: "./login.component.html",
})
export class LoginComponent {
  email = "";
  password = "";
  busy = false;
  error = "";
  mock = environment.useMock;
  constructor(
    private auth: AuthService,
    private router: Router,
    private route: ActivatedRoute,
  ) {}
  submit() {
    if (this.busy) return;
    this.busy = true;
    this.error = "";
    this.auth
      .login(this.email, this.password)
      .pipe(finalize(() => (this.busy = false)))
      .subscribe({
        next: () => {
          const url = this.route.snapshot.queryParamMap.get("returnUrl");
          this.router.navigateByUrl(
            url && url.startsWith("/") && !url.startsWith("//") ? url : "/",
          );
        },
        error: (e) => (this.error = errorMessage(e)),
      });
  }
}
