import React, { useState } from 'react';
import { Form, Input, Button, Card, Typography, Alert, Spin } from 'antd';
import { UserOutlined, LockOutlined, MailOutlined } from '@ant-design/icons';
// import { useAuth } from '../contexts/AuthContext'; // Removed as not used
import authService from '../services/authService'; // Direct service call for signup
import { useNavigate, Link } from 'react-router-dom';

const { Title } = Typography;
// const { Option } = Select; // Removed as role selector is commented out

const SignupPage = () => {
  const [form] = Form.useForm();
  const navigate = useNavigate();
  // const { login } = useAuth(); // If you want to auto-login after signup
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const onFinish = async (values) => {
    setError('');
    setSuccess('');
    setLoading(true);

    const userData = {
      username: values.username,
      email: values.email,
      password: values.password,
      // Backend expects roles as an array of strings e.g. ["ROLE_USER"]
      // Defaulting to user if no role selector, or you can add a role selector
      role: values.role ? [values.role] : ["ROLE_USER"] 
    };

    try {
      const response = await authService.signup(userData);
      setSuccess(response.message || 'Registration successful! Please login.');
      // Optionally, clear form or redirect to login
      form.resetFields();
      setTimeout(() => navigate('/login'), 2000); // Redirect after a short delay
    } catch (err) {
      setError(err.message || 'Signup failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 'calc(100vh - 140px)' }}>
      <Card title={<Title level={3} style={{ textAlign: 'center', marginBottom:0 }}>创建账户</Title>} style={{ width: 450 }}>
        {error && <Alert message={error} type="error" showIcon style={{ marginBottom: '10px' }}/>}
        {success && <Alert message={success} type="success" showIcon style={{ marginBottom: '10px' }}/>}
        <Form
          form={form}
          name="signup_form"
          onFinish={onFinish}
          autoComplete="off"
          layout="vertical"
        >
          <Form.Item
            name="username"
            label="用户名"
            rules={[{ required: true, message: '请输入您的用户名!' }]}
          >
            <Input prefix={<UserOutlined />} placeholder="用户名" />
          </Form.Item>

          <Form.Item
            name="email"
            label="邮箱"
            rules={[
              { required: true, message: '请输入您的邮箱地址!' },
              { type: 'email', message: '请输入有效的邮箱地址!' }
            ]}
          >
            <Input prefix={<MailOutlined />} placeholder="邮箱" />
          </Form.Item>

          <Form.Item
            name="password"
            label="密码"
            rules={[{ required: true, message: '请输入您的密码!' }]} 
            hasFeedback
          >
            <Input.Password prefix={<LockOutlined />} placeholder="密码" />
          </Form.Item>

          <Form.Item
            name="confirmPassword"
            label="确认密码"
            dependencies={['password']}
            hasFeedback
            rules={[
              { required: true, message: '请确认您的密码!' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('password') === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error('两次输入的密码不匹配!'));
                },
              }),
            ]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="确认密码" />
          </Form.Item>

          {/* Optional Role Selector - Your backend AuthController handles role strings like "admin", "user" */}
          {/* For simplicity, we can omit this and default to ROLE_USER or let backend assign default */}
          {/* <Form.Item
            name="role"
            label="角色 (可选)"
          >
            <Select placeholder="选择一个角色">
              <Option value="user">User</Option> // This will be sent as ["user"] and converted to ERole.ROLE_USER 
              <Option value="admin">Admin</Option> // Backend should handle mapping "admin" to ERole.ROLE_ADMIN
            </Select>
          </Form.Item> */}

          <Form.Item>
            <Button type="primary" htmlType="submit" style={{ width: '100%' }} loading={loading} disabled={loading}>
              {loading ? <Spin /> : '注册'}
            </Button>
          </Form.Item>
        </Form>
        <div style={{ textAlign: 'center' }}>
          已有账户? <Link to="/login">前往登录</Link>
        </div>
      </Card>
    </div>
  );
};

export default SignupPage; 