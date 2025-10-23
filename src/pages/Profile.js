import React, { useEffect, useState } from 'react';
import { Form, Input, Button, Card, Avatar, Upload, Typography, message, Spin, Row, Col, Descriptions, DatePicker, Table, Empty, Alert, Statistic } from 'antd';
import { UserOutlined, UploadOutlined, MailOutlined, PhoneOutlined, HomeOutlined, BarChartOutlined, ShoppingCartOutlined } from '@ant-design/icons';
import { useAuth } from '../contexts/AuthContext';
import userService from '../services/userService';
import statisticsService from '../services/statisticsService';
import dayjs from 'dayjs';

const { Title, Text, Paragraph } = Typography;
const { RangePicker } = DatePicker;

const Profile = () => {
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
      if (statsDateRange && statsDateRange[0] && statsDateRange[1]) {
        fetchPersonalStats(statsDateRange[0], statsDateRange[1]);
      }
    }
  }, [user, form]);

  useEffect(() => {
    console.log("EFFECT UserProfile: user", user);
    console.log("EFFECT UserProfile: statsDateRange before fetch", statsDateRange);
    if (statsDateRange && statsDateRange[0] && statsDateRange[1]) {
      console.log("EFFECT UserProfile: statsDateRange[0] type:", typeof statsDateRange[0], "isDayjs:", dayjs.isDayjs(statsDateRange[0]), "value:", statsDateRange[0]);
      console.log("EFFECT UserProfile: statsDateRange[1] type:", typeof statsDateRange[1], "isDayjs:", dayjs.isDayjs(statsDateRange[1]), "value:", statsDateRange[1]);
    }

    if (user && user.id && statsDateRange && statsDateRange[0] && statsDateRange[1]) {
      fetchPersonalStats(statsDateRange[0], statsDateRange[1]);
    } else if (user && user.id && !statsDateRange) {
      setPersonalBookStatsList([]);
    }
  }, [user, statsDateRange]);

  const fetchPersonalStats = async (startInput, endInput) => {
    if (!user || !user.id || !startInput || !endInput) return;
    setLoadingStats(true);
    setStatsError(null);
    try {
      const startDate = startInput.toISOString();
      const endDate = endInput.toISOString();
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
        console.warn("Profile.js: getMyBookStats returned non-array data:", statsData);
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

  const handleStatsDateRangeChange = (dates, dateStrings) => {
    console.log("RangePicker onChange CALLED. dates:", dates, "dateStrings:", dateStrings);
    if (dates && dates.length === 2 && dates[0] && dates[1]) {
      if (dayjs.isDayjs(dates[0]) && dates[0].isValid() && dayjs.isDayjs(dates[1]) && dates[1].isValid()) {
        console.log("Setting statsDateRange with valid Dayjs objects from 'dates' argument.");
        setStatsDateRange(dates);
      } else if (dateStrings && dateStrings.length === 2 && dateStrings[0] && dateStrings[1]) {
        console.warn("'dates' argument from RangePicker were not valid Dayjs objects. Falling back to dateStrings.", dates);
        const parsedDates = [dayjs(dateStrings[0]), dayjs(dateStrings[1])];
        if (parsedDates[0].isValid() && parsedDates[1].isValid()) {
          console.log("Setting statsDateRange with dates parsed from dateStrings.");
          setStatsDateRange(parsedDates);
        } else {
          console.error("Failed to parse dateStrings into valid Dayjs objects. Clearing range.", dateStrings);
          setStatsDateRange(null);
          setPersonalBookStatsList([]);
          setTotalUniqueBookTypes(0);
          setTotalBooksBought(0);
          setOverallTotalSpent(0);
        }
      } else {
        console.error("RangePicker onChange provided insufficient data to form a valid date range. Clearing range.", { dates, dateStrings });
        setStatsDateRange(null);
        setPersonalBookStatsList([]);
        setTotalUniqueBookTypes(0);
        setTotalBooksBought(0);
        setOverallTotalSpent(0);
      }
    } else if (!dates) {
      console.log("RangePicker cleared. Setting statsDateRange to null.");
      setStatsDateRange(null);
      setPersonalBookStatsList([]);
      setTotalUniqueBookTypes(0);
      setTotalBooksBought(0);
      setOverallTotalSpent(0);
    } else {
      console.warn("RangePicker onChange provided unexpected 'dates' value. Clearing range.", dates);
      setStatsDateRange(null);
      setPersonalBookStatsList([]);
      setTotalUniqueBookTypes(0);
      setTotalBooksBought(0);
      setOverallTotalSpent(0);
    }
  };

  const personalStatsColumns = [
    { title: '封面', dataIndex: 'bookCover', key: 'bookCover', render: (cover, record) => <img src={cover || 'https://via.placeholder.com/40x60?text=N/A'} alt={record.bookTitle} style={{width: 40, height: 60, objectFit: 'contain'}} />, width: 80},
    { title: '书名', dataIndex: 'bookTitle', key: 'bookTitle', ellipsis: true },
    { title: '作者', dataIndex: 'bookAuthor', key: 'bookAuthor', ellipsis: true },    
    { title: '购买数量', dataIndex: 'quantityBought', key: 'quantityBought', width: 100, align: 'right' },
    { title: '花费金额', dataIndex: 'totalSpentOnBook', key: 'totalSpentOnBook', render: (val) => `¥${val != null ? Number(val).toFixed(2) : '0.00'}`, width: 120, align: 'right' },
  ];

  if (authLoading && !user) return <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 'calc(100vh - 128px)' }}><Spin size="large" tip="加载用户信息..." /></div>;
  if (authError) return <Alert message="认证错误" description={authError} type="error" showIcon />;
  if (!user && !authLoading) return <Alert message="错误" description="无法加载用户信息，请重新登录。" type="error" showIcon />;

  const datePickerPresets = [
    { label: '过去7天', value: [dayjs().subtract(7, 'days'), dayjs()] },
    { label: '过去30天', value: [dayjs().subtract(30, 'days'), dayjs()] },
    { label: '本月', value: [dayjs().startOf('month'), dayjs().endOf('month')] },
    { label: '上个月', value: [dayjs().subtract(1, 'month').startOf('month'), dayjs().subtract(1, 'month').endOf('month')] },
    { label: '今年', value: [dayjs().startOf('year'), dayjs().endOf('year')] },
    { label: '所有时间', value: [dayjs('2000-01-01'), dayjs()] },
  ];

  return (
    <Row gutter={[24, 24]}>
      <Col xs={24} md={10} lg={8}>
        <Card title="账户信息">
            <div style={{ textAlign: 'center', marginBottom: 24 }}>
                <Avatar size={100} src={user?.avatarUrl ? `http://localhost:8080${user.avatarUrl}` : undefined} icon={!user?.avatarUrl && <UserOutlined />} />
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
            <Form.Item name="username" label="用户名">
              <Input readOnly />
            </Form.Item>
            <Form.Item name="email" label="邮箱">
              <Input readOnly />
            </Form.Item>
            <Form.Item
              name="phone"
              label="手机号"
              rules={[{ pattern: /^1\d{10}$/, message: '请输入有效的手机号' }]}
            >
              <Input />
            </Form.Item>
            <Form.Item name="address" label="收货地址">
              <Input.TextArea rows={3} />
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
        <Card title="我的购书统计">
            <div style={{ marginBottom: 16 }}>
                <Text strong>选择统计时间范围: </Text>
                <RangePicker 
                    value={statsDateRange} 
                    onChange={handleStatsDateRangeChange} 
                    allowClear={true}
                    style={{marginLeft: 8}}
                    presets={datePickerPresets}
                />
            </div>
            {loadingStats && <div style={{textAlign: 'center', padding: '20px'}}><Spin tip="加载统计数据中..."/></div>}
            {statsError && !loadingStats && <Alert message="加载统计出错" description={statsError} type="error" showIcon />}
            {!loadingStats && !statsError && personalBookStatsList.length > 0 && (
                <>
                    <Descriptions bordered column={1} size="small" style={{ marginBottom: 20 }}>
                        <Descriptions.Item label="总购买书籍种类">{totalUniqueBookTypes} 种</Descriptions.Item>
                        <Descriptions.Item label="总购买书籍数量">{totalBooksBought} 本</Descriptions.Item>
                        <Descriptions.Item label="总消费金额">¥{overallTotalSpent.toFixed(2)}</Descriptions.Item>
                    </Descriptions>
                    <Title level={5} style={{marginBottom: 16}}>详细购书列表</Title>
                    <Table 
                        dataSource={personalBookStatsList}
                        columns={personalStatsColumns}
                        rowKey="bookId"
                        size="small"
                        pagination={{ pageSize: 5 }}
                        scroll={{ y: 300 }}
                    />
                </>
            )}
             {!loadingStats && !statsError && !personalBookStatsList.length && statsDateRange && (
                <Empty description="选定时间范围内暂无购书记录" />
            )}
             {!loadingStats && !statsError && !personalBookStatsList.length && !statsDateRange && (
                <Empty description="请选择日期范围以查看统计数据" />
            )}
        </Card>
      </Col>
    </Row>
  );
};

export default Profile; 