import apiClient from './apiClient'; // Import the new API client

const API_BASE_URL = 'http://localhost:8082/api'; // Your Spring Boot backend address
const ORDER_API_URL = `${API_BASE_URL}/orders`;
const ADMIN_ORDER_API_URL = `${API_BASE_URL}/admin/orders`; // Admin endpoint

// Create order from cart
const createOrder = async (shippingAddress) => {
  const payload = shippingAddress ? { shippingAddress } : {};
  return apiClient.post(`${ORDER_API_URL}/create`, payload);
};

// Create order for a single book
const createSingleBookOrder = async (bookId, quantity, shippingAddress) => {
  const payload = {
    bookId,
    quantity,
    shippingAddress
  };
  return apiClient.post(`${ORDER_API_URL}/create-single`, payload);
};

// Create async order from cart (Kafka-based)
const createOrderAsync = async (shippingAddress) => {
  const payload = shippingAddress ? { shippingAddress } : {};
  console.log('=== FRONTEND ASYNC ORDER REQUEST ===');
  console.log('Creating async cart order with payload:', payload);
  return apiClient.post(`${ORDER_API_URL}/create-async`, payload);
};

// Create async order for a single book (Kafka-based)
const createSingleBookOrderAsync = async (bookId, quantity, shippingAddress) => {
  const payload = {
    bookId,
    quantity,
    shippingAddress
  };
  console.log('=== FRONTEND ASYNC SINGLE ORDER REQUEST ===');
  console.log('Creating async single book order with payload:', payload);
  return apiClient.post(`${ORDER_API_URL}/create-single-async`, payload);
};

// Get user's orders (paginated and filtered)
const getOrders = async (params = {}) => {
  // params: { page, size, sort, startDate, endDate }
  // startDate & endDate should be ISO string format if used (e.g., YYYY-MM-DDTHH:mm:ss)
  const queryParams = new URLSearchParams();
  if (params.page !== undefined) queryParams.append('page', params.page);
  if (params.size !== undefined) queryParams.append('size', params.size);
  if (params.sort) queryParams.append('sort', params.sort); // e.g., "orderDate,desc"
  if (params.startDate) queryParams.append('startDate', params.startDate);
  if (params.endDate) queryParams.append('endDate', params.endDate);
  if (params.bookName) queryParams.append('bookName', params.bookName);

  const queryString = queryParams.toString();
  return apiClient.get(`${ORDER_API_URL}${queryString ? '?' + queryString : ''}`);
};

// Get specific order details (for user)
const getOrderDetails = async (orderId) => {
  return apiClient.get(`${ORDER_API_URL}/${orderId}`);
};

// --- Admin specific methods ---

// Get all orders for admin (paginated and filtered)
const getAllOrdersForAdmin = async (params = {}) => {
  // params: { page, size, sort, startDate, endDate, userId }
  const queryParams = new URLSearchParams();
  if (params.page !== undefined) queryParams.append('page', params.page);
  if (params.size !== undefined) queryParams.append('size', params.size);
  if (params.sort) queryParams.append('sort', params.sort); 
  if (params.startDate) queryParams.append('startDate', params.startDate);
  if (params.endDate) queryParams.append('endDate', params.endDate);
  if (params.userId) queryParams.append('userId', params.userId);
  if (params.bookName) queryParams.append('bookName', params.bookName);

  const queryString = queryParams.toString();
  return apiClient.get(`${ADMIN_ORDER_API_URL}${queryString ? '?' + queryString : ''}`);
};

// Get specific order details for admin
const getAdminOrderDetails = async (orderId) => {
  return apiClient.get(`${ADMIN_ORDER_API_URL}/${orderId}`);
};

const orderService = {
  createOrder,
  createSingleBookOrder,
  createOrderAsync, // Added async cart order
  createSingleBookOrderAsync, // Added async single book order
  getOrders,
  getOrderDetails,
  getAllOrdersForAdmin, // Added admin method
  getAdminOrderDetails, // Added admin method
};

export default orderService; 