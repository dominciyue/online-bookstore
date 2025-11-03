import apiClient from './apiClient'; // Import the new API client

const API_BASE_URL = 'http://localhost:8082/api'; // Your Spring Boot backend address
const CART_API_URL = `${API_BASE_URL}/cart`;

// Get cart items (User ID is derived from token on backend)
const getCartItems = async () => {
  return apiClient.get(CART_API_URL);
};

// Add item to cart
const addToCart = async (bookId, quantity) => {
  return apiClient.post(`${CART_API_URL}/add`, { bookId, quantity });
};

// Update cart item quantity
const updateCartItemQuantity = async (bookId, quantity) => {
  return apiClient.put(`${CART_API_URL}/update/${bookId}`, { quantity });
};

// Remove item from cart
const removeFromCart = async (bookId) => {
  return apiClient.delete(`${CART_API_URL}/remove/${bookId}`);
};

// Clear cart
const clearCart = async () => {
  return apiClient.delete(`${CART_API_URL}/clear`);
};

const cartService = {
  getCartItems,
  addToCart,
  updateCartItemQuantity,
  removeFromCart,
  clearCart,
};

export default cartService; 