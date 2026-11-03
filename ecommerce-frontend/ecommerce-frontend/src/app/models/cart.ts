export interface CartItem {
  id: number;
  productId: number;
  productName: string;
  imageUrl: string;
  price: number;
  quantity: number;
  subtotal: number;
}
export interface Cart {
  id: number;
  items: CartItem[];
  total: number;
}
