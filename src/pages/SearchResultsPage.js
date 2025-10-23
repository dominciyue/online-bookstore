import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Row, Col, Card, Typography, Spin, Empty, Button, Space, Tooltip } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import bookService from '../services/bookService';

const { Title, Text } = Typography;
const { Meta } = Card;

const SearchResultsPage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const query = searchParams.get('q');

  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (query) {
      const fetchSearchResults = async () => {
        setLoading(true);
        setError(null);
        try {
          // bookService.getAllBooks can take a { title: query } argument
          const results = await bookService.getAllBooks({ title: query });
          setBooks(results && results.content ? results.content : []);
        } catch (err) {
          console.error("Error fetching search results:", err);
          setError('搜索书籍时出错，请稍后重试。');
          setBooks([]);
        } finally {
          setLoading(false);
        }
      };
      fetchSearchResults();
    } else {
      // If no query, maybe redirect or show a message to enter a search term
      setBooks([]); // Clear books if query is removed or empty
    }
  }, [query]);

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '50vh' }}>
        <Spin size="large" tip={`正在搜索 "${query}"...`} />
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ textAlign: 'center', padding: '50px' }}>
        <Text type="danger">{error}</Text>
        <br />
        <Button style={{ marginTop: '20px' }} onClick={() => navigate('/')}>返回首页</Button>
      </div>
    );
  }

  return (
    <div style={{ padding: '24px' }}>
      <Button 
        type="text" 
        icon={<ArrowLeftOutlined />} 
        onClick={() => navigate(-1)} // Go back to previous page or home
        style={{marginBottom: '16px'}}
      >
        返回
      </Button>
      <Title level={2} style={{ marginBottom: '24px' }}>
        搜索结果: <Text type="secondary">"{query}"</Text>
      </Title>

      {!query && (
        <Empty description="请输入关键词进行搜索。" />
      )}

      {query && books.length === 0 && !loading && (
        <Empty description={<span>未找到与 "{query}" 相关的书籍。</span>}>
          <Button type="primary" onClick={() => navigate('/')}>浏览所有书籍</Button>
        </Empty>
      )}

      {books.length > 0 && (
        <Row gutter={[16, 24]}>
          {books.map(book => (
            <Col xs={24} sm={12} md={8} lg={6} xl={4} key={book.id}>
              <Card
                hoverable
                cover={
                  <img 
                    alt={book.title} 
                    src={book.cover || 'https://via.placeholder.com/200x300?text=No+Image'} 
                    style={{ height: '250px', objectFit: 'contain', padding: '10px' }} 
                  />
                }
                onClick={() => navigate(`/book/${book.id}`)}
                style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between', height: '100%' }}
              >
                <Meta
                  title={<Tooltip title={book.title}>{book.title}</Tooltip>}
                  description={
                    <Space direction="vertical" style={{width: '100%'}}>
                      <Text type="secondary">{book.author}</Text>
                      <Text type="danger" style={{fontSize: '18px'}}>
                        ¥{typeof book.price === 'number' ? book.price.toFixed(2) : 'N/A'}
                      </Text>
                    </Space>
                  }
                />
              </Card>
            </Col>
          ))}
        </Row>
      )}
    </div>
  );
};

export default SearchResultsPage; 