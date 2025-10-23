import React, { useState, useEffect, useCallback } from 'react';
import { List, Typography, Spin, Alert, Empty, Button, Tag, Space, Modal, Card, Divider, DatePicker, Input, message } from 'antd';
import { Link } from 'react-router-dom'; // For linking to user or book details if needed
import orderService from '../services/orderService';
import { EyeOutlined, FilterOutlined, ClearOutlined, UserOutlined, BookOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';

const { Title, Text } = Typography;
const { RangePicker } = DatePicker;

const AdminOrderManagement = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10, // Admin page might show more items by default
    total: 0,
  });

  const [dateRange, setDateRange] = useState(null);
  const [filterUserId, setFilterUserId] = useState(''); // For filtering by user ID
  const [bookNameSearch, setBookNameSearch] = useState(''); // State for book name search
  const [activeFilters, setActiveFilters] = useState({});

  const fetchOrders = useCallback(async (currentPagination, filters) => {
    setLoading(true);
    setError(null);
    try {
      const params = {
        page: currentPagination.current - 1,
        size: currentPagination.pageSize,
        sort: 'orderDate,desc',
      };

      if (filters.startDate) params.startDate = filters.startDate.toISOString();
      if (filters.endDate) params.endDate = filters.endDate.toISOString();
      if (filters.userId) params.userId = filters.userId;
      if (filters.bookName) params.bookName = filters.bookName; // Add bookName to params

      const response = await orderService.getAllOrdersForAdmin(params);

      if (response && response.content) {
        setOrders(response.content);
        setPagination(prev => ({
          ...prev,
          current: response.number + 1,
          pageSize: response.size,
          total: response.totalElements,
        }));
      } else {
        setOrders([]);
        setPagination(prev => ({ ...prev, total: 0, current: 1 }));
      }
    } catch (err) {
      console.error("Failed to fetch admin orders:", err);
      setError('无法加载所有订单列表，请稍后再试。');
      setOrders([]);
      setPagination(prev => ({ ...prev, total: 0, current: 1 }));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchOrders(pagination, activeFilters);
  }, [fetchOrders, pagination.current, pagination.pageSize, activeFilters]);

  const handleApplyFilters = () => {
    const newFilters = {};
    if (dateRange && dateRange[0] && dateRange[1]) {
      newFilters.startDate = dateRange[0].startOf('day');
      newFilters.endDate = dateRange[1].endOf('day');
    }
    if (filterUserId.trim()) {
        newFilters.userId = filterUserId.trim();
    }
    if (bookNameSearch.trim()) {
        newFilters.bookName = bookNameSearch.trim();
    }
    setActiveFilters(newFilters);
    setPagination(prev => ({ ...prev, current: 1 }));
  };

  const handleClearFilters = () => {
    setDateRange(null);
    setFilterUserId('');
    setBookNameSearch(''); // Clear book name search
    setActiveFilters({});
    setPagination(prev => ({ ...prev, current: 1 }));
  };

  const handleTableChange = (newPage, newPageSize) => {
    setPagination(prev => ({
      ...prev,
      current: newPage,
      pageSize: newPageSize || prev.pageSize,
    }));
  };

  const getStatusTagColor = (status) => {
    switch (status) {
      case 'PENDING': return 'orange';
      case 'CONFIRMED': case 'PAID': return 'blue';
      case 'PROCESSING': return 'purple';
      case 'SHIPPED': return 'cyan';
      case 'DELIVERED': return 'green';
      case 'CANCELLED': return 'red';
      default: return 'default';
    }
  };

  const [isModalVisible, setIsModalVisible] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState(null);

  const showOrderDetails = async (orderId) => {
    try {
      // For admin, we can fetch details without userId constraint if service allows
      const orderDetails = await orderService.getAdminOrderDetails(orderId);
      setSelectedOrder(orderDetails);
      setIsModalVisible(true);
    } catch (err) {
        message.error(`加载订单详情失败: ${err.message}`);
    }
  };

  const handleModalClose = () => {
    setIsModalVisible(false);
    setSelectedOrder(null);
  };

  if (loading && orders.length === 0) {
    return <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '50vh' }}><Spin size="large" tip="正在加载所有订单..." /></div>;
  }

  if (error && orders.length === 0) {
    return <Alert message="错误" description={error} type="error" showIcon style={{ margin: '20px' }} />;
  }

  return (
    <div style={{ padding: '24px' }}>
      <Title level={2} style={{ marginBottom: '24px' }}>订单管理 (管理员)</Title>
      
      <Card style={{ marginBottom: '24px' }}>
        <Space wrap>
          <RangePicker value={dateRange} onChange={setDateRange} allowClear />
          <Input 
            placeholder="按用户ID筛选"
            value={filterUserId}
            onChange={e => setFilterUserId(e.target.value)}
            style={{ width: 150 }}
            prefix={<UserOutlined />}
            allowClear
            onPressEnter={handleApplyFilters}
          />
          <Input
            placeholder="按书名搜索..."
            value={bookNameSearch}
            onChange={e => setBookNameSearch(e.target.value)}
            style={{ width: 200 }}
            prefix={<BookOutlined />}
            allowClear
            onPressEnter={handleApplyFilters}
          />
          <Button icon={<FilterOutlined />} type="primary" onClick={handleApplyFilters}>筛选</Button>
          <Button icon={<ClearOutlined />} onClick={handleClearFilters}>清除所有筛选</Button>
        </Space>
      </Card>

      {loading && <div style={{textAlign: 'center', margin: '20px'}}><Spin tip="更新订单列表中..."/></div>} 
      {!loading && error && <Alert message="加载部分数据时出错" description={error} type="error" showIcon closable style={{marginBottom: '16px'}}/>}

      {orders.length === 0 && !loading ? (
        <Empty description={Object.keys(activeFilters).length > 0 ? "没有找到符合筛选条件的订单。" : "系统中暂无订单记录。"} />
      ) : (
        <List
          itemLayout="vertical"
          dataSource={orders}
          loading={loading}
          pagination={{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: pagination.total,
            onChange: handleTableChange,
            onShowSizeChange: handleTableChange,
            showSizeChanger: true,
            pageSizeOptions: ['10', '20', '50'],
            showTotal: (total, range) => `${range[0]}-${range[1]} 共 ${total} 条订单`,
          }}
          renderItem={order => (
            <List.Item
              key={order.id}
              actions={[
                <Button icon={<EyeOutlined />} onClick={() => showOrderDetails(order.id)}>查看详情</Button>,
                // Admin might have other actions like "Update Status", "Cancel Order" etc. later
              ]}
              style={{ background: '#fff', padding: '20px', marginBottom:'16px', borderRadius:'8px', boxShadow: '0 2px 8px rgba(0,0,0,0.09)'}}
            >
              <List.Item.Meta
                title={<Text strong>订单号: {order.id}</Text>}
                description={
                    <Space size="middle">
                        <Text type="secondary">用户ID: {order.userId}</Text>
                        <Text type="secondary">下单时间: {dayjs(order.orderDate).format('YYYY-MM-DD HH:mm:ss')}</Text>
                    </Space>
                }
              />
              <Space direction="vertical" size="small" style={{width: '100%'}}>
                <div><Text strong>总金额: </Text><Text type="danger" style={{fontSize: '16px'}}>¥{order.totalPrice ? order.totalPrice.toFixed(2) : 'N/A'}</Text></div>
                <div><Text strong>状态: </Text><Tag color={getStatusTagColor(order.status)}>{order.status}</Tag></div>
                {order.shippingAddress && <div><Text strong>收货地址: </Text><Text>{order.shippingAddress}</Text></div>}
                <Text strong>商品 ({order.orderItems ? order.orderItems.length : 0}件):</Text>
                {order.orderItems && order.orderItems.slice(0, 2).map(item => (
                     <Text key={item.id} style={{display:'block', marginLeft: '16px'}}>
                        {item.bookTitle || `书籍ID: ${item.bookId || 'N/A'}`} x {item.quantity} (¥{item.priceAtPurchase ? item.priceAtPurchase.toFixed(2) : 'N/A'})
                     </Text>
                ))}
                {order.orderItems && order.orderItems.length > 2 && <Text style={{marginLeft: '16px'}}>...等更多商品</Text>}
              </Space>
            </List.Item>
          )}
        />
      )}
      {selectedOrder && (
        <Modal
          title={`订单详情 (订单号: ${selectedOrder.id}) - 用户ID: ${selectedOrder.userId}`}
          visible={isModalVisible}
          onCancel={handleModalClose}
          footer={[
            <Button key="close" onClick={handleModalClose}>
              关闭
            </Button>,
            // Admin might have actions here like changing order status
          ]}
          width={800}
        >
          <p><Text strong>下单时间:</Text> {dayjs(selectedOrder.orderDate).format('YYYY-MM-DD HH:mm:ss')}</p>
          <p><Text strong>订单状态:</Text> <Tag color={getStatusTagColor(selectedOrder.status)}>{selectedOrder.status}</Tag></p>
          <p><Text strong>总金额:</Text> ¥{selectedOrder.totalPrice ? selectedOrder.totalPrice.toFixed(2) : 'N/A'}</p>
          {selectedOrder.shippingAddress && <p><Text strong>收货地址:</Text> {selectedOrder.shippingAddress}</p>}
          <Divider>商品列表</Divider>
          <List
            dataSource={selectedOrder.orderItems}
            renderItem={item => (
              <List.Item key={item.id}>
                <List.Item.Meta
                  avatar={<img src={item.bookCover || 'https://via.placeholder.com/60x80?text=No+Cover'} alt={item.bookTitle} style={{width: 60, height: 80, objectFit: 'contain'}}/>}
                  title={<Link to={`/book/${item.bookId || '#'}`}>{item.bookTitle || `书籍ID: ${item.bookId || 'N/A'}`}</Link>}
                  description={`单价: ¥${item.priceAtPurchase ? item.priceAtPurchase.toFixed(2) : 'N/A'} x 数量: ${item.quantity}`}
                />
                <div>小计: ¥{(item.priceAtPurchase && item.quantity ? (item.priceAtPurchase * item.quantity).toFixed(2) : 'N/A')}</div>
              </List.Item>
            )}
          />
        </Modal>
      )}
    </div>
  );
};

export default AdminOrderManagement; 