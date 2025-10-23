import React, { createContext, useState, useContext, useEffect } from 'react';
import authService from '../services/authService';
import userService from '../services/userService'; // Import userService
import { jwtDecode } from 'jwt-decode'; // Ensure this is installed

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true); // For initial auth state loading

  // Function to fetch full user details and update context
  const fetchAndSetUser = async () => {
    setLoading(true);
    try {
      const token = authService.getToken();
      if (token) {
        // Validate token locally (optional, backend will validate anyway)
        const decodedToken = jwtDecode(token);
        if (decodedToken.exp * 1000 < Date.now()) {
          // Token expired, clear auth data
          authService.logout();
          setUser(null);
        } else {
          try {
            // Token is valid, fetch full user details from backend
            const fullUserDetails = await userService.getCurrentUserDetails();
            setUser(fullUserDetails);
            // Also, update localStorage.currentUser if your app uses it elsewhere directly
            localStorage.setItem('currentUser', JSON.stringify(fullUserDetails));
          } catch (apiError) {
            // If API call fails (e.g., 401 Unauthorized), clear auth data
            console.error("AuthContext: API error, clearing auth data:", apiError);
            authService.logout();
            setUser(null);
          }
        }
      } else {
        setUser(null);
      }
    } catch (error) {
      console.error("AuthContext: Error fetching user:", error);
      // Clear any stale auth data on any error
      authService.logout();
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAndSetUser(); // Fetch user on initial load
  }, []);

  const login = async (username, password) => {
    try {
      const loginResponse = await authService.login(username, password); // This stores token & basic user in LS
      // After successful login and token storage by authService, fetch full user details
      if (loginResponse && loginResponse.token) {
        // Record login time for session timer
        localStorage.setItem('loginTime', Date.now().toString());
        await fetchAndSetUser(); // This will use the new token to get full user details
      }
      // No need to return loginResponse directly if fetchAndSetUser handles user state
    } catch (error) {
      console.error("AuthContext: Login error", error);
      throw error; // Re-throw to be handled by LoginPage
    }
  };

  const logout = async () => {
    try {
      const logoutResponse = await authService.logout();
      setUser(null);

      // 清理localStorage（现在由authService在错误时处理，这里是成功时的清理）
      localStorage.removeItem('userToken');
      localStorage.removeItem('currentUser');
      localStorage.removeItem('loginTime');

      return logoutResponse;
    } catch (error) {
      console.error("AuthContext: Logout error", error);
      setUser(null);

      // 清理localStorage（错误情况下的清理）
      localStorage.removeItem('userToken');
      localStorage.removeItem('currentUser');
      localStorage.removeItem('loginTime');

      throw error;
    }
  };

  // Function to be called after profile updates or other relevant actions
  const refreshCurrentUser = async () => {
    console.log("AuthContext: Refreshing current user...");
    await fetchAndSetUser(); // Re-fetch and set user data
  };

  // If still loading initial auth state, you might want to show a global spinner
  // For now, children will render and components can handle their own loading based on `user` being null
  // if (loading) {
  //   return <Spin size="large" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh'}} />;
  // }

  return (
    <AuthContext.Provider value={{ isAuthenticated: !!user, user, login, logout, loadingAuth: loading, refreshCurrentUser }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}; 