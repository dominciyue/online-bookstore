import React, { useState, useEffect, useCallback } from 'react';
import { List, Typography, Spin, Alert, Empty, Button, Tag, Space, Modal, Card, Divider, DatePicker, Input, Badge } from 'antd';
import { Link, useNavigate } from 'react-router-dom';
import orderService from '../services/orderService';
import { EyeOutlined, ShoppingCartOutlined, FilterOutlined, ClearOutlined, BookOutlined, WifiOutlined, DisconnectOutlined } from '@ant-design/icons';
import dayjs from 'dayjs'; // 用于格式化日期
import { useOrderStatus } from '../hooks/useOrderStatus';

const { Title, Text } = Typography;
const { RangePicker } = DatePicker;

const OrderList = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  
  // 订单状态管理（使用Ajax轮询）
  const { orderUpdates, getOrderStatus } = useOrderStatus();

  // Pagination state
  const [pagination, setPagination] = useState({
    current: 1, // Antd List current page is 1-indexed
    pageSize: 5, // Default page size for orders, can be adjusted
    total: 0,    // Total number of orders
  });

  // Filter state
  const [dateRange, setDateRange] = useState(null); // [startDate, endDate] using dayjs objects
  const [bookNameSearch, setBookNameSearch] = useState(''); // State for book name search
  const [activeFilters, setActiveFilters] = useState({}); // To store current applied filters for fetchOrders

  const fetchOrders = useCallback(async (currentPagination, filters) => {
    setLoading(true);
    setError(null);
    try {
      const params = {
        page: currentPagination.current - 1, // Spring Pageable is 0-indexed
        size: currentPagination.pageSize,
        sort: 'orderDate,desc', // Default sort
      };

      if (filters.startDate) params.startDate = filters.startDate.toISOString();
      if (filters.endDate) params.endDate = filters.endDate.toISOString();
      if (filters.bookName) params.bookName = filters.bookName; // Add bookName to params

      const response = await orderService.getOrders(params);

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
      console.error("Failed to fetch orders:", err);
      setError('无法加载订单列表，请稍后再试。');
      setOrders([]);
      setPagination(prev => ({ ...prev, total: 0, current: 1 }));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchOrders(pagination, activeFilters);
  }, [fetchOrders, pagination.current, pagination.pageSize, activeFilters]); // Re-fetch when pagination or activeFilters change

  // 添加自动刷新机制 - 每30秒刷新一次订单列表（减少频率，因为有了WebSocket）
  useEffect(() => {
    const interval = setInterval(() => {
      // 只在没有加载状态时刷新，避免重复请求
      if (!loading) {
        fetchOrders(pagination, activeFilters);
      }
    }, 30000); // 30秒刷新一次

    return () => clearInterval(interval);
  }, [fetchOrders, pagination, activeFilters, loading]);

  // 处理WebSocket订单状态更新
  useEffect(() => {
    if (orderUpdates.length > 0) {
      // 当收到新的订单状态更新时，刷新订单列表
      fetchOrders(pagination, activeFilters);
    }
  }, [orderUpdates, fetchOrders, pagination, activeFilters]);

  const handleApplyFilters = () => {
    const newFilters = {};
    if (dateRange && dateRange[0] && dateRange[1]) {
      newFilters.startDate = dateRange[0].startOf('day'); // Start of selected day
      newFilters.endDate = dateRange[1].endOf('day');     // End of selected day
    }
    if (bookNameSearch.trim()) {
        newFilters.bookName = bookNameSearch.trim();
    }
    setActiveFilters(newFilters);
    setPagination(prev => ({ ...prev, current: 1 })); // Reset to first page when filters change
  };

  const handleClearFilters = () => {
    setDateRange(null);
    setBookNameSearch(''); // Clear book name search
    setActiveFilters({});
    setPagination(prev => ({ ...prev, current: 1 })); // Reset to first page
  };

  const handleTableChange = (newPage, newPageSize) => {
    // This is called by the List pagination component
    // fetchOrders will be triggered by useEffect due to pagination state change
    setPagination(prev => ({
        ...prev,
        current: newPage,
        pageSize: newPageSize || prev.pageSize // onShowSizeChange might not pass newPage for current
    }));
  };

  const getStatusTagColor = (status) => {
    switch (status) {
      case 'PENDING': return 'orange';
      case 'PAID':
      case 'CONFIRMED': return 'blue';
      case 'PROCESSING': return 'purple';
      case 'SHIPPED': return 'cyan';
      case 'DELIVERED': return 'green';
      case 'CANCELLED': return 'red';
      default: return 'default';
    }
  };
  
  // 订单详情模态框相关
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState(null);

  const showOrderDetails = (order) => {
    setSelectedOrder(order);
    setIsModalVisible(true);
  };

  const handleModalClose = () => {
    setIsModalVisible(false);
    setSelectedOrder(null);
  };


  if (loading && orders.length === 0) { // Show main loading spinner only on initial load or when no data is present
    return <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '50vh' }}><Spin size="large" tip="正在加载订单..." /></div>;
  }

  if (error && orders.length === 0) { // Show main error only if no data could be loaded
    return <Alert message="错误" description={error} type="error" showIcon style={{ margin: '20px' }} />;
  }

  return (
    <div style={{ padding: '24px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <Title level={2} style={{ margin: 0 }}>我的订单</Title>
        <Space>
          <Badge 
            status="success" 
            text="自动刷新"
          />
          <WifiOutlined style={{ color: '#52c41a' }} />
        </Space>
      </div>
      
      <Card style={{ marginBottom: '24px' }}>
        <Space wrap>
          <RangePicker 
            value={dateRange} 
            onChange={setDateRange} 
            style={{ marginRight: 8 }}
            allowClear
          />
          <Input
            placeholder="按书名搜索..."
            value={bookNameSearch}
            onChange={e => setBookNameSearch(e.target.value)}
            style={{ width: 200, marginRight: 8 }}
            prefix={<BookOutlined />}
            allowClear
            onPressEnter={handleApplyFilters} // Allow Enter key to trigger search
          />
          <Button icon={<FilterOutlined />} type="primary" onClick={handleApplyFilters}>筛选</Button>
          <Button icon={<ClearOutlined />} onClick={handleClearFilters}>清除筛选</Button>
        </Space>
      </Card>

      {loading && <div style={{textAlign: 'center', margin: '20px'}}><Spin tip="更新订单列表中..."/></div>} 
      {!loading && error && <Alert message="加载部分数据时出错" description={error} type_error showIcon closable style={{marginBottom: '16px'}}/>}

      {orders.length === 0 && !loading ? (
        <Empty description={Object.keys(activeFilters).length > 0 ? "没有找到符合筛选条件的订单。" : "您目前还没有订单。"}>
          {Object.keys(activeFilters).length === 0 && (
            <Button type="primary" icon={<ShoppingCartOutlined />} onClick={() => navigate('/')}>
              去购物
            </Button>
          )}
        </Empty>
      ) : (
        <List
          itemLayout="vertical"
          dataSource={orders}
          loading={loading} // Use loading state from fetchOrders
          pagination={{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: pagination.total,
            onChange: handleTableChange,
            onShowSizeChange: handleTableChange, // Antd calls onChange with (page, pageSize) for size changes
            showSizeChanger: true,
            pageSizeOptions: ['5', '10', '20'],
            showTotal: (total, range) => `${range[0]}-${range[1]} 共 ${total} 条订单`,
          }}
          renderItem={order => {
            // 获取实时订单状态
            const realtimeStatus = getOrderStatus(order.id);
            const displayOrder = realtimeStatus ? { ...order, ...realtimeStatus } : order;
            
            return (
              <List.Item
                key={order.id}
                actions={[
                  <Button icon={<EyeOutlined />} onClick={() => showOrderDetails(displayOrder)}>查看详情</Button>,
                  // 根据订单状态可以有其他操作，例如 "再次购买", "申请售后" 等
                ]}
                style={{ 
                  background: '#fff', 
                  padding: '20px', 
                  marginBottom:'16px', 
                  borderRadius:'8px', 
                  boxShadow: '0 2px 8px rgba(0,0,0,0.09)',
                  border: realtimeStatus ? '2px solid #52c41a' : '1px solid #d9d9d9'
                }}
              >
              <List.Item.Meta
                title={<Text strong>订单号: {order.id}</Text>}
                description={<Text type="secondary">下单时间: {dayjs(order.orderDate).format('YYYY-MM-DD HH:mm:ss')}</Text>}
              />
              <Space direction="vertical" size="small" style={{width: '100%'}}>
                <div><Text strong>总金额: </Text><Text type="danger" style={{fontSize: '16px'}}>¥{displayOrder.totalPrice ? displayOrder.totalPrice.toFixed(2) : 'N/A'}</Text></div>
                <div>
                  <Text strong>状态: </Text>
                  <Tag color={getStatusTagColor(displayOrder.status)}>{displayOrder.status}</Tag>
                  {realtimeStatus && (
                    <Tag color="green" style={{ marginLeft: '8px' }}>
                      实时更新
                    </Tag>
                  )}
                </div>
                {order.shippingAddress && <div><Text strong>收货地址: </Text><Text>{order.shippingAddress}</Text></div>}
                 {/* 简单展示前几个商品 */}
                <Text strong>商品 ({order.orderItems ? order.orderItems.length : 0}件):</Text>
                {order.orderItems && order.orderItems.slice(0, 2).map(item => (
                     <Text key={item.id} style={{display:'block', marginLeft: '16px'}}>
                        {item.bookTitle || `书籍ID: ${item.bookId || 'N/A'}`} x {item.quantity} (¥{item.priceAtPurchase ? item.priceAtPurchase.toFixed(2) : 'N/A'})
                     </Text>
                ))}
                {order.orderItems && order.orderItems.length > 2 && <Text style={{marginLeft: '16px'}}>...等更多商品</Text>}
                {realtimeStatus && realtimeStatus.message && (
                  <div style={{ marginTop: '8px', padding: '8px', background: '#f6ffed', border: '1px solid #b7eb8f', borderRadius: '4px' }}>
                    <Text type="success" style={{ fontSize: '12px' }}>
                      {realtimeStatus.message}
                    </Text>
                  </div>
                )}
              </Space>
            </List.Item>
            );
          }}
        />
      )}
      {selectedOrder && (
        <Modal
          title={`订单详情 (订单号: ${selectedOrder.id})`}
          visible={isModalVisible}
          onCancel={handleModalClose}
          footer={[
            <Button key="close" onClick={handleModalClose}>
              关闭
            </Button>,
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

export default OrderList; 