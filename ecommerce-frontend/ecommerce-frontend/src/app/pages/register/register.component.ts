import { Component, Input, Output, EventEmitter, OnInit } from "@angular/core";
import { AuthService } from "../../services/auth.service";
import { Router, ActivatedRoute } from "@angular/router";
import { finalize } from "rxjs";
import { environment } from "../../../environments/environment";
import { errorMessage } from "../../services/error-message";
@Component({
  selector: "app-register",
  standalone: false,
  templateUrl: "./register.component.html",
})
export class RegisterComponent {
  name = "";
  email = "";
  password = "";
  confirmation = "";
  busy = false;
  error = "";
  constructor(
    private auth: AuthService,
    private router: Router,
  ) {}
  submit() {
    if (this.busy || this.password !== this.confirmation) return;
    this.busy = true;
    this.error = "";
    this.auth
      .register(this.name, this.email, this.password)
      .pipe(finalize(() => (this.busy = false)))
      .subscribe({
        next: () => this.router.navigate(["/"]),
        error: (e) => (this.error = errorMessage(e)),
      });
  }
}
