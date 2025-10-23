import apiClient from './apiClient';

const API_BASE_URL = 'http://localhost:8080/api/users';

// Get current user's full details
const getCurrentUserDetails = async () => {
  return apiClient.get(`${API_BASE_URL}/me`);
};

// Update current user's profile (phone, address)
const updateProfile = async (profileData) => {
  // profileData should be an object like { phone: "123", address: "456 Main St" }
  return apiClient.put(`${API_BASE_URL}/profile`, profileData);
};

// Upload avatar (formData should contain the image file)
const uploadAvatar = async (formData) => {
  // apiClient will handle Content-Type for FormData appropriately if not set manually
  // However, for FormData, you typically don't set Content-Type header manually in fetch,
  // the browser does it correctly with the boundary.
  // We might need a specialized apiClient method or adjust apiClient if it forces application/json
  return apiClient.post(`${API_BASE_URL}/avatar`, formData, {
    headers: {
      // 'Content-Type': 'multipart/form-data' // apiClient might override or handle this. Let's test.
      // Let browser set Content-Type for FormData
    }
  }); 
  // If apiClient struggles with FormData, we might need to make a raw fetch call here
  // or enhance apiClient to handle FormData without forcing 'application/json'.
};

const userService = {
  getCurrentUserDetails,
  updateProfile,
  uploadAvatar,
};

export default userService; 