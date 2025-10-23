import React, { useState, useEffect, useCallback } from 'react';
import { Table, Spin, Alert, Typography, Button, Space, message, Input, Switch, Tabs, Tag, Modal } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined, RestoreOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import bookService from '../services/bookService';
import { useNavigate } from 'react-router-dom'; // For navigation, e.g., to add/edit page

const { Title } = Typography;
const { TabPane } = Tabs;
const { confirm } = Modal;

const AdminBookManagement = () => {
    const [books, setBooks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchTerm, setSearchTerm] = useState(''); // State for search term
    const [activeTab, setActiveTab] = useState('active'); // 'active', 'all', 'deleted'
    const navigate = useNavigate();

    // Pagination state
    const [pagination, setPagination] = useState({
        current: 1, // Antd Table current page is 1-indexed
        pageSize: 10, // Default page size
        total: 0, // Total number of books
    });

    const fetchBooks = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);

            const params = {
                page: pagination.current - 1, // Convert to 0-indexed for backend
                size: pagination.pageSize,
                sort: 'id,asc', // Default sorting
            };

            if (searchTerm.trim()) {
                params.title = searchTerm.trim();
            }

            let response;
            if (activeTab === 'all') {
                response = await bookService.getAllBooksForAdmin(params);
            } else if (activeTab === 'deleted') {
                response = await bookService.getDeletedBooks(params);
            } else {
                // activeTab === 'active', default behavior
                response = await bookService.getAllBooks(params);
            }

            if (response && response.content) {
                setBooks(response.content);
                setPagination(prevPagination => ({
                    ...prevPagination,
                    total: response.totalElements, // Total count from backend
                }));
            } else {
                message.error('获取书籍列表时响应格式不正确');
            }
        } catch (error) {
            console.error('Error fetching books:', error);
            setError('获取书籍列表时出错。请重试。');
        } finally {
            setLoading(false);
        }
    }, [pagination.current, pagination.pageSize, searchTerm, activeTab]);

    useEffect(() => {
        fetchBooks();
    }, [fetchBooks]);

    const handleTableChange = (newPagination, filters, sorter) => {
        setPagination({
            current: newPagination.current,
            pageSize: newPagination.pageSize,
            total: pagination.total, // Keep existing total until new data arrives
        });
    };

    const handleSearch = (value) => {
        setSearchTerm(value);
        setPagination(prevPagination => ({
            ...prevPagination,
            current: 1, // Reset to first page when searching
        }));
    };

    const handleAddBook = () => {
        navigate('/admin/books/add');
    };

    const handleEditBook = (bookId) => {
        // Navigate to edit book page
        navigate(`/admin/books/edit/${bookId}`);
    };

    const handleSoftDeleteBook = async (bookId, bookTitle) => {
        confirm({
            title: '确认删除',
            icon: <ExclamationCircleOutlined />,
            content: `您确定要删除书籍"${bookTitle}"吗？这是软删除，书籍可以恢复。`,
            okText: '确认删除',
            okType: 'danger',
            cancelText: '取消',
            onOk: async () => {
                try {
                    await bookService.deleteBook(bookId);
                    message.success('书籍已成功删除（软删除）');
                    fetchBooks(); // Refresh the list
                } catch (error) {
                    console.error('Error deleting book:', error);
                    message.error(error.message || '删除书籍时出错');
                }
            },
        });
    };

    const handleRestoreBook = async (bookId, bookTitle) => {
        confirm({
            title: '确认恢复',
            icon: <ReloadOutlined />,
            content: `您确定要恢复书籍"${bookTitle}"吗？`,
            okText: '确认恢复',
            cancelText: '取消',
            onOk: async () => {
                try {
                    await bookService.restoreBook(bookId);
                    message.success('书籍已成功恢复');
                    fetchBooks(); // Refresh the list
                } catch (error) {
                    console.error('Error restoring book:', error);
                    message.error(error.message || '恢复书籍时出错');
                }
            },
        });
    };

    const handleHardDeleteBook = async (bookId, bookTitle) => {
        confirm({
            title: '确认永久删除',
            icon: <ExclamationCircleOutlined />,
            content: (
                <div>
                    <p>您确定要<strong>永久删除</strong>书籍"{bookTitle}"吗？</p>
                    <p style={{ color: 'red', fontWeight: 'bold' }}>警告：此操作不可逆转！</p>
                </div>
            ),
            okText: '永久删除',
            okType: 'danger',
            cancelText: '取消',
            onOk: async () => {
                try {
                    await bookService.hardDeleteBook(bookId);
                    message.success('书籍已永久删除');
                    fetchBooks(); // Refresh the list
                } catch (error) {
                    console.error('Error hard deleting book:', error);
                    message.error(error.message || '永久删除书籍时出错');
                }
            },
        });
    };

    const handleTabChange = (key) => {
        setActiveTab(key);
        setPagination(prevPagination => ({
            ...prevPagination,
            current: 1, // Reset to first page when changing tabs
        }));
    };

    const columns = [
        {
            title: 'ID',
            dataIndex: 'id',
            key: 'id',
            width: 80,
        },
        {
            title: '标题',
            dataIndex: 'title',
            key: 'title',
            ellipsis: true,
        },
        {
            title: '作者',
            dataIndex: 'author',
            key: 'author',
            width: 150,
        },
        {
            title: 'ISBN',
            dataIndex: 'isbn',
            key: 'isbn',
            width: 150,
        },
        {
            title: '分类',
            dataIndex: 'category',
            key: 'category',
            width: 120,
        },
        {
            title: '价格',
            dataIndex: 'price',
            key: 'price',
            width: 100,
            render: (price) => `¥${price}`,
        },
        {
            title: '库存',
            dataIndex: 'stock',
            key: 'stock',
            width: 80,
        },
        {
            title: '状态',
            dataIndex: 'deleted',
            key: 'deleted',
            width: 100,
            render: (deleted) => (
                <Tag color={deleted ? 'red' : 'green'}>
                    {deleted ? '已删除' : '正常'}
                </Tag>
            ),
        },
        {
            title: '创建时间',
            dataIndex: 'createdAt',
            key: 'createdAt',
            width: 180,
            render: (date) => date ? new Date(date).toLocaleString('zh-CN') : '-',
        },
        {
            title: '删除时间',
            dataIndex: 'deletedAt',
            key: 'deletedAt',
            width: 180,
            render: (date) => date ? new Date(date).toLocaleString('zh-CN') : '-',
        },
        {
            title: '操作',
            key: 'actions',
            width: 200,
            render: (text, record) => (
                <Space size="small">
                    {!record.deleted && (
                        <>
                            <Button
                                type="link"
                                icon={<EditOutlined />}
                                onClick={() => handleEditBook(record.id)}
                                size="small"
                            >
                                编辑
                            </Button>
                            <Button
                                type="link"
                                danger
                                icon={<DeleteOutlined />}
                                onClick={() => handleSoftDeleteBook(record.id, record.title)}
                                size="small"
                            >
                                删除
                            </Button>
                        </>
                    )}
                    {record.deleted && (
                        <>
                            <Button
                                type="link"
                                icon={<ReloadOutlined />}
                                onClick={() => handleRestoreBook(record.id, record.title)}
                                size="small"
                            >
                                恢复
                            </Button>
                            {activeTab === 'deleted' && (
                                <Button
                                    type="link"
                                    danger
                                    icon={<DeleteOutlined />}
                                    onClick={() => handleHardDeleteBook(record.id, record.title)}
                                    size="small"
                                >
                                    永久删除
                                </Button>
                            )}
                        </>
                    )}
                </Space>
            ),
        },
    ];

    if (error) {
        return <Alert message="错误" description={error} type="error" showIcon />;
    }

    return (
        <div style={{ padding: '24px' }}>
            <div style={{ marginBottom: '16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Title level={2}>书籍管理</Title>
                <Button type="primary" icon={<PlusOutlined />} onClick={handleAddBook}>
                    添加书籍
                </Button>
            </div>

            <div style={{ marginBottom: '16px' }}>
                <Input.Search
                    placeholder="搜索书籍标题..."
                    allowClear
                    enterButton="搜索"
                    style={{ width: 300 }}
                    onSearch={handleSearch}
                    onChange={(e) => {
                        if (e.target.value === '') {
                            handleSearch(''); // Clear search when input is empty
                        }
                    }}
                />
            </div>

            <Tabs activeKey={activeTab} onChange={handleTabChange}>
                <TabPane tab="活跃书籍" key="active">
                    <Table
                        columns={columns}
                        dataSource={books}
                        rowKey="id"
                        loading={loading}
                        pagination={{
                            current: pagination.current,
                            pageSize: pagination.pageSize,
                            total: pagination.total,
                            showSizeChanger: true,
                            showQuickJumper: true,
                            showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
                        }}
                        onChange={handleTableChange}
                        scroll={{ x: 1200 }}
                    />
                </TabPane>
                <TabPane tab="所有书籍" key="all">
                    <Table
                        columns={columns}
                        dataSource={books}
                        rowKey="id"
                        loading={loading}
                        pagination={{
                            current: pagination.current,
                            pageSize: pagination.pageSize,
                            total: pagination.total,
                            showSizeChanger: true,
                            showQuickJumper: true,
                            showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
                        }}
                        onChange={handleTableChange}
                        scroll={{ x: 1200 }}
                    />
                </TabPane>
                <TabPane tab="已删除书籍" key="deleted">
                    <Table
                        columns={columns}
                        dataSource={books}
                        rowKey="id"
                        loading={loading}
                        pagination={{
                            current: pagination.current,
                            pageSize: pagination.pageSize,
                            total: pagination.total,
                            showSizeChanger: true,
                            showQuickJumper: true,
                            showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
                        }}
                        onChange={handleTableChange}
                        scroll={{ x: 1200 }}
                    />
                </TabPane>
            </Tabs>
        </div>
    );
};

export default AdminBookManagement; 