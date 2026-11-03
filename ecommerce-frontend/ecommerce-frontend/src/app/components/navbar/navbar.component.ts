import { Component, Input, Output, EventEmitter, OnInit } from "@angular/core";
import { AuthService } from "../../services/auth.service";
import { Router } from "@angular/router";
import { environment } from "../../../environments/environment";
@Component({
  selector: "app-navbar",
  standalone: false,
  templateUrl: "./navbar.component.html",
})
export class NavbarComponent {
  open = false;
  mock = environment.useMock;
  constructor(
    public auth: AuthService,
    private router: Router,
  ) {}
  logout() {
    this.auth.logout();
    this.router.navigate(["/"]);
  }
}
