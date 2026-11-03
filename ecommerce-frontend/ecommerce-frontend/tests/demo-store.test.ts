import { test } from "node:test";
import assert from "node:assert/strict";
import { DemoStore } from "../src/app/services/demo-store";
const make = () => new DemoStore();
test("login rejects an incorrect password and registration remains CLIENT", () => {
  const s = make();
  assert.throws(() => s.login("client@demo.com", "wrong"));
  const auth = s.register("Nouveau", "new@example.com", "Strong123!");
  assert.equal(auth.user.role, "CLIENT");
  assert.throws(() => s.register("Nouveau", "new@example.com", "Strong123!"));
});
test("cart quantities and stock are checked; checkout keeps a price snapshot", () => {
  const s = make();
  const u = s.login("client@demo.com", "Demo12345!").user;
  assert.throws(() => s.add(u.id, 1, 0));
  assert.throws(() => s.add(u.id, 1, 9999));
  assert.equal(s.add(u.id, 1, 2).total, 79.8);
  const order = s.checkout(u.id, "12 rue de l Universite, Tunis");
  assert.equal(order.total, 79.8);
  assert.equal(s.cart(u.id).items.length, 0);
  assert.equal(s.products()[0].stock, 28);
  assert.throws(() => s.checkout(u.id, "12 rue de l Universite, Tunis"));
  const p = s.products()[0];
  s.saveProduct({ ...p, price: 100 });
  assert.equal(s.orders(u.id)[0].items[0].price, 39.9);
});
test("carts and orders are isolated between demo users", () => {
  const s = make();
  const a = s.login("client@demo.com", "Demo12345!").user;
  const b = s.register("Autre", "b@example.com", "Strong123!").user;
  s.add(a.id, 1, 1);
  assert.equal(s.cart(b.id).total, 0);
  s.checkout(a.id, "12 rue de l Universite, Tunis");
  assert.equal(s.orders(b.id).length, 0);
});
test("category in use cannot be deleted; inactive products cannot be ordered", () => {
  const s = make();
  const u = s.login("client@demo.com", "Demo12345!").user;
  assert.throws(() => s.deleteCategory(1));
  s.add(u.id, 1, 1);
  s.deleteProduct(1);
  assert.throws(() => s.checkout(u.id, "12 rue de l Universite, Tunis"));
});
