import React, { useState, useEffect } from 'react';
import { DatePicker, Spin, Alert, Typography, Row, Col, Card, Table, Empty, Button, message } from 'antd';
import { BarChartOutlined, UserOutlined, ShoppingCartOutlined } from '@ant-design/icons'; // Removed AreaChartOutlined as it's not directly used in the Card titles here, but it's fine if kept.
import statisticsService from '../services/statisticsService';
import dayjs from 'dayjs';
import { Column } from '@ant-design/charts'; // Import Column chart instead of Bar

const { Title, Text } = Typography;
const { RangePicker } = DatePicker;

const AdminStatisticsPage = () => {
    const [dateRange, setDateRange] = useState([dayjs().subtract(30, 'days'), dayjs()]);
    const [loadingBookStats, setLoadingBookStats] = useState(false);
    const [bookStats, setBookStats] = useState([]);
    const [bookStatsError, setBookStatsError] = useState(null);
    const [loadingUserStats, setLoadingUserStats] = useState(false);
    const [userStats, setUserStats] = useState([]);
    const [userStatsError, setUserStatsError] = useState(null);

    const fetchBookSalesStats = async (start, end) => {
        setLoadingBookStats(true);
        setBookStatsError(null);
        try {
            const data = await statisticsService.getBookSalesStats(start, end);
            setBookStats(data || []);
        } catch (err) {
            message.error(`加载书籍销量统计失败: ${err.message || '未知错误'}`);
            setBookStatsError(err.message || '获取统计数据失败');
            setBookStats([]);
        }
        setLoadingBookStats(false);
    };

    const fetchUserConsumptionStats = async (start, end) => {
        setLoadingUserStats(true);
        setUserStatsError(null);
        try {
            const data = await statisticsService.getUserConsumptionStats(start, end);
            setUserStats(data || []);
        } catch (err) {
            message.error(`加载用户消费统计失败: ${err.message || '未知错误'}`);
            setUserStatsError(err.message || '获取统计数据失败');
            setUserStats([]);
        }
        setLoadingUserStats(false);
    };

    useEffect(() => {
        if (dateRange && dateRange[0] && dateRange[1]) {
            fetchBookSalesStats(dateRange[0], dateRange[1]);
            fetchUserConsumptionStats(dateRange[0], dateRange[1]);
        }
    }, [dateRange]);

    const handleDateRangeChange = (dates) => {
        if (dates && dates.length === 2) {
            setDateRange(dates);
        } else {
            setDateRange(null); 
            setBookStats([]);
            setUserStats([]);
        }
    };

    const bookSalesColumns = [
        { title: '排名', key: 'rank', render: (text, record, index) => index + 1, width: 70 },
        { title: '封面', dataIndex: 'bookCover', key: 'bookCover', render: (cover, record) => <img src={cover || 'https://via.placeholder.com/40x60?text=N/A'} alt={record.bookTitle} style={{width: 40, height: 60, objectFit: 'contain'}} />, width: 80},
        { title: '书名', dataIndex: 'bookTitle', key: 'bookTitle', ellipsis: true },
        { title: '作者', dataIndex: 'bookAuthor', key: 'bookAuthor', ellipsis: true },
        { title: '销量', dataIndex: 'totalQuantitySold', key: 'totalQuantitySold', sorter: (a, b) => a.totalQuantitySold - b.totalQuantitySold, defaultSortOrder: 'descend', width: 100, align: 'right' },
        { title: '销售额', dataIndex: 'totalRevenue', key: 'totalRevenue', render: (val) => `¥${val != null ? Number(val).toFixed(2) : '0.00'}`, sorter: (a, b) => a.totalRevenue - b.totalRevenue, width: 120, align: 'right' },
    ];

    const userConsumptionColumns = [
        { title: '排名', key: 'rank', render: (text, record, index) => index + 1, width: 70 },
        { title: '用户名', dataIndex: 'username', key: 'username', ellipsis: true },
        { title: '订单数', dataIndex: 'totalOrderCount', key: 'totalOrderCount', sorter: (a, b) => a.totalOrderCount - b.totalOrderCount, width: 100, align: 'right' },
        { title: '消费总额', dataIndex: 'totalAmountSpent', key: 'totalAmountSpent', render: (val) => `¥${val != null ? Number(val).toFixed(2) : '0.00'}`, sorter: (a, b) => a.totalAmountSpent - b.totalAmountSpent, defaultSortOrder: 'descend', width: 150, align: 'right' },
    ];

    const topN = 10;
    const bookSalesChartData = bookStats.slice(0, topN).map(item => ({ name: item.bookTitle, value: Number(item.totalQuantitySold) }));
    const userConsumptionChartData = userStats.slice(0, topN).map(item => ({ name: item.username, value: Number(item.totalAmountSpent) }));

    const bookSalesChartConfig = {
        data: bookSalesChartData,
        xField: 'name',
        yField: 'value',
        xAxis: {
            label: { 
                autoHide: true, 
                autoRotate: true,
                formatter: (v) => v && v.length > 10 ? v.substring(0,10)+'...': v
            },
            title: { text: '书籍名称', style: { fontSize: 12 } }
        },
        yAxis: {
            title: { text: '销量 (本)', style: { fontSize: 12 } },
            label: { formatter: (v) => `${v}` }
        },
        meta: {
            name: { alias: '书名' },
            value: { alias: '销量' }
        },
        tooltip: { formatter: (datum) => ({ name: datum.name, value: `${datum.value} 本` }) },
        height: 300,
        padding: 'auto',
    };

    const userConsumptionChartConfig = {
        data: userConsumptionChartData,
        xField: 'name',
        yField: 'value',
        xAxis: {
            label: { 
                autoHide: true, 
                autoRotate: true 
            },
            title: { text: '用户名', style: { fontSize: 12 } }
        },
        yAxis: {
            title: { text: '消费总额 (元)', style: { fontSize: 12 } },
            label: { formatter: (v) => `¥${Number(v).toFixed(2)}` }
        },
        meta: {
            name: { alias: '用户名' },
            value: { alias: '消费总额', formatter: (v) => `¥${Number(v).toFixed(2)}` }
        },
        tooltip: { formatter: (datum) => ({ name: datum.name, value: `¥${Number(datum.value).toFixed(2)}` }) },
        height: 300,
        padding: 'auto',
    };

    return (
        <div>
            <Title level={2} style={{ marginBottom: 24 }}>销售统计 (管理员)</Title>
            <Card style={{ marginBottom: 24 }}>
                <Text strong>选择统计时间范围: </Text>
                <RangePicker 
                    value={dateRange} 
                    onChange={handleDateRangeChange} 
                    allowClear={false} 
                    style={{marginLeft: 8}}
                    ranges={{
                        '过去7天': [dayjs().subtract(7, 'days'), dayjs()],
                        '过去30天': [dayjs().subtract(30, 'days'), dayjs()],
                        '本月': [dayjs().startOf('month'), dayjs().endOf('month')],
                        '上个月': [dayjs().subtract(1, 'month').startOf('month'), dayjs().subtract(1, 'month').endOf('month')],
                    }}
                />
            </Card>

            <Row gutter={[24, 24]}>
                <Col xs={24} lg={12}>
                    <Card title={<><ShoppingCartOutlined style={{marginRight: 8}}/> 书籍热销榜</>} bordered={false} style={{ boxShadow: '0 2px 8px rgba(0,0,0,0.09)' }}>
                        {loadingBookStats && <div style={{textAlign: 'center', padding: '20px'}}><Spin tip="加载热销榜中..."/></div>}
                        {bookStatsError && !loadingBookStats && <Alert message="错误" description={bookStatsError} type="error" showIcon />}
                        {!loadingBookStats && !bookStatsError && bookStats.length === 0 && <Empty description="暂无数据或选定范围内无销售记录" />}
                        {!loadingBookStats && !bookStatsError && bookStats.length > 0 && (
                            <>
                                <Title level={5} style={{marginTop: 0, marginBottom: 16}}>Top {Math.min(topN, bookSalesChartData.length)} 书籍销量图</Title>
                                { bookSalesChartData.length > 0 ? <Column {...bookSalesChartConfig} /> : <Text>销量数据不足以生成图表。</Text> }
                                <Title level={5} style={{marginTop: 24, marginBottom: 16}}>详细列表</Title>
                                <Table dataSource={bookStats} columns={bookSalesColumns} rowKey="bookId" size="small" pagination={{ pageSize: 5 }} scroll={{ y: 240 }} />
                            </>
                        )}
                    </Card>
                </Col>
                <Col xs={24} lg={12}>
                    <Card title={<><UserOutlined style={{marginRight: 8}}/> 用户消费榜</>} bordered={false} style={{ boxShadow: '0 2px 8px rgba(0,0,0,0.09)' }}>
                        {loadingUserStats && <div style={{textAlign: 'center', padding: '20px'}}><Spin tip="加载消费榜中..."/></div>}
                        {userStatsError && !loadingUserStats && <Alert message="错误" description={userStatsError} type="error" showIcon />}
                        {!loadingUserStats && !userStatsError && userStats.length === 0 && <Empty description="暂无数据或选定范围内无消费记录" />}
                        {!loadingUserStats && !userStatsError && userStats.length > 0 && (
                            <>
                                <Title level={5} style={{marginTop: 0, marginBottom: 16}}>Top {Math.min(topN, userConsumptionChartData.length)} 用户消费图</Title>
                                {userConsumptionChartData.length > 0 ? <Column {...userConsumptionChartConfig} /> : <Text>消费数据不足以生成图表。</Text> }
                                <Title level={5} style={{marginTop: 24, marginBottom: 16}}>详细列表</Title>
                                <Table dataSource={userStats} columns={userConsumptionColumns} rowKey="userId" size="small" pagination={{ pageSize: 5 }} scroll={{ y: 240 }} />
                            </>
                        )}
                    </Card>
                </Col>
            </Row>
        </div>
    );
};

export default AdminStatisticsPage; 