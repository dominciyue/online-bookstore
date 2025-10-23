import React, { useState } from 'react';
import { Form, Input, Button, Card, Typography, Alert, Spin } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate, Link, useLocation } from 'react-router-dom';

const { Title } = Typography;

const LoginPage = () => {
  const [form] = Form.useForm();
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const from = location.state?.from?.pathname || "/"; // Redirect to previous page or home

  const onFinish = async (values) => {
    setError('');
    setLoading(true);
    try {
      await login(values.username, values.password);
      navigate(from, { replace: true });
    } catch (err) {
      setError(err.message || 'Login failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 'calc(100vh - 140px)' }}>
      <Card title={<Title level={3} style={{ textAlign: 'center', marginBottom:0 }}>用户登录</Title>} style={{ width: 400 }}>
        {error && <Alert message={error} type="error" showIcon style={{ marginBottom: '20px' }}/>}
        <Form
          form={form}
          name="login_form"
          onFinish={onFinish}
          autoComplete="off"
        >
          <Form.Item
            name="username"
            rules={[{ required: true, message: '请输入您的用户名!' }]}
          >
            <Input prefix={<UserOutlined />} placeholder="用户名" />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: '请输入您的密码!' }]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="密码" />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" style={{ width: '100%' }} loading={loading} disabled={loading}>
              {loading ? <Spin /> : '登录'}
            </Button>
          </Form.Item>
        </Form>
        <div style={{ textAlign: 'center' }}>
          没有账户? <Link to="/signup">立即注册</Link>
        </div>
      </Card>
    </div>
  );
};

export default LoginPage; 