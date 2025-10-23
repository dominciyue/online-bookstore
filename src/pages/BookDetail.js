import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Button, Row, Col, Typography, Space, Divider, message, Spin, Alert, Modal, Form, Input, InputNumber as AntInputNumber } from 'antd';
import { ShoppingCartOutlined, DollarOutlined, ArrowLeftOutlined } from '@ant-design/icons';
// import { books as localBooks } from '../data/books'; // To be replaced by API call
import bookService from '../services/bookService'; // Import default export
import { useCart } from '../data/cartContext'; // Keep for now, but actions might be placeholder
import orderService from '../services/orderService'; // Import orderService

const { Title, Paragraph, Text } = Typography;

const BookDetail = () => {
  const { id: bookId } = useParams();
  const navigate = useNavigate();
  const { addToCart } = useCart(); // 使用 cartContext 中的 addToCart

  const [book, setBook] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [buyNowModalVisible, setBuyNowModalVisible] = useState(false);
  const [buyNowLoading, setBuyNowLoading] = useState(false);
  const [buyNowForm] = Form.useForm();
  const [quantity, setQuantity] = useState(1); // State for quantity for Buy Now

  useEffect(() => {
    const fetchBookDetail = async () => {
      if (!bookId) return;
      try {
        setLoading(true);
        const fetchedBook = await bookService.getBookById(bookId);
        if (fetchedBook) {
          setBook(fetchedBook);
          setError(null);
        } else {
          setError('未找到该书籍。它可能已被移除或ID无效。');
          setBook(null);
        }
      } catch (err) {
        console.error(`Failed to fetch book detail for ID ${bookId}:`, err);
        setError('加载书籍详情失败，请稍后再试。');
        setBook(null);
      } finally {
        setLoading(false);
      }
    };
    fetchBookDetail();
  }, [bookId]);

  const handleAddToCart = () => {
    if (book) {
      addToCart(book, quantity); // Pass current quantity
    }
  };

  const handleOpenBuyNowModal = () => {
    if (!book) {
      message.error('书籍信息加载不完整，无法购买。');
      return;
    }
    // Reset form and quantity for modal
    buyNowForm.resetFields(); 
    setQuantity(1); // Reset quantity to 1 when modal opens
    setBuyNowModalVisible(true);
  };

  const handleCancelBuyNowModal = () => {
    setBuyNowModalVisible(false);
  };

  const handleConfirmBuyNow = async (values) => {
    if (!book) return;
    setBuyNowLoading(true);
    try {
      console.log('=== FRONTEND: Calling async single book order ===');
      // values contains shippingAddress, quantity is from state
      const createdOrder = await orderService.createSingleBookOrderAsync(book.id, quantity, values.shippingAddress);
      if (createdOrder && createdOrder.requestId) {
        message.success(`单品订单请求已提交，正在异步处理！请求ID: ${createdOrder.requestId}`);
        setBuyNowModalVisible(false);
        
        // 显示处理中的消息，然后等待几秒钟让Kafka处理完成
        message.loading('订单正在处理中，请稍候...', 3);
        
        // 等待3秒让Kafka处理完成，然后跳转到订单页面
        setTimeout(() => {
          navigate('/orders');
        }, 3000);
      } else {
        const errorMsg = createdOrder && createdOrder.message ? createdOrder.message : '提交单品订单请求失败。';
        message.error(errorMsg);
      }
    } catch (err) {
      console.error("Error in async Buy Now:", err);
      message.error(`异步单品订单提交失败: ${err.message || '未知错误'}`);
    } finally {
      setBuyNowLoading(false);
    }
  };

  if (loading) {
    return <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '50vh' }}><Spin size="large" /></div>;
  }

  if (error) {
    return (
      <div style={{ padding: '50px', textAlign: 'center' }}>
        <Alert message="错误" description={error} type="error" showIcon />
        <Button style={{ marginTop: '20px' }} onClick={() => navigate(-1)}>返回</Button>
      </div>
    );
  }

  if (!book) {
    // This case should ideally be covered by the error state if book is not found from API
    // but as a fallback:
    return <div style={{ padding: '50px', textAlign: 'center' }}>书籍信息不存在。</div>;
  }

  return (
    <div style={{padding: '24px'}}>
        <Button 
          type="text" 
          icon={<ArrowLeftOutlined />} 
          onClick={() => navigate(-1)}
          style={{marginBottom: '16px'}}
        >
          返回列表
        </Button>
      <Row gutter={[24, 24]}>
        <Col xs={24} md={8}>
          <div style={{ 
            border: '1px solid #f0f0f0', 
            borderRadius: '8px',
            padding: '20px',
            background: '#fff', // Changed from f8f8f8 to white for cleaner look
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            minHeight: '300px', // Adjusted minHeight
            boxShadow: '0 2px 8px rgba(0,0,0,0.09)'
          }}>
            <img
              src={book.cover || 'https://via.placeholder.com/300x450?text=No+Image'} // Larger placeholder
              alt={book.title}
              style={{ 
                maxWidth: '100%', 
                maxHeight: '400px', // Adjusted maxHeight
                objectFit: 'contain' // Use contain to ensure full image is visible
              }}
            />
          </div>
        </Col>
        <Col xs={24} md={16}>
          <Card bordered={false} style={{ boxShadow: '0 2px 8px rgba(0,0,0,0.09)' }}>
            <Title level={2} style={{marginTop: 0}}>{book.title}</Title>
            <Divider />
            <Space direction="vertical" size="middle" style={{ width: '100%' }}>
              <div>
                <Text strong>作者：</Text>
                <Text>{book.author || '未知'}</Text>
              </div>
              {book.publisher && (
                <div>
                  <Text strong>出版社：</Text>
                  <Text>{book.publisher}</Text>
                </div>
              )}
              {book.isbn && (
                <div>
                  <Text strong>ISBN：</Text>
                  <Text>{book.isbn}</Text>
                </div>
              )}
               <div>
                <Text strong>库存：</Text>
                <Text>{typeof book.stock === 'number' ? `${book.stock} 件` : 'N/A'}</Text>
              </div>
              <div>
                <Text strong>数量：</Text>
                <AntInputNumber min={1} max={book.stock || 99} value={quantity} onChange={setQuantity} />
              </div>
              <div>
                <Text strong>价格：</Text>
                <Text type="danger" style={{ fontSize: '24px', fontWeight: 'bold' }}>
                  ¥{typeof book.price === 'number' ? book.price.toFixed(2) : 'N/A'}
                </Text>
              </div>
              <Divider />
              {book.description && (
                <Paragraph>
                  <Text strong style={{ fontSize: '16px' }}>图书简介：</Text>
                  <br />
                  <Text style={{ lineHeight: '1.8', display:'block', marginTop:'8px' }}>{book.description}</Text>
                </Paragraph>
              )}
              <Divider />
              <Space size="large">
                <Button 
                  type="default"
                  icon={<ShoppingCartOutlined />} 
                  size="large"
                  onClick={handleAddToCart}
                  disabled={!book || book.stock === 0}
                >
                  {book && book.stock === 0 ? '库存不足' : '加入购物车'}
                </Button>
                <Button 
                  type="primary"
                  danger 
                  icon={<DollarOutlined />} 
                  size="large"
                  onClick={handleOpenBuyNowModal}
                  disabled={!book || book.stock === 0}
                >
                  {book && book.stock === 0 ? '库存不足' : '立即购买'}
                </Button>
              </Space>
            </Space>
          </Card>
        </Col>
      </Row>

      <Modal
        title={`立即购买: ${book?.title}`}
        visible={buyNowModalVisible}
        onCancel={handleCancelBuyNowModal}
        confirmLoading={buyNowLoading}
        onOk={() => {
          buyNowForm
            .validateFields()
            .then(values => {
              handleConfirmBuyNow(values);
            })
            .catch(info => {
              console.log('Address Validation Failed:', info);
            });
        }}
        okText="确认下单"
        cancelText="取消"
      >
        <Form form={buyNowForm} layout="vertical" name="buy_now_address_form">
          <Paragraph>
            您将为书籍 "{book?.title}" 下单，数量: {quantity}件。
          </Paragraph>
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

export default BookDetail; 