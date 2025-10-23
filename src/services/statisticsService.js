import apiClient from './apiClient';

const API_BASE_URL = 'http://localhost:8080/api/statistics';

// Fetch book sales statistics (Admin)
const getBookSalesStats = async (startDate, endDate) => {
  const params = new URLSearchParams();
  if (startDate) params.append('startDate', startDate.toISOString());
  if (endDate) params.append('endDate', endDate.toISOString());
  return apiClient.get(`${API_BASE_URL}/book-sales?${params.toString()}`);
};

// Fetch user consumption statistics (Admin)
const getUserConsumptionStats = async (startDate, endDate) => {
  const params = new URLSearchParams();
  if (startDate) params.append('startDate', startDate.toISOString());
  if (endDate) params.append('endDate', endDate.toISOString());
  return apiClient.get(`${API_BASE_URL}/user-consumption?${params.toString()}`);
};

// Fetch personal book statistics (Authenticated User)
const getMyBookStats = async (startDate, endDate) => {
  // The parameters startDate and endDate are now expected to be ISO strings
  // So, we pass them directly to the apiClient
  const params = {
    startDate: startDate, // No longer call .toISOString()
    endDate: endDate,     // No longer call .toISOString()
  };
  try {
    const response = await apiClient.get(`${API_BASE_URL}/my-book-stats`, params);
    return response;
  } catch (error) {
    console.error("Error in getMyBookStats service:", error);
    throw error; // Re-throw to be handled by the calling component
  }
};

const statisticsService = {
  getBookSalesStats,
  getUserConsumptionStats,
  getMyBookStats,
};

export default statisticsService; 