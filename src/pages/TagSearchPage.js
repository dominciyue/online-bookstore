import React, { useState } from 'react';
import { Row, Col, Card, Typography, Spin, Empty, Button, message, Pagination } from 'antd';
import { useNavigate } from 'react-router-dom';
import { ArrowLeftOutlined, ShoppingCartOutlined } from '@ant-design/icons';
import TagSearchPanel from '../components/TagSearchPanel';
import bookService from '../services/bookService';
import { useCart } from '../data/cartContext';

const { Title, Text } = Typography;
const { Meta } = Card;

/**
 * 标签搜索页面
 * 提供基于Neo4j标签图的智能搜索功能
 */
const TagSearchPage = () => {
  const navigate = useNavigate();
  const { addToCart } = useCart();

  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [searchedTags, setSearchedTags] = useState([]);
  
  // 分页状态
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(12);
  const [totalElements, setTotalElements] = useState(0);

  // 处理标签搜索
  const handleTagSearch = async (tags, page = 0) => {
    setLoading(true);
    setError(null);
    setSearchedTags(tags);

    try {
      const response = await bookService.searchBooksByTags(tags, {
        page: page,
        size: pageSize,
        sort: 'id,asc'
      });

      setBooks(response.content || []);
      setTotalElements(response.totalElements || 0);
      setCurrentPage(page);

      if (!response.content || response.content.length === 0) {
        message.info('未找到符合条件的图书');
      } else {
        message.success(`找到 ${response.totalElements} 本相关图书`);
      }
    } catch (err) {
      console.error('标签搜索出错:', err);
      setError('搜索图书时出错，请稍后重试');
      message.error('搜索失败，请稍后重试');
      setBooks([]);
    } finally {
      setLoading(false);
    }
  };

  // 处理分页变化
  const handlePageChange = (page) => {
    handleTagSearch(searchedTags, page - 1); // Ant Design Pagination从1开始，后端从0开始
  };

  // 添加到购物车
  const handleAddToCart = (book) => {
    addToCart(book);
    message.success(`《${book.title}》已添加到购物车`);
  };

  // 查看图书详情
  const handleViewDetail = (bookId) => {
    navigate(`/book/${bookId}`); // 修复：路由为/book/:id（无s）
  };

  // 返回首页
  const handleBackToHome = () => {
    navigate('/');
  };

  return (
    <div style={{ padding: '24px', maxWidth: '1400px', margin: '0 auto' }}>
      {/* 页面标题 */}
      <div style={{ marginBottom: 24 }}>
        <Button 
          icon={<ArrowLeftOutlined />} 
          onClick={handleBackToHome}
          style={{ marginBottom: 16 }}
        >
          返回首页
        </Button>
        <Title level={2}>智能标签搜索</Title>
        <Text type="secondary">
          通过标签分类快速找到您需要的图书，系统将自动查找相关分类的图书
        </Text>
      </div>

      <Row gutter={[24, 24]}>
        {/* 左侧：标签搜索面板 */}
        <Col xs={24} sm={24} md={8} lg={6}>
          <TagSearchPanel onSearch={(tags) => handleTagSearch(tags, 0)} />
        </Col>

        {/* 右侧：搜索结果 */}
        <Col xs={24} sm={24} md={16} lg={18}>
          {loading ? (
            <div style={{ 
              display: 'flex', 
              justifyContent: 'center', 
              alignItems: 'center', 
              minHeight: '400px' 
            }}>
              <Spin size="large" tip="搜索中..." />
            </div>
          ) : error ? (
            <div style={{ textAlign: 'center', padding: '50px' }}>
              <Text type="danger">{error}</Text>
            </div>
          ) : books.length === 0 ? (
            <Empty
              description={
                searchedTags.length > 0 
                  ? "未找到符合条件的图书" 
                  : "请在左侧选择标签开始搜索"
              }
              style={{ marginTop: '100px' }}
            />
          ) : (
            <>
              {/* 搜索结果统计 */}
              <div style={{ marginBottom: 16 }}>
                <Text strong>搜索结果：</Text>
                <Text type="secondary"> 共找到 {totalElements} 本图书</Text>
              </div>

              {/* 图书列表 */}
              <Row gutter={[16, 16]}>
                {books.map((book) => (
                  <Col xs={24} sm={12} md={12} lg={8} key={book.id}>
                    <Card
                      hoverable
                      cover={
                        <div 
                          style={{ 
                            height: 280, 
                            overflow: 'hidden',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            backgroundColor: '#f5f5f5'
                          }}
                        >
                          <img
                            alt={book.title}
                            src={book.cover}
                            style={{ 
                              maxHeight: '100%', 
                              maxWidth: '100%',
                              objectFit: 'contain'
                            }}
                            onError={(e) => {
                              e.target.src = '/images/default-book.jpg';
                            }}
                          />
                        </div>
                      }
                      actions={[
                        <Button 
                          type="link" 
                          onClick={() => handleViewDetail(book.id)}
                        >
                          查看详情
                        </Button>,
                        <Button 
                          type="link" 
                          icon={<ShoppingCartOutlined />}
                          onClick={() => handleAddToCart(book)}
                          disabled={book.stock <= 0}
                        >
                          {book.stock > 0 ? '加入购物车' : '缺货'}
                        </Button>,
                      ]}
                    >
                      <Meta
                        title={
                          <div style={{ 
                            overflow: 'hidden', 
                            textOverflow: 'ellipsis', 
                            whiteSpace: 'nowrap' 
                          }}>
                            {book.title}
                          </div>
                        }
                        description={
                          <div>
                            <div style={{ color: '#666', marginBottom: 8 }}>
                              {book.author}
                            </div>
                            <div style={{ 
                              color: '#ff4d4f', 
                              fontSize: '18px', 
                              fontWeight: 'bold' 
                            }}>
                              ¥{book.price}
                            </div>
                            <div style={{ 
                              color: '#999', 
                              fontSize: '12px',
                              marginTop: 8 
                            }}>
                              库存：{book.stock}
                            </div>
                            {/* 显示图书标签 */}
                            {book.tags && book.tags.length > 0 && (
                              <div style={{ marginTop: 8 }}>
                                {book.tags.slice(0, 3).map((tag, index) => (
                                  <span
                                    key={index}
                                    style={{
                                      display: 'inline-block',
                                      padding: '2px 8px',
                                      margin: '2px',
                                      backgroundColor: '#e6f7ff',
                                      border: '1px solid #91d5ff',
                                      borderRadius: '2px',
                                      fontSize: '11px',
                                      color: '#0050b3'
                                    }}
                                  >
                                    {tag}
                                  </span>
                                ))}
                              </div>
                            )}
                          </div>
                        }
                      />
                    </Card>
                  </Col>
                ))}
              </Row>

              {/* 分页 */}
              {totalElements > pageSize && (
                <div style={{ textAlign: 'center', marginTop: 32 }}>
                  <Pagination
                    current={currentPage + 1}
                    total={totalElements}
                    pageSize={pageSize}
                    onChange={handlePageChange}
                    showSizeChanger={false}
                    showTotal={(total) => `共 ${total} 本图书`}
                  />
                </div>
              )}
            </>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default TagSearchPage;

