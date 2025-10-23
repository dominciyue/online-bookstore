import React, { useState, useEffect } from 'react';
import { Table, Spin, Alert, Typography, Button, Switch, message, Space } from 'antd';
import adminService from '../services/adminService';

const { Title } = Typography;

const AdminUserManagement = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchUsers = async () => {
        setLoading(true);
        setError(null);
        try {
            const fetchedUsers = await adminService.getAllUsers();
            setUsers(fetchedUsers || []);
        } catch (err) {
            console.error("Failed to fetch users:", err);
            setError('无法加载用户列表，请稍后再试。');
            setUsers([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    const handleToggleUserEnabled = async (userId, currentEnabledStatus) => {
        try {
            let response;
            if (currentEnabledStatus) {
                response = await adminService.disableUser(userId);
            } else {
                response = await adminService.enableUser(userId);
            }
            message.success(response.message || '用户状态更新成功！');
            // Optimistically update UI or refetch
            setUsers(prevUsers => 
                prevUsers.map(user => 
                    user.id === userId ? { ...user, enabled: !currentEnabledStatus } : user
                )
            );
            // Or refetch for consistency: fetchUsers();
        } catch (err) {
            message.error(`操作失败: ${err.message || '未知错误'}`);
        }
    };

    const columns = [
        { title: 'ID', dataIndex: 'id', key: 'id', sorter: (a, b) => a.id - b.id },
        { title: '用户名', dataIndex: 'username', key: 'username', sorter: (a, b) => a.username.localeCompare(b.username) },
        { title: '邮箱', dataIndex: 'email', key: 'email' },
        {
            title: '角色',
            dataIndex: 'roles',
            key: 'roles',
            render: roles => (
                <Space direction="vertical">
                    {roles.map(role => <span key={role}>{role}</span>)}
                </Space>
            ),
            filters: [
                { text: 'ADMIN', value: 'ROLE_ADMIN' },
                { text: 'USER', value: 'ROLE_USER' },
                { text: 'MODERATOR', value: 'ROLE_MODERATOR' },
            ],
            onFilter: (value, record) => record.roles.includes(value),
        },
        {
            title: '启用状态',
            dataIndex: 'enabled',
            key: 'enabled',
            render: (enabled, record) => (
                <Switch 
                    checked={enabled} 
                    onChange={() => handleToggleUserEnabled(record.id, enabled)}
                    checkedChildren="已启用"
                    unCheckedChildren="已禁用"
                />
            ),
            filters: [
                { text: '已启用', value: true },
                { text: '已禁用', value: false },
            ],
            onFilter: (value, record) => record.enabled === value,
        },
        // 可选：添加操作列，例如直接编辑用户信息等
    ];

    if (loading) {
        return <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '50vh' }}><Spin size="large" tip="正在加载用户数据..." /></div>;
    }

    if (error) {
        return <Alert message="错误" description={error} type="error" showIcon style={{ margin: '20px' }} />;
    }

    return (
        <div>
            <Title level={2} style={{ marginBottom: '24px' }}>用户管理</Title>
            <Button onClick={fetchUsers} style={{ marginBottom: 16}} disabled={loading}>
                刷新列表
            </Button>
            <Table 
                dataSource={users}
                columns={columns} 
                rowKey="id" 
                loading={loading}
            />
        </div>
    );
};

export default AdminUserManagement; 