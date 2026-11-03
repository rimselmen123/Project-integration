import { Component, Input, Output, EventEmitter, OnInit } from "@angular/core";
import { Product } from "../../models/product";
@Component({
  selector: "app-product-card",
  standalone: false,
  templateUrl: "./product-card.component.html",
})
export class ProductCardComponent {
  @Input({ required: true }) product!: Product;
  @Input() busy = false;
  @Output() add = new EventEmitter<Product>();
  fallback(event: Event) {
    const image = event.target as HTMLImageElement;
    image.onerror = null;
    image.src = "/assets/placeholder.svg";
  }
}
