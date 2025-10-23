import authService from './authService'; // To get the token

// Helper function to handle API responses (can be shared or part of apiClient)
const handleResponse = async (response) => {
  const text = await response.text(); // Get text first to avoid "already read" errors
  if (!response.ok) {
    let errorMessage = `HTTP error! status: ${response.status}`;
    if (text) {
      try {
        const errorData = JSON.parse(text);
        errorMessage = errorData.message || errorData.error || errorMessage;
      } catch (e) {
        // If error response is not JSON, use the text itself or the HTTP error
        errorMessage = text || errorMessage;
      }
    }
    throw new Error(errorMessage);
  }
  // If response is OK and text is not empty, try to parse as JSON
  // Otherwise, for 204 No Content or non-JSON success, return null or the text itself if needed
  if (!text) return null; // Handles 204 No Content specifically
  try {
    return JSON.parse(text);
  } catch (e) {
    return text; // If it's not JSON but request was OK (e.g., plain text response)
  }
};

const apiClient = async (url, options = {}) => {
  const token = authService.getToken();
  // ---- DEBUGGING LOGS ----
  console.log(`[apiClient] Requesting URL: ${options.method || 'GET'} ${url}`);
  console.log('[apiClient] Token from authService.getToken():', token);
  // ---- END DEBUGGING LOGS ----

  const defaultHeaders = {}; // Initialize empty

  // Set Content-Type only if body is not FormData
  // For FormData, browser sets it with the correct boundary
  if (!(options.body instanceof FormData)) {
    defaultHeaders['Content-Type'] = 'application/json';
  }

  if (token) {
    console.log('[apiClient] Token exists, adding Authorization header.'); // DEBUG LOG
    defaultHeaders['Authorization'] = `Bearer ${token}`;
  } else {
    console.log('[apiClient] No token found, not adding Authorization header.'); // DEBUG LOG
  }

  const config = {
    ...options,
    headers: {
      ...defaultHeaders,
      ...options.headers,
    },
  };
  
  // If body is FormData, delete Content-Type from final config headers 
  // to let the browser handle it. This is crucial.
  if (config.body instanceof FormData) {
    delete config.headers['Content-Type'];
  }

  console.log('[apiClient] Request config being sent:', config); // DEBUG LOG

  try {
    const response = await fetch(url, config);
    // Log the raw response status as well for immediate feedback
    console.log(`[apiClient] Response status for ${url}: ${response.status}`); 
    return handleResponse(response);
  } catch (error) {
    console.error('API Client Error:', error);
    throw error; // Re-throw to be handled by the calling service/component
  }
};

// Convenience methods for GET, POST, PUT, DELETE
apiClient.get = (url, queryParams = {}) => {
  const MRA_url = new URL(url); // Use a different variable name to avoid conflict if 'URL' is used elsewhere
  if (queryParams) {
    Object.keys(queryParams).forEach(key => {
      if (queryParams[key] !== undefined) {
        MRA_url.searchParams.append(key, queryParams[key]);
      }
    });
  }
  // Call the main apiClient with the new URL that includes query parameters
  return apiClient(MRA_url.toString(), { method: 'GET' });
};

apiClient.post = (url, data, options = {}) => {
  // If data is FormData, pass it directly as body, otherwise stringify
  const body = data instanceof FormData ? data : JSON.stringify(data);
  return apiClient(url, { ...options, method: 'POST', body });
};

apiClient.put = (url, data, options = {}) => {
  const body = data instanceof FormData ? data : JSON.stringify(data);
  return apiClient(url, { ...options, method: 'PUT', body });
};

apiClient.delete = (url, options = {}) => {
  return apiClient(url, { ...options, method: 'DELETE' });
};

export default apiClient; 