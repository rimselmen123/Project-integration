import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { BehaviorSubject, defer, Observable, of, tap } from "rxjs";
import { environment } from "../../environments/environment";
import { User, AuthResponse } from "../models/user";
import { DemoService } from "./demo.service";
@Injectable({ providedIn: "root" })
export class AuthService {
  private key = environment.useMock ? "maison-demo-auth" : "maison-api-auth";
  private session: AuthResponse | null = null;
  readonly user$ = new BehaviorSubject<User | null>(null);
  constructor(
    private http: HttpClient,
    private demo: DemoService,
  ) {
    try {
      this.session = JSON.parse(localStorage.getItem(this.key) || "null");
      if (this.session && (!this.session.token || !this.session.user))
        this.session = null;
    } catch {
      this.session = null;
    }
    this.user$.next(this.session?.user ?? null);
  }
  get user() {
    return this.user$.value;
  }
  get token() {
    return this.session?.token ?? null;
  }
  get userId() {
    if (!this.user) throw new Error("Connectez-vous pour continuer.");
    return this.user.id;
  }
  private save(data: AuthResponse) {
    this.session = data;
    localStorage.setItem(this.key, JSON.stringify(data));
    this.user$.next(data.user);
  }
  login(email: string, password: string): Observable<AuthResponse> {
    return (
      environment.useMock
        ? defer(() => of(this.demo.login(email, password)))
        : this.http.post<AuthResponse>(environment.apiUrl + "/auth/login", {
            email,
            password,
          })
    ).pipe(tap((data) => this.save(data)));
  }
  register(
    name: string,
    email: string,
    password: string,
  ): Observable<AuthResponse> {
    return (
      environment.useMock
        ? defer(() => of(this.demo.register(name, email, password)))
        : this.http.post<AuthResponse>(environment.apiUrl + "/auth/register", {
            name,
            email,
            password,
          })
    ).pipe(tap((data) => this.save(data)));
  }
  me(): Observable<User> {
    return (
      environment.useMock
        ? defer(() => of(this.demo.me(this.userId)))
        : this.http.get<User>(environment.apiUrl + "/auth/me")
    ).pipe(
      tap((user) => {
        if (this.session) this.save({ ...this.session, user });
      }),
    );
  }
  logout() {
    this.session = null;
    localStorage.removeItem(this.key);
    this.user$.next(null);
  }
}
