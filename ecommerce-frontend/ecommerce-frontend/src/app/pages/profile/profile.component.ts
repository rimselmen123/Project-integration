import { Component, Input, Output, EventEmitter, OnInit } from "@angular/core";
import { User } from "../../models/user";
import { AuthService } from "../../services/auth.service";
import { finalize } from "rxjs";
import { errorMessage } from "../../services/error-message";
@Component({
  selector: "app-profile",
  standalone: false,
  templateUrl: "./profile.component.html",
})
export class ProfileComponent {
  user: User | null = null;
  loading = true;
  error = "";
  constructor(private auth: AuthService) {}
  ngOnInit() {
    this.auth
      .me()
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (u) => (this.user = u),
        error: (e) => (this.error = errorMessage(e)),
      });
  }
}
