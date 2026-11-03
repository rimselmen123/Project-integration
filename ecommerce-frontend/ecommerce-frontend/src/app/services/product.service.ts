import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { defer, of, Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { DemoService } from "./demo.service";
import { Product } from "../models/product";
@Injectable({ providedIn: "root" })
export class ProductService {
  private url = environment.apiUrl + "/products";
  constructor(
    private http: HttpClient,
    private demo: DemoService,
  ) {}
  getAll(admin = false): Observable<Product[]> {
    return environment.useMock
      ? defer(() => of(this.demo.products(admin)))
      : this.http.get<Product[]>(this.url + (admin ? "/admin" : ""));
  }
  getById(id: number): Observable<Product> {
    return environment.useMock
      ? defer(() => of(this.demo.product(id)))
      : this.http.get<Product>(this.url + "/" + id);
  }
  save(p: Partial<Product>): Observable<Product> {
    return environment.useMock
      ? defer(() => of(this.demo.saveProduct(p)))
      : p.id
        ? this.http.put<Product>(this.url + "/" + p.id, p)
        : this.http.post<Product>(this.url, p);
  }
  delete(id: number): Observable<void> {
    return environment.useMock
      ? defer(() => {
          this.demo.deleteProduct(id);
          return of(undefined);
        })
      : this.http.delete<void>(this.url + "/" + id);
  }
}
