export type OrderStatus = "EN_ATTENTE" | "CONFIRMEE" | "EXPEDIEE" | "LIVREE";
export interface OrderItem {
  id: number;
  productId: number;
  productName: string;
  price: number;
  quantity: number;
  subtotal: number;
}
export interface Order {
  id: number;
  userId: number;
  customerName: string;
  createdAt: string;
  status: OrderStatus;
  shippingAddress: string;
  items: OrderItem[];
  total: number;
}
