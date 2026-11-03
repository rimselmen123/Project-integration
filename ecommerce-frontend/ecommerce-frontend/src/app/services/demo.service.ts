import { Injectable } from "@angular/core";
import { DemoStore } from "./demo-store";
@Injectable({ providedIn: "root" })
export class DemoService extends DemoStore {
  constructor() {
    super(localStorage);
  }
}
