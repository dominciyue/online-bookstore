import React, { useState, useEffect } from 'react';
import { Row, Col, Card, Typography, Carousel, Space, Spin } from 'antd';
import { useNavigate } from 'react-router-dom';
import bookService from '../services/bookService';
import { FireOutlined, StarOutlined, TagsOutlined, CodeOutlined, RobotOutlined, LaptopOutlined, ReadOutlined } from '@ant-design/icons';

const { Title, Text } = Typography;
const { Meta } = Card;

const Home = () => {
  const navigate = useNavigate();
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchBooks = async () => {
      try {
        setLoading(true);
        // Fetch books for home page, we might want a specific endpoint or params for non-paginated popular/new books
        // For now, let's assume getAllBooks with no params or specific params fetches what we need for Home.
        // If getAllBooks returns a Page object, we need its content.
        const response = await bookService.getAllBooks({ size: 8, sort: 'id,desc' }); // Example: Get 8 newest books
        if (response && response.content) {
            setBooks(response.content);
        } else if (Array.isArray(response)) { // Fallback if it directly returns an array (older version)
            setBooks(response);
        } else {
            setBooks([]); // Default to empty array if unexpected response
        }
        setError(null);
      } catch (err) {
        console.error("Failed to fetch books for Home:", err);
        setError("无法加载书籍列表，请稍后再试。");
        setBooks([]);
      } finally {
        setLoading(false);
      }
    };
    fetchBooks();
  }, []);

  // Ensure books is always an array before trying to iterate/sort
  const safeBooks = Array.isArray(books) ? books : [];

  const hotBooks = [...safeBooks].sort((a, b) => (b.stock || 0) - (a.stock || 0)).slice(0, 4); // Example: sort by stock for "hot"
  const newBooks = [...safeBooks].sort((a, b) => b.id - a.id).slice(0, 4); // Sort by ID for "new"

  const categories = [
    { name: '计算机经典', key: 'cs-classic', icon: <LaptopOutlined /> },
    { name: 'Web开发', key: 'web-dev', icon: <CodeOutlined /> },
    { name: '编程语言', key: 'programming-lang', icon: <ReadOutlined /> },
    { name: '科幻小说', key: 'sci-fi', icon: <RobotOutlined /> }
  ];

  if (loading) {
    return <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '50vh' }}><Spin size="large" /></div>;
  }

  if (error) {
    return <div style={{ textAlign: 'center', padding: '50px' }}><Text type="danger">{error}</Text></div>;
  }

  return (
    <div style={{ padding: '0 24px' }}>
      <Carousel autoplay style={{ marginBottom: '32px' }}>
        {[1, 2].map(item => (
          <div key={item}>
            <img 
              src={`https://picsum.photos/seed/${item}/1200/400`} 
              alt={`banner${item}`} 
              style={{ width: '100%', height: '300px', objectFit: 'cover' }} 
            />
          </div>
        ))}
      </Carousel>

      {categories.length > 0 && (
        <div style={{ marginBottom: '32px' }}>
          <Title level={3} style={{ marginBottom: '16px' }}>
            <TagsOutlined /> 图书分类
          </Title>
          <Row gutter={[16, 16]}>
            {categories.map((category) => (
              <Col xs={12} sm={8} md={6} lg={6} key={category.key}>
                <Card 
                  hoverable 
                  style={{ textAlign: 'center' }}
                  onClick={() => navigate(`/category/${category.key}`)}
                >
                  <div style={{ fontSize: '32px', marginBottom: '8px' }}>{category.icon}</div>
                  <Text strong>{category.name}</Text>
                </Card>
              </Col>
            ))}
          </Row>
        </div>
      )}

      {hotBooks.length > 0 && (
        <div style={{ marginBottom: '32px' }}>
          <Title level={3} style={{ marginBottom: '16px' }}><FireOutlined /> 热门推荐</Title>
          <Row gutter={[16, 16]}>
            {hotBooks.map(book => (
              <Col xs={24} sm={12} md={8} lg={6} key={book.id}>
                <Card
                  hoverable
                  cover={<img alt={book.title} src={book.cover || 'https://via.placeholder.com/200x300?text=No+Image'} style={{ height: '250px', objectFit: 'contain', padding: '10px' }} />}
                  onClick={() => navigate(`/book/${book.id}`)}
                >
                  <Meta
                    title={book.title}
                    description={
                      <Space direction="vertical" style={{width: '100%'}}>
                        <Text type="secondary">{book.author}</Text>
                        <Text type="danger" style={{fontSize: '18px'}}>¥{typeof book.price === 'number' ? book.price.toFixed(2) : 'N/A'}</Text>
                      </Space>
                    }
                  />
                </Card>
              </Col>
            ))}
          </Row>
        </div>
      )}

      {newBooks.length > 0 && (
         <div style={{ marginBottom: '32px' }}>
          <Title level={3} style={{ marginBottom: '16px' }}><StarOutlined /> 新书上架</Title>
          <Row gutter={[16, 16]}>
            {newBooks.map(book => (
              <Col xs={24} sm={12} md={8} lg={6} key={book.id}>
                <Card
                  hoverable
                  cover={<img alt={book.title} src={book.cover || 'https://via.placeholder.com/200x300?text=No+Image'} style={{ height: '250px', objectFit: 'contain', padding: '10px' }} />}
                  onClick={() => navigate(`/book/${book.id}`)}
                >
                  <Meta
                    title={book.title}
                    description={
                      <Space direction="vertical" style={{width: '100%'}}>
                        <Text type="secondary">{book.author}</Text>
                        <Text type="danger" style={{fontSize: '18px'}}>¥{typeof book.price === 'number' ? book.price.toFixed(2) : 'N/A'}</Text>
                      </Space>
                    }
                  />
                </Card>
              </Col>
            ))}
          </Row>
        </div>
      )}
    </div>
  );
};

export default Home; 