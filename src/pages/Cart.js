import React, { useState, useEffect } from 'react';
import { Table, Button, InputNumber, Space, Typography, Empty, message, Modal, Spin, Form, Input } from 'antd';
import { DeleteOutlined, ShoppingOutlined } from '@ant-design/icons';
import { useCart } from '../data/cartContext';
import { Link, useNavigate } from 'react-router-dom';
import orderService from '../services/orderService';
import { useOrderStatus } from '../hooks/useOrderStatus';

const { Title, Text } = Typography;

const Cart = () => {
  const { cartItems, removeFromCart, updateQuantity, clearCartLocal, loadingCart } = useCart();
  const navigate = useNavigate();
  const [checkoutLoading, setCheckoutLoading] = useState(false);
  const [addressModalVisible, setAddressModalVisible] = useState(false);
  const [form] = Form.useForm();
  
  // 使用订单状态Hook
  const { getRequestStatus, isConnected } = useOrderStatus();
  const handleDelete = (bookId) => {
    removeFromCart(bookId);
  };

  const handleQuantityChange = (bookId, value) => {
    // 防止null、undefined或无效值导致商品被删除
    if (value === null || value === undefined || value < 1) {
      return; // 忽略无效值，不更新数量
    }
    updateQuantity(bookId, value);
  };

  const handleOpenAddressModal = () => {
    if (cartItems.length === 0) {
      message.warning('购物车是空的，无法结算。');
      return;
    }
    setAddressModalVisible(true);
  };

  const handleCancelAddressModal = () => {
    setAddressModalVisible(false);
    form.resetFields();
  };

  const handleConfirmCheckout = async (values) => {
    setAddressModalVisible(false);
    setCheckoutLoading(true);
    try {
      console.log('=== FRONTEND: Calling async cart order ===');
      const createdOrder = await orderService.createOrderAsync(values.shippingAddress);
      if (createdOrder && createdOrder.requestId) {
        message.success(`订单请求已提交，正在异步处理！请求ID: ${createdOrder.requestId}`);
        
        // 保存requestId用于监听订单处理结果
        const requestId = createdOrder.requestId;
        
        // 使用WebSocket监听订单处理结果
        const checkOrderStatus = () => {
          const orderStatus = getRequestStatus(requestId);
          if (orderStatus) {
            if (orderStatus.status === 'PENDING' || orderStatus.status === 'COMPLETED') {
              // 订单处理成功，清空购物车
              clearCartLocal();
              message.success('订单处理成功！');
              navigate('/orders');
            } else if (orderStatus.status === 'FAILED') {
              // 订单处理失败，不清空购物车
              message.error(`订单处理失败: ${orderStatus.message}`);
            }
          }
        };
        
        // 立即检查一次状态
        checkOrderStatus();
        
        // 设置定期检查（每2秒检查一次）
        const statusCheckInterval = setInterval(() => {
          checkOrderStatus();
        }, 2000);
        
        // 设置超时保护（15秒后停止检查）
        setTimeout(() => {
          clearInterval(statusCheckInterval);
          message.warning('订单处理超时，请手动刷新订单页面查看结果');
          navigate('/orders');
        }, 15000);
        
      } else {
        const errorMsg = createdOrder && createdOrder.message ? createdOrder.message : '提交订单请求失败。';
        message.error(errorMsg);
        console.log("Unexpected response from createOrderAsync:", createdOrder);
      }
    } catch (error) {
      console.error("Error creating async order:", error);
      message.error(`异步订单提交失败: ${error.message || '未知错误'}`);
    } finally {
      setCheckoutLoading(false);
      form.resetFields();
    }
  };

  const columns = [
    {
      title: '书名',
      dataIndex: 'title',
      key: 'title',
      render: (text, record) => (
        <Space>
          <img 
            src={record.cover || 'https://via.placeholder.com/50x50?text=No+Cover'} 
            alt={text} 
            style={{ width: 50, height: 50, objectFit: 'cover' }} 
          />
          <Link to={`/book/${record.bookId}`}>{text}</Link>
        </Space>
      ),
    },
    {
      title: '单价',
      dataIndex: 'price',
      key: 'price',
      render: (price) => `¥${typeof price === 'number' ? price.toFixed(2) : 'N/A'}`,
    },
    {
      title: '数量',
      dataIndex: 'quantity',
      key: 'quantity',
      render: (quantity, record) => (
        <InputNumber
          min={1}
          max={record.stock || 99}
          value={quantity}
          onChange={(value) => handleQuantityChange(record.bookId, value)}
        />
      ),
    },
    {
      title: '小计',
      key: 'subtotal',
      render: (_, record) => {
        const price = typeof record.price === 'number' ? record.price : 0;
        const quantity = typeof record.quantity === 'number' ? record.quantity : 0;
        return `¥${(price * quantity).toFixed(2)}`;
      },
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Button 
          type="text" 
          danger 
          icon={<DeleteOutlined />}
          onClick={() => handleDelete(record.bookId)}
        >
          删除
        </Button>
      ),
    },
  ];

  const total = cartItems.reduce((sum, item) => sum + (item.price || 0) * item.quantity, 0);
  const totalQuantity = cartItems.reduce((sum, item) => sum + item.quantity, 0);

  if (loadingCart) {
    return <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '50vh' }}><Spin size="large" tip="正在加载购物车..." /></div>;
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
        <Title level={2} style={{ margin: 0 }}>我的购物车</Title>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <div style={{ 
            width: '8px', 
            height: '8px', 
            borderRadius: '50%', 
            backgroundColor: isConnected ? '#52c41a' : '#ff4d4f' 
          }} />
          <Text type="secondary" style={{ fontSize: '12px' }}>
            {isConnected ? '实时通知已连接' : '实时通知未连接'}
          </Text>
        </div>
      </div>
      
      {cartItems.length === 0 ? (
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description={<span>购物车是空的，赶紧去选购吧！</span>}
        >
          <Link to="/">
            <Button type="primary" icon={<ShoppingOutlined />}>去购物</Button>
          </Link>
        </Empty>
      ) : (
        <>
          <Table
            columns={columns}
            dataSource={cartItems.map(item => ({ ...item, key: item.bookId }))}
            rowKey={record => record.id || record.bookId}
            pagination={false}
          />
          <div style={{ textAlign: 'right', marginTop: '24px' }}>
            <Space size="large">
              <Text strong>商品总数：</Text>
              <Text>{totalQuantity} 件</Text>
              <Text strong>总计：</Text>
              <Text type="danger" style={{ fontSize: '24px', fontWeight: 'bold' }}>
                ¥{total.toFixed(2)}
              </Text>
              <Button type="primary" size="large" onClick={handleOpenAddressModal} disabled={cartItems.length === 0} loading={checkoutLoading}>
                确认下单
              </Button>
            </Space>
          </div>
        </>
      )}
      <Modal
        title="填写收货地址"
        visible={addressModalVisible}
        onCancel={handleCancelAddressModal}
        confirmLoading={checkoutLoading}
        onOk={() => {
          form
            .validateFields()
            .then(values => {
              handleConfirmCheckout(values);
            })
            .catch(info => {
              console.log('Validate Failed:', info);
            });
        }}
        okText="确认并下单"
        cancelText="取消"
      >
        <Form form={form} layout="vertical" name="shipping_address_form">
          <Form.Item
            name="shippingAddress"
            label="收货地址"
            rules={[{ required: true, message: '请输入收货地址!' }]}
          >
            <Input.TextArea rows={4} placeholder="请输入详细的收货地址" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default Cart; 