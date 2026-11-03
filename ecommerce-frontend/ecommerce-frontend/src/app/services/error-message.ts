export function errorMessage(error: any): string {
  if (error?.status === 0) {
    return "L’API est inaccessible. Vérifiez que le backend est lancé et que apiUrl et CORS sont configurés.";
  }
  return (
    error?.error?.message ||
    error?.message ||
    "Une erreur est survenue. Réessayez."
  );
}
