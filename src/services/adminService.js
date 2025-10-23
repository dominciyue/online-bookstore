import apiClient from './apiClient';

const API_ADMIN_URL = 'http://localhost:8080/api/admin';

// Get all users (for admin)
const getAllUsers = async () => {
  return apiClient.get(`${API_ADMIN_URL}/users`);
};

// Disable a user (for admin)
const disableUser = async (userId) => {
  return apiClient.put(`${API_ADMIN_URL}/users/${userId}/disable`);
};

// Enable a user (for admin)
const enableUser = async (userId) => {
  return apiClient.put(`${API_ADMIN_URL}/users/${userId}/enable`);
};

const adminService = {
  getAllUsers,
  disableUser,
  enableUser,
};

export default adminService; 