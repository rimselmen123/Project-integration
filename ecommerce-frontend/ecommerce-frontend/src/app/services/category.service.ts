import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { defer, of, Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { DemoService } from "./demo.service";
import { Category } from "../models/category";
@Injectable({ providedIn: "root" })
export class CategoryService {
  private url = environment.apiUrl + "/categories";
  constructor(
    private http: HttpClient,
    private demo: DemoService,
  ) {}
  getAll(): Observable<Category[]> {
    return environment.useMock
      ? defer(() => of(this.demo.categories()))
      : this.http.get<Category[]>(this.url);
  }
  save(c: Partial<Category>): Observable<Category> {
    return environment.useMock
      ? defer(() => of(this.demo.saveCategory(c)))
      : c.id
        ? this.http.put<Category>(this.url + "/" + c.id, c)
        : this.http.post<Category>(this.url, c);
  }
  delete(id: number): Observable<void> {
    return environment.useMock
      ? defer(() => {
          this.demo.deleteCategory(id);
          return of(undefined);
        })
      : this.http.delete<void>(this.url + "/" + id);
  }
}
