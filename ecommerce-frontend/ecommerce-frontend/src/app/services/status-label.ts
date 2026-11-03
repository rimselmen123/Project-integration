import { OrderStatus } from "../models/order";
export const statuses: OrderStatus[] = [
  "EN_ATTENTE",
  "CONFIRMEE",
  "EXPEDIEE",
  "LIVREE",
];
export function statusLabel(s: OrderStatus): string {
  return {
    EN_ATTENTE: "En attente",
    CONFIRMEE: "Confirmée",
    EXPEDIEE: "Expédiée",
    LIVREE: "Livrée",
  }[s];
}
