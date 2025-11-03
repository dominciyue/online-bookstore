import React, { useEffect, useState } from 'react';
import { Form, Input, Button, Card, Avatar, Upload, Typography, message, Spin, Row, Col, Descriptions, DatePicker, Table, Empty, Alert, Statistic } from 'antd';
import { UserOutlined, UploadOutlined, MailOutlined, PhoneOutlined, HomeOutlined, BarChartOutlined, ShoppingCartOutlined } from '@ant-design/icons';
import { useAuth } from '../contexts/AuthContext';
import userService from '../services/userService';
import statisticsService from '../services/statisticsService';
import dayjs from 'dayjs';

const { Title, Text } = Typography;
const { RangePicker } = DatePicker;

const UserProfile = () => {
  const [form] = Form.useForm();
  const { user, loading: authLoading, error: authError, refreshCurrentUser } = useAuth();
  const [profileUpdateLoading, setProfileUpdateLoading] = useState(false);
  const [avatarUploading, setAvatarUploading] = useState(false);

  const [statsDateRange, setStatsDateRange] = useState([dayjs().subtract(30, 'days'), dayjs()]);
  const [personalBookStatsList, setPersonalBookStatsList] = useState([]);
  const [loadingStats, setLoadingStats] = useState(false);
  const [statsError, setStatsError] = useState(null);

  const [totalUniqueBookTypes, setTotalUniqueBookTypes] = useState(0);
  const [totalBooksBought, setTotalBooksBought] = useState(0);
  const [overallTotalSpent, setOverallTotalSpent] = useState(0);

  useEffect(() => {
    if (user) {
      form.setFieldsValue({
        username: user.username,
        email: user.email,
        phone: user.phone || '',
        address: user.address || '',
      });
      if (user.id && statsDateRange && statsDateRange[0] && statsDateRange[1]) {
        fetchPersonalStats(statsDateRange[0], statsDateRange[1]);
      }
    }
  }, [user, form]);

  const fetchPersonalStats = async (startDateInput, endDateInput) => {
    if (!user || !user.id || !startDateInput || !endDateInput) return;
    setLoadingStats(true);
    setStatsError(null);
    try {
      const startDate = startDateInput.toISOString();
      const endDate = endDateInput.toISOString();
      const statsData = await statisticsService.getMyBookStats(startDate, endDate);
      
      if (Array.isArray(statsData)) {
        setPersonalBookStatsList(statsData);

        setTotalUniqueBookTypes(statsData.length);
        let booksBought = 0;
        let totalSpent = 0;
        statsData.forEach(item => {
          booksBought += item.quantityBought || 0;
          totalSpent += item.totalSpentOnBook || 0;
        });
        setTotalBooksBought(booksBought);
        setOverallTotalSpent(totalSpent);
      } else {
        console.warn("UserProfile.js: getMyBookStats returned non-array data:", statsData);
        setPersonalBookStatsList([]);
        setTotalUniqueBookTypes(0);
        setTotalBooksBought(0);
        setOverallTotalSpent(0);
      }

    } catch (err) {
      message.error(`加载个人统计失败: ${err.message || '未知错误'}`);
      setStatsError(err.message || '获取统计数据时发生错误');
      setPersonalBookStatsList([]);
      setTotalUniqueBookTypes(0);
      setTotalBooksBought(0);
      setOverallTotalSpent(0);
    }
    setLoadingStats(false);
  };

  useEffect(() => {
    if (user && user.id && statsDateRange && statsDateRange[0] && statsDateRange[1]) {
      fetchPersonalStats(statsDateRange[0], statsDateRange[1]);
    } else if (!statsDateRange) {
        setPersonalBookStatsList([]);
        setTotalUniqueBookTypes(0);
        setTotalBooksBought(0);
        setOverallTotalSpent(0);
    }
  }, [statsDateRange]);

  const onProfileFinish = async (values) => {
    setProfileUpdateLoading(true);
    try {
      const updateData = {
        phone: values.phone,
        address: values.address,
      };
      const response = await userService.updateProfile(updateData);
      message.success(response.message || '个人信息更新成功！');
      if (refreshCurrentUser) {
        await refreshCurrentUser();
      }
    } catch (err) {
      message.error(err.message || '更新失败，请稍后再试。');
    } finally {
      setProfileUpdateLoading(false);
    }
  };

  const handleCustomAvatarUpload = async ({ file, onSuccess, onError }) => {
    setAvatarUploading(true);
    const formData = new FormData();
    formData.append('avatar', file);

    try {
      const response = await userService.uploadAvatar(formData);
      onSuccess(response, file);
      message.success(response.message || '头像上传成功!');
      if (refreshCurrentUser) {
        await refreshCurrentUser();
      }
    } catch (err) {
      onError(err);
      message.error(err.message || '头像上传失败，请检查文件或稍后再试。');
    } finally {
      setAvatarUploading(false);
    }
  };

  const handleStatsDateRangeChange = (dates) => {
    if (dates && dates.length === 2) {
        setStatsDateRange(dates);
    } else {
        setStatsDateRange(null);
    }
  };

  const personalStatsColumns = [
    { title: '封面', dataIndex: 'bookCover', key: 'bookCover', render: (cover, record) => <Avatar shape="square" size={64} src={cover || 'https://via.placeholder.com/64x80?text=N/A'} alt={record.bookTitle} /> },
    { title: '书名', dataIndex: 'bookTitle', key: 'bookTitle', ellipsis: true },
    { title: '作者', dataIndex: 'bookAuthor', key: 'bookAuthor', ellipsis: true },    
    { title: '购买数量', dataIndex: 'quantityBought', key: 'quantityBought', width: 100, align: 'right' },
    { title: '花费金额', dataIndex: 'totalSpentOnBook', key: 'totalSpentOnBook', render: (val) => `¥${val != null ? Number(val).toFixed(2) : '0.00'}`, width: 120, align: 'right' },
  ];

  if (authLoading && !user) return <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 'calc(100vh - 128px)' }}><Spin size="large" tip="加载用户信息..." /></div>;
  if (authError) return <Alert message="认证错误" description={authError} type="error" showIcon />;
  if (!user && !authLoading) return <Alert message="错误" description="无法加载用户信息，请重新登录。" type="error" showIcon />;

  return (
    <Row gutter={[24, 24]}>
      <Col xs={24} md={10} lg={8}>
        <Card title="账户信息" bordered={false} style={{ boxShadow: '0 2px 8px rgba(0,0,0,0.09)' }}>
            <div style={{ textAlign: 'center', marginBottom: 24 }}>
                <Avatar size={100} src={user?.avatarUrl ? `http://localhost:8082${user.avatarUrl}` : undefined} icon={!user?.avatarUrl && <UserOutlined />} />
                <Upload 
                    name="avatar"
                    showUploadList={false} 
                    customRequest={handleCustomAvatarUpload}
                    beforeUpload={(file) => {
                    const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif';
                    if (!isJpgOrPng) {
                        message.error('你只能上传 JPG/PNG/GIF 文件!');
                    }
                    const isLt2M = file.size / 1024 / 1024 < 2;
                    if (!isLt2M) {
                        message.error('图片必须小于 2MB!');
                    }
                    return isJpgOrPng && isLt2M;
                    }}
                >
                    <Button icon={<UploadOutlined />} style={{ marginTop: 16 }} loading={avatarUploading}>
                    {avatarUploading ? '上传中...' : '更换头像'}
                    </Button>
                </Upload>
            </div>
            <Form
                form={form}
                layout="vertical"
                onFinish={onProfileFinish}
                initialValues={{
                    username: user?.username,
                    email: user?.email,
                    phone: user?.phone || '',
                    address: user?.address || '',
                }}
            >
            <Form.Item name="username" label={<><UserOutlined style={{marginRight: '8px'}}/>用户名</>}>
              <Input readOnly />
            </Form.Item>
            <Form.Item name="email" label={<><MailOutlined style={{marginRight: '8px'}}/>邮箱</>}>
              <Input readOnly />
            </Form.Item>
            <Form.Item
              name="phone"
              label={<><PhoneOutlined style={{marginRight: '8px'}}/>手机号</>}
              rules={[{ pattern: /^1\d{10}$/, message: '请输入有效的手机号' }]}
            >
              <Input placeholder="请输入您的电话号码" />
            </Form.Item>
            <Form.Item name="address" label={<><HomeOutlined style={{marginRight: '8px'}}/>收货地址</>}>
              <Input.TextArea rows={3} placeholder="请输入您的收货地址" />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" block loading={profileUpdateLoading}>
                {profileUpdateLoading ? '保存中...' : '保存修改'}
              </Button>
            </Form.Item>
          </Form>
        </Card>
      </Col>
      <Col xs={24} md={14} lg={16}>
        <Card title={<><BarChartOutlined style={{marginRight: 8}}/>我的购书统计</>} bordered={false} style={{ boxShadow: '0 2px 8px rgba(0,0,0,0.09)' }}>
            <div style={{ marginBottom: 16 }}>
                <Text strong>选择统计时间范围: </Text>
                <RangePicker 
                    value={statsDateRange} 
                    onChange={handleStatsDateRangeChange} 
                    allowClear={false}
                    style={{marginLeft: 8}}
                    ranges={{
                        '过去7天': [dayjs().subtract(7, 'days'), dayjs()],
                        '过去30天': [dayjs().subtract(30, 'days'), dayjs()],
                        '本月': [dayjs().startOf('month'), dayjs().endOf('month')],
                        '上个月': [dayjs().subtract(1, 'month').startOf('month'), dayjs().subtract(1, 'month').endOf('month')],
                    }}
                />
            </div>
            {loadingStats && <div style={{textAlign: 'center', padding: '20px'}}><Spin tip="加载统计数据中..."/></div>}
            {statsError && !loadingStats && <Alert message="加载统计出错" description={statsError} type="error" showIcon />}
            {!loadingStats && !statsError && (
                <>
                    <Row gutter={16} style={{ marginBottom: 24, textAlign: 'center' }}>
                        <Col span={8}>
                            <Statistic title="购买书籍种类" value={totalUniqueBookTypes} prefix={<ShoppingCartOutlined />} />
                        </Col>
                        <Col span={8}>
                            <Statistic title="总购买书籍" value={totalBooksBought} suffix="本" />
                        </Col>
                        <Col span={8}>
                            <Statistic title="总消费金额" value={overallTotalSpent} precision={2} prefix="¥" />
                        </Col>
                    </Row>
                    <Title level={5} style={{marginBottom: 16}}>详细购书列表</Title>
                    {personalBookStatsList.length > 0 ? (
                        <Table 
                            dataSource={personalBookStatsList}
                            columns={personalStatsColumns}
                            rowKey="bookId"
                            size="small"
                            pagination={{ pageSize: 5 }}
                            scroll={{ y: 300, x: 'max-content' }}
                        />
                    ) : (
                        <Empty description={statsDateRange ? "选定日期范围内暂无购书记录" : "请选择日期范围查看统计"} />
                    )}
                </>
            )}
        </Card>
      </Col>
    </Row>
  );
};

export default UserProfile; 