import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import MainLayout from './layouts/MainLayout';
import Home from './pages/Home';
import BookDetail from './pages/BookDetail';
import Cart from './pages/Cart';
import Profile from './pages/Profile';
import CategoryPage from './pages/Category';
import OrderList from './pages/OrderList';
import SearchResultsPage from './pages/SearchResultsPage';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import ProtectedRoute from './components/ProtectedRoute';
import AdminUserManagement from './pages/AdminUserManagement';
import AdminBookManagement from './pages/AdminBookManagement';
import AdminBookForm from './pages/AdminBookForm';
import AdminOrderManagement from './pages/AdminOrderManagement';
import { AuthProvider } from './contexts/AuthContext';
import { CartProvider } from './data/cartContext';
import AdminStatisticsPage from './pages/AdminStatisticsPage';

// 不再使用 antd/dist/reset.css，新版本有不同的导入方式

const App = () => {
  return (
    <AuthProvider>
      <CartProvider>
        <Router>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/signup" element={<SignupPage />} />
            <Route path="/" element={<MainLayout />}>
              <Route index element={<Home />} />
              <Route path="book/:id" element={<BookDetail />} />
              <Route path="category/:categoryKey" element={<CategoryPage />} />
              <Route path="search" element={<SearchResultsPage />} />
              <Route path="cart" element={<ProtectedRoute><Cart /></ProtectedRoute>} />
              <Route path="profile" element={<ProtectedRoute><Profile /></ProtectedRoute>} />
              <Route path="orders" element={<ProtectedRoute><OrderList /></ProtectedRoute>} />
              <Route 
                path="admin/users" 
                element={(
                  <ProtectedRoute requiredRole="ROLE_ADMIN">
                    <AdminUserManagement />
                  </ProtectedRoute>
                )}
              />
              <Route 
                path="admin/books" 
                element={(
                  <ProtectedRoute requiredRole="ROLE_ADMIN">
                    <AdminBookManagement />
                  </ProtectedRoute>
                )}
              />
              <Route 
                path="admin/books/add" 
                element={(
                  <ProtectedRoute requiredRole="ROLE_ADMIN">
                    <AdminBookForm />
                  </ProtectedRoute>
                )}
              />
              <Route 
                path="admin/books/edit/:bookId" 
                element={(
                  <ProtectedRoute requiredRole="ROLE_ADMIN">
                    <AdminBookForm />
                  </ProtectedRoute>
                )}
              />
              <Route 
                path="admin/orders" 
                element={(
                  <ProtectedRoute requiredRole="ROLE_ADMIN">
                    <AdminOrderManagement />
                  </ProtectedRoute>
                )}
              />
              <Route 
                path="admin/statistics" 
                element={(
                  <ProtectedRoute requiredRole="ROLE_ADMIN">
                    <AdminStatisticsPage />
                  </ProtectedRoute>
                )}
              />
            </Route>
          </Routes>
        </Router>
      </CartProvider>
    </AuthProvider>
  );
};

export default App; 