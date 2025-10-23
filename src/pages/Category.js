import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Row, Col, Card, Typography, Space, Empty, Button, Spin } from 'antd';
import { ArrowLeftOutlined, LaptopOutlined, CodeOutlined, RobotOutlined, ReadOutlined } from '@ant-design/icons';
// import { books as localBooks } from '../data/books'; // Will fetch from API
import bookService from '../services/bookService'; // Import default export

const { Title, Text } = Typography;
const { Meta } = Card;

const Category = () => {
  const { categoryKey } = useParams(); // Get categoryKey from URL params
  const navigate = useNavigate();
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  // const [categoryName, setCategoryName] = useState(categoryKey); // No longer strictly needed if we rely on the map below

  useEffect(() => {
    const fetchBooksForCategory = async () => {
      if (!categoryKey) return;
      try {
        setLoading(true);
        // You might want a mapping from categoryKey to a displayable name if they differ
        // For now, we can just capitalize the key or use a predefined map if available
        // Example: Fetch display name if your backend/service provides it
        // const categoryDetails = await bookService.getCategoryDetails(categoryKey); 
        // setCategoryName(categoryDetails ? categoryDetails.name : categoryKey);

        const fetchedBooks = await bookService.getAllBooks({ category: categoryKey });
        setBooks(fetchedBooks && fetchedBooks.content ? fetchedBooks.content : []);
        setError(null);
      } catch (err) {
        console.error(`Failed to fetch books for category ${categoryKey}:`, err);
        setError(`无法加载分类 “${categoryKey}” 下的书籍，请稍后再试。`);
        setBooks([]);
      } finally {
        setLoading(false);
      }
    };

    fetchBooksForCategory();
  }, [categoryKey]); // Re-run effect if categoryKey changes

  // Updated map for display names, matching Home.js
  const categoryDisplayMap = {
    'cs-classic': { name: '计算机经典', icon: <LaptopOutlined /> },
    'web-dev': { name: 'Web开发', icon: <CodeOutlined /> },
    'programming-lang': { name: '编程语言', icon: <ReadOutlined /> },
    'sci-fi': { name: '科幻小说', icon: <RobotOutlined /> }
  };

  const currentCategoryDetails = categoryDisplayMap[categoryKey] || { name: categoryKey, icon: null };
  const displayCategoryName = currentCategoryDetails.name;

  if (loading) {
    return <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '50vh' }}><Spin size="large" /></div>;
  }

  if (error) {
    // Display error message, potentially using the more friendly displayCategoryName if available
    return <div style={{ textAlign: 'center', padding: '50px' }}><Text type="danger">{`无法加载分类 “${displayCategoryName}” 下的书籍，请稍后再试。`}</Text></div>;
  }

  return (
    <div style={{ padding: '0 24px' }}>
      <div style={{ margin: '16px 0' }}>
        <Button 
          type="text" 
          icon={<ArrowLeftOutlined />} 
          onClick={() => navigate(-1)} // Go back to previous page
        >
          返回
        </Button>
      </div>
      
      <Title level={2} style={{ marginBottom: '24px' }}>{displayCategoryName}</Title>

      {books.length === 0 && !loading ? (
        <Empty description={`分类 “${displayCategoryName}” 下暂无相关书籍`} />
      ) : (
        <Row gutter={[16, 16]}>
          {books.map(book => (
            <Col xs={24} sm={12} md={8} lg={6} key={book.id}>
              <Card
                hoverable
                cover={<img alt={book.title} src={book.cover || 'https://via.placeholder.com/200x300?text=No+Image'} style={{ height: '250px', objectFit: 'contain', padding:'10px' }} />}
                onClick={() => navigate(`/book/${book.id}`)}
              >
                <Meta
                  title={book.title}
                  description={
                    <Space direction="vertical" style={{width: '100%'}}>
                      <Text type="secondary">{book.author}</Text>
                      {/* <Rate disabled defaultValue={book.rating || 0} /> */}
                      <Text type="danger" style={{fontSize:'18px'}}>¥{typeof book.price === 'number' ? book.price.toFixed(2) : 'N/A'}</Text>
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

export default Category; 