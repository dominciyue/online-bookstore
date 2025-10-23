const API_BASE_URL = 'http://localhost:8080/api/auth';

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
  return text ? JSON.parse(text) : null;
};

const login = async (username, password) => {
  try {
    const response = await fetch(`${API_BASE_URL}/signin`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ username, password }),
    });
    const data = await handleResponse(response);
    if (data && data.token) {
      localStorage.setItem('userToken', data.token);
      // Optionally store user info as well, but token is key for auth
      localStorage.setItem('currentUser', JSON.stringify({ id: data.id, username: data.username, email: data.email, roles: data.roles }));
    }
    return data;
  } catch (error) {
    console.error('Login error:', error);
    throw error;
  }
};

const signup = async (userData) => {
  // userData should be an object like { username, email, password, role (optional array) }
  try {
    const response = await fetch(`${API_BASE_URL}/signup`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(userData),
    });
    return handleResponse(response);
  } catch (error) {
    console.error('Signup error:', error);
    throw error;
  }
};

const logout = async () => {
  try {
    const token = getToken();
    if (token) {
      try {
        const response = await fetch(`${API_BASE_URL}/signout`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
          },
        });
        const responseData = await handleResponse(response); // Get the response data
        console.log('=== AUTH SERVICE DEBUG ===');
        console.log('Logout API response data:', responseData);
        console.log('Response type:', typeof responseData);
        console.log('Has sessionDuration?', responseData?.hasOwnProperty('sessionDuration'));
        console.log('Session duration value:', responseData?.sessionDuration);
        return responseData; // Return the response data for the caller to use
      } catch (signoutError) {
        // Even if signout API fails, continue with local cleanup
        console.warn('Signout API failed, but continuing with local cleanup:', signoutError);
        return null; // Return null if API fails, but still proceed with logout
      }
    }
    return null;
  } catch (error) {
    console.error('Logout error:', error);
    return null;
  } finally {
    // Always clear local storage regardless of API success/failure
    localStorage.removeItem('userToken');
    localStorage.removeItem('currentUser');
    localStorage.removeItem('loginTime');
  }
};

const getCurrentUser = () => {
  const userStr = localStorage.getItem('currentUser');
  return userStr ? JSON.parse(userStr) : null;
};

const getToken = () => {
  return localStorage.getItem('userToken');
}

const authService = {
  login,
  signup,
  logout,
  getCurrentUser,
  getToken,
};

// 添加一个测试函数来验证logout响应格式
const testLogoutResponse = () => {
  console.log('=== TESTING LOGOUT RESPONSE FORMAT ===');
  const testResponse = {
    message: "User logged out successfully!",
    sessionDuration: 102874
  };
  console.log('Test response:', testResponse);
  console.log('Has sessionDuration?', testResponse?.hasOwnProperty('sessionDuration'));
  console.log('Session duration value:', testResponse?.sessionDuration);
  console.log('Is sessionDuration > 0?', testResponse?.sessionDuration > 0);

  if (testResponse && testResponse.sessionDuration && testResponse.sessionDuration > 0) {
    const seconds = Math.floor(testResponse.sessionDuration / 1000);
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    const durationText = `您已成功登出! 本次会话持续时间: ${minutes}分钟${remainingSeconds}秒`;
    console.log('Would show:', durationText);
  } else {
    console.log('Would show basic logout message');
  }
};

// 在开发模式下运行测试
if (process.env.NODE_ENV === 'development') {
  // testLogoutResponse(); // 取消注释来运行测试
}

export default authService; 