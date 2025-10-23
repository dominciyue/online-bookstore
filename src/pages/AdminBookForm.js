import React, { useState, useEffect } from 'react';
import { Form, Input, Button, Card, Typography, message, Spin, InputNumber, Select } from 'antd';
import { SaveOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import bookService from '../services/bookService';
import { useNavigate, useParams } from 'react-router-dom';

const { Title } = Typography;
const { TextArea } = Input;
const { Option } = Select;

// Predefined categories - ideally, these might come from a config or API
const categories = [
    { key: 'cs-classic', name: '计算机经典' },
    { key: 'web-dev', name: 'Web开发' },
    { key: 'programming-lang', name: '编程语言' },
    { key: 'sci-fi', name: '科幻小说' },
    { key: 'literature', name: '文学' },
    { key: 'history', name: '历史' },
    // Add more categories as needed
];

const AdminBookForm = () => {
    const [form] = Form.useForm();
    const navigate = useNavigate();
    const { bookId } = useParams(); // For edit mode
    const isEditMode = !!bookId;

    const [loading, setLoading] = useState(false);
    const [pageLoading, setPageLoading] = useState(false); // For fetching book details in edit mode

    useEffect(() => {
        if (isEditMode) {
            setPageLoading(true);
            bookService.getBookById(bookId)
                .then(bookData => {
                    if (bookData) {
                        form.setFieldsValue(bookData);
                    } else {
                        message.error('未找到要编辑的书籍。');
                        navigate('/admin/books');
                    }
                })
                .catch(err => {
                    message.error(`加载书籍信息失败: ${err.message}`);
                    navigate('/admin/books');
                })
                .finally(() => setPageLoading(false));
        }
    }, [isEditMode, bookId, form, navigate]);

    const onFinish = async (values) => {
        setLoading(true);
        try {
            if (isEditMode) {
                await bookService.updateBook(bookId, values);
                message.success('书籍更新成功！');
            } else {
                await bookService.addBook(values);
                message.success('新书添加成功！');
            }
            navigate('/admin/books');
        } catch (err) {
            message.error(`${isEditMode ? '更新' : '添加'}书籍失败: ${err.message || '请检查输入的数据'}`);
        } finally {
            setLoading(false);
        }
    };

    if (pageLoading) {
        return <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '50vh' }}><Spin size="large" tip="加载书籍数据中..." /></div>;
    }

    return (
        <Card>
            <Title level={3} style={{ marginBottom: '24px' }}>
                <Button icon={<ArrowLeftOutlined />} type="text" onClick={() => navigate('/admin/books')} style={{ marginRight: '10px' }} />
                {isEditMode ? '编辑书籍' : '添加新书'}
            </Title>
            <Form form={form} layout="vertical" onFinish={onFinish} initialValues={{ stock: 0, price: 0.00 }}>
                <Form.Item name="title" label="书名" rules={[{ required: true, message: '请输入书名' }]}>
                    <Input placeholder="例如: 深入理解计算机系统" />
                </Form.Item>
                <Form.Item name="author" label="作者" rules={[{ required: true, message: '请输入作者' }]}>
                    <Input placeholder="例如: Randal E. Bryant" />
                </Form.Item>
                <Form.Item name="isbn" label="ISBN" rules={[{ required: true, message: '请输入ISBN' }, { len: 13, message: 'ISBN必须为13位数字' }, { pattern: /^[0-9]+$/, message: 'ISBN只能包含数字' }]}>
                    <Input placeholder="例如: 9787111512815 (13位数字)" />
                </Form.Item>
                <Form.Item name="publisher" label="出版社" rules={[{ required: true, message: '请输入出版社' }]}>
                    <Input placeholder="例如: 机械工业出版社" />
                </Form.Item>
                <Form.Item name="price" label="价格" rules={[{ required: true, message: '请输入价格' }, { type: 'number', min: 0, message: '价格不能为负'}]}>
                    <InputNumber style={{ width: '100%' }} precision={2} step={0.01} placeholder="例如: 89.00" />
                </Form.Item>
                <Form.Item name="stock" label="库存" rules={[{ required: true, message: '请输入库存量' }, { type: 'integer', min: 0, message: '库存不能为负'}]}>
                    <InputNumber style={{ width: '100%' }} placeholder="例如: 100" />
                </Form.Item>
                <Form.Item name="category" label="分类" rules={[{ required: true, message: '请选择分类' }]}>
                    <Select placeholder="选择一个分类">
                        {categories.map(cat => (
                            <Option key={cat.key} value={cat.key}>{cat.name}</Option>
                        ))}
                    </Select>
                </Form.Item>
                <Form.Item name="cover" label="封面图片URL" rules={[{ message: '请输入有效的URL' }]}>
                    <Input placeholder="例如: /images/book-cover.jpg 或 https://example.com/cover.png" />
                </Form.Item>
                <Form.Item name="description" label="描述">
                    <TextArea rows={4} placeholder="输入书籍的详细描述" />
                </Form.Item>
                <Form.Item>
                    <Button type="primary" htmlType="submit" icon={<SaveOutlined />} loading={loading} style={{ width: '100%' }}>
                        {loading ? '处理中...' : (isEditMode ? '保存更改' : '添加书籍')}
                    </Button>
                </Form.Item>
            </Form>
        </Card>
    );
};

export default AdminBookForm; 