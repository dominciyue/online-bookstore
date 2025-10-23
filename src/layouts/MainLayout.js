import React from 'react';
import { Layout, Menu, Input, Button, Space, Dropdown, Avatar, message } from 'antd';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import {
  HomeOutlined,
  ShoppingCartOutlined,
  UserOutlined,
  SolutionOutlined,
  LoginOutlined,
  LogoutOutlined,
  FormOutlined,
  TeamOutlined, // Icon for User Management
  BookOutlined, // Icon for Book Management
  SettingOutlined, // For general admin settings or management
  ShoppingOutlined, // For order management
  AreaChartOutlined // For statistics
} from '@ant-design/icons';
import { useAuth } from '../contexts/AuthContext'; // Import useAuth

const { Header, Content, Sider } = Layout;
const { Search } = Input;

// Helper function to determine selected keys from path
const getSelectedKeys = (pathname) => {
  if (pathname.startsWith('/cart')) return ['cart'];
  if (pathname.startsWith('/profile')) return ['profile'];
  if (pathname.startsWith('/orders')) return ['orders'];
  if (pathname.startsWith('/admin/users')) return ['admin-users']; // Key for admin users page
  if (pathname.startsWith('/admin/books')) return ['admin-books']; // Key for admin books page
  if (pathname.startsWith('/category')) return ['categories']; // Example for a category menu item
  if (pathname === '/') return ['home'];
  return [];
};

const MainLayout = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { isAuthenticated, user, logout } = useAuth();
  const selectedKeys = getSelectedKeys(location.pathname);

  const handleSearch = (value) => {
    if (value.trim()) {
      navigate(`/search?q=${encodeURIComponent(value.trim())}`);
    }
  };

  const handleLogout = async () => {
    try {
      const logoutResponse = await logout();
      console.log('=== LOGOUT DEBUG INFO ===');
      console.log('Full logout response:', logoutResponse);
      console.log('Session duration from response:', logoutResponse?.sessionDuration);
      console.log('Response type:', typeof logoutResponse?.sessionDuration);

      if (logoutResponse && logoutResponse.sessionDuration && logoutResponse.sessionDuration > 0) {
        const seconds = Math.floor(logoutResponse.sessionDuration / 1000);
        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = seconds % 60;
        const durationText = `您已成功登出! 本次会话持续时间: ${minutes}分钟${remainingSeconds}秒`;

        console.log('=== SUCCESS: Showing session duration ===');
        console.log('Formatted duration text:', durationText);
        console.log('Original sessionDuration:', logoutResponse.sessionDuration);
        console.log('Calculated minutes:', minutes, 'seconds:', remainingSeconds);

        // 显示包含持续时间的消息
        message.success({
          content: durationText,
          duration: 4, // 显示4秒
        });
      } else {
        console.warn('=== WARNING: No valid session duration found ===');
        console.warn('logoutResponse:', logoutResponse);
        console.warn('logoutResponse.sessionDuration:', logoutResponse?.sessionDuration);
        console.warn('Is sessionDuration > 0?', logoutResponse?.sessionDuration > 0);
        console.warn('Full logoutResponse object:', JSON.stringify(logoutResponse, null, 2));

        // 显示基本成功消息
        message.success({
          content: '您已成功登出!',
          duration: 3,
        });
      }

      // 立即跳转到登录页面
      navigate('/login');
    } catch (error) {
      console.error('Logout error:', error);
      message.error({
        content: '登出时发生错误，请稍后重试',
        duration: 3,
      });

      // 立即跳转到登录页面
      navigate('/login');
    }
  };

  const userMenuItems = (
    <Menu>
      <Menu.Item key="profile">
        <Link to="/profile"><UserOutlined /> 个人中心</Link>
      </Menu.Item>
      <Menu.Item key="orders">
        <Link to="/orders"><SolutionOutlined /> 我的订单</Link>
      </Menu.Item>
      {user && user.roles && user.roles.includes('ROLE_ADMIN') && (
        <Menu.Item key="admin-users-dropdown">
          <Link to="/admin/users"><TeamOutlined /> 用户管理</Link>
        </Menu.Item>
      )}
      {user && user.roles && user.roles.includes('ROLE_ADMIN') && (
        <Menu.Item key="admin-books-dropdown">
          <Link to="/admin/books"><BookOutlined /> 书籍管理</Link>
        </Menu.Item>
      )}
      <Menu.Divider />
      <Menu.Item key="logout" onClick={handleLogout}>
        <LogoutOutlined /> 退出登录
      </Menu.Item>
    </Menu>
  );

  const mainMenuItems = [
    {
      key: 'home',
      icon: <HomeOutlined />,
      label: <Link to="/">主页</Link>,
    },
    {
      key: 'cart',
      icon: <ShoppingCartOutlined />,
      label: <Link to="/cart">购物车</Link>,
    },
    ...(isAuthenticated ? [
      {
        key: 'orders',
        icon: <SolutionOutlined />,
        label: <Link to="/orders">我的订单</Link>,
      },
      {
        key: 'profile',
        icon: <UserOutlined />,
        label: <Link to="/profile">个人信息</Link>,
      },
    ] : []),
    // Admin specific menu item in Sider
    ...(isAuthenticated && user && user.roles && user.roles.includes('ROLE_ADMIN') ? [
      {
        key: 'admin-users',
        icon: <TeamOutlined />,
        label: <Link to="/admin/users">用户管理</Link>,
      },
      {
        key: 'admin-books',
        icon: <BookOutlined />,
        label: <Link to="/admin/books">书籍管理</Link>,
      },
      {
        key: 'admin-orders',
        icon: <ShoppingOutlined />,
        label: <Link to="/admin/orders">订单管理</Link>,
      },
      {
        key: 'admin-statistics',
        icon: <AreaChartOutlined />,
        label: <Link to="/admin/statistics">统计分析</Link>,
      }
    ] : []),
  ];

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header style={{ background: '#fff', padding: '0 24px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ fontSize: '20px', fontWeight: 'bold' }}>
          <Link to="/" style={{ color: 'inherit', textDecoration: 'none' }}>在线书店</Link>
        </div>
        <Search
          placeholder="搜索书籍..."
          onSearch={handleSearch}
          style={{ width: 300, verticalAlign: 'middle', margin: '0 20px' }}
        />
        <Space>
          {isAuthenticated && user ? (
            <Dropdown overlay={userMenuItems} trigger={['click']}>
              <Button type="text" style={{ display: 'flex', alignItems: 'center' }}>
                <Avatar icon={<UserOutlined />} src={user.avatarUrl ? `http://localhost:8080${user.avatarUrl}` : undefined} size="small" style={{ marginRight: 8 }} />
                {user.username}
              </Button>
            </Dropdown>
          ) : (
            <Space>
              <Button icon={<LoginOutlined />} onClick={() => navigate('/login')}>登录</Button>
              <Button icon={<FormOutlined />} type="primary" onClick={() => navigate('/signup')}>注册</Button>
            </Space>
          )}
        </Space>
      </Header>
      <Layout>
        <Sider width={200} style={{ background: '#fff' }}>
          <Menu
            mode="inline"
            selectedKeys={selectedKeys}
            style={{ height: '100%', borderRight: 0 }}
            items={mainMenuItems}
          />
        </Sider>
        <Layout style={{ padding: '0 24px 24px' }}>
          <Content
            style={{
              background: '#fff',
              padding: 24,
              margin: '16px 0',
              minHeight: 280,
            }}
          >
            <Outlet />
          </Content>
        </Layout>
      </Layout>
    </Layout>
  );
};

export default MainLayout; 