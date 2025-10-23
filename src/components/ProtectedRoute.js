import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { Spin, Alert } from 'antd';

const ProtectedRoute = ({ children, requiredRole }) => {
  const { isAuthenticated, user, loadingAuth } = useAuth();
  const location = useLocation();

  if (loadingAuth) {
    // Show a loading spinner while auth state is being determined
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // If a requiredRole is specified, check if the user has that role
  if (requiredRole && (!user || !user.roles || !user.roles.includes(requiredRole))) {
    // User is authenticated but does not have the required role
    return (
      <div style={{ padding: '50px', textAlign: 'center' }}>
        <Alert 
          message="权限不足"
          description="您没有权限访问此页面。"
          type="error"
          showIcon 
        />
        {/* Optionally, add a button to go back or to home */}
      </div>
    );
    // Or redirect to a generic "Unauthorized" page: return <Navigate to="/unauthorized" replace />;
  }

  return children;
};

export default ProtectedRoute; 