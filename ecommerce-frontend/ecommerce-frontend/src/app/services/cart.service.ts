import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { defer, of, Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { DemoService } from "./demo.service";
import { Cart } from "../models/cart";
import { AuthService } from "./auth.service";
@Injectable({ providedIn: "root" })
export class CartService {
  private url = environment.apiUrl + "/cart";
  constructor(
    private http: HttpClient,
    private demo: DemoService,
    private auth: AuthService,
  ) {}
  get(): Observable<Cart> {
    return environment.useMock
      ? defer(() => of(this.demo.cart(this.auth.userId)))
      : this.http.get<Cart>(this.url);
  }
  add(productId: number, quantity = 1): Observable<Cart> {
    return environment.useMock
      ? defer(() => of(this.demo.add(this.auth.userId, productId, quantity)))
      : this.http.post<Cart>(this.url + "/items", { productId, quantity });
  }
  update(id: number, quantity: number): Observable<Cart> {
    return environment.useMock
      ? defer(() => of(this.demo.update(this.auth.userId, id, quantity)))
      : this.http.put<Cart>(this.url + "/items/" + id, { quantity });
  }
  remove(id: number): Observable<void> {
    return environment.useMock
      ? defer(() => {
          this.demo.remove(this.auth.userId, id);
          return of(undefined);
        })
      : this.http.delete<void>(this.url + "/items/" + id);
  }
  clear(): Observable<void> {
    return environment.useMock
      ? defer(() => {
          this.demo.clear(this.auth.userId);
          return of(undefined);
        })
      : this.http.delete<void>(this.url);
  }
}
