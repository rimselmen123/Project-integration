import { Component } from "@angular/core";
@Component({
  selector: "app-root",
  standalone: false,
  template:
    '<a class="skip-link" href="#main">Aller au contenu</a><app-navbar></app-navbar><main id="main"><router-outlet></router-outlet></main><app-footer></app-footer>',
})
export class AppComponent {}
