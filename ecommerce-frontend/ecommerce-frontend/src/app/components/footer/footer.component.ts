import { Component, Input, Output, EventEmitter, OnInit } from "@angular/core";

@Component({
  selector: "app-footer",
  standalone: false,
  templateUrl: "./footer.component.html",
})
export class FooterComponent {
  year = new Date().getFullYear();
}
