export interface User {
  id: number;
  name: string;
  email: string;
  role: "CLIENT" | "ADMIN";
}
export interface AuthResponse {
  token: string;
  user: User;
}
