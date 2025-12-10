import React, { useState, useRef, useEffect } from 'react';
import { Button, Input, Badge, Spin, Typography, Space, Tooltip } from 'antd';
import {
    MessageOutlined,
    CloseOutlined,
    SendOutlined,
    RobotOutlined,
    UserOutlined,
    MinusOutlined,
    QuestionCircleOutlined,
} from '@ant-design/icons';
import { sendMessage, checkServiceHealth } from '../services/chatService';

const { TextArea } = Input;
const { Text } = Typography;

// 样式定义
const styles = {
    // 悬浮按钮
    floatButton: {
        position: 'fixed',
        bottom: '24px',
        right: '24px',
        width: '56px',
        height: '56px',
        borderRadius: '50%',
        backgroundColor: '#1890ff',
        color: 'white',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        cursor: 'pointer',
        boxShadow: '0 4px 12px rgba(24, 144, 255, 0.4)',
        transition: 'all 0.3s ease',
        zIndex: 1000,
        border: 'none',
        fontSize: '24px',
    },
    floatButtonHover: {
        transform: 'scale(1.1)',
        boxShadow: '0 6px 16px rgba(24, 144, 255, 0.6)',
    },
    // 聊天窗口容器
    chatContainer: {
        position: 'fixed',
        bottom: '24px',
        right: '24px',
        width: '380px',
        height: '520px',
        backgroundColor: '#fff',
        borderRadius: '12px',
        boxShadow: '0 8px 32px rgba(0, 0, 0, 0.15)',
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden',
        zIndex: 1001,
        animation: 'slideUp 0.3s ease',
    },
    // 头部
    header: {
        background: 'linear-gradient(135deg, #1890ff 0%, #096dd9 100%)',
        color: 'white',
        padding: '12px 16px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
    },
    headerTitle: {
        display: 'flex',
        alignItems: 'center',
        gap: '8px',
        fontSize: '16px',
        fontWeight: '500',
    },
    headerButtons: {
        display: 'flex',
        gap: '8px',
    },
    headerBtn: {
        background: 'rgba(255, 255, 255, 0.2)',
        border: 'none',
        borderRadius: '50%',
        width: '28px',
        height: '28px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        cursor: 'pointer',
        color: 'white',
        transition: 'background 0.2s',
    },
    // 消息区域
    messagesContainer: {
        flex: 1,
        overflowY: 'auto',
        padding: '16px',
        backgroundColor: '#f5f7fa',
    },
    // 消息气泡
    messageWrapper: {
        display: 'flex',
        marginBottom: '12px',
        alignItems: 'flex-start',
        gap: '8px',
    },
    messageWrapperUser: {
        flexDirection: 'row-reverse',
    },
    avatar: {
        width: '32px',
        height: '32px',
        borderRadius: '50%',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        fontSize: '14px',
        flexShrink: 0,
    },
    avatarBot: {
        backgroundColor: '#1890ff',
        color: 'white',
    },
    avatarUser: {
        backgroundColor: '#52c41a',
        color: 'white',
    },
    messageBubble: {
        maxWidth: '75%',
        padding: '10px 14px',
        borderRadius: '12px',
        fontSize: '14px',
        lineHeight: '1.5',
        wordBreak: 'break-word',
        whiteSpace: 'pre-wrap',
    },
    messageBubbleBot: {
        backgroundColor: 'white',
        color: '#333',
        borderBottomLeftRadius: '4px',
        boxShadow: '0 1px 2px rgba(0, 0, 0, 0.1)',
    },
    messageBubbleUser: {
        backgroundColor: '#1890ff',
        color: 'white',
        borderBottomRightRadius: '4px',
    },
    // 输入区域
    inputContainer: {
        padding: '12px 16px',
        backgroundColor: 'white',
        borderTop: '1px solid #e8e8e8',
    },
    inputWrapper: {
        display: 'flex',
        gap: '8px',
        alignItems: 'flex-end',
    },
    // 快捷问题
    quickQuestions: {
        padding: '8px 16px',
        backgroundColor: 'white',
        borderTop: '1px solid #f0f0f0',
        display: 'flex',
        gap: '8px',
        flexWrap: 'wrap',
    },
    quickBtn: {
        fontSize: '12px',
        borderRadius: '16px',
    },
    // 状态指示器
    statusIndicator: {
        display: 'flex',
        alignItems: 'center',
        gap: '4px',
        fontSize: '12px',
        color: 'rgba(255, 255, 255, 0.8)',
    },
    statusDot: {
        width: '6px',
        height: '6px',
        borderRadius: '50%',
        backgroundColor: '#52c41a',
    },
    statusDotOffline: {
        backgroundColor: '#ff4d4f',
    },
    // 加载动画
    typingIndicator: {
        display: 'flex',
        gap: '4px',
        padding: '8px 0',
    },
    typingDot: {
        width: '8px',
        height: '8px',
        borderRadius: '50%',
        backgroundColor: '#1890ff',
        animation: 'bounce 1.4s infinite ease-in-out both',
    },
};

// CSS动画注入
const injectStyles = () => {
    if (document.getElementById('chatbot-styles')) return;
    
    const styleSheet = document.createElement('style');
    styleSheet.id = 'chatbot-styles';
    styleSheet.textContent = `
        @keyframes slideUp {
            from {
                opacity: 0;
                transform: translateY(20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        
        @keyframes bounce {
            0%, 80%, 100% {
                transform: scale(0);
            }
            40% {
                transform: scale(1);
            }
        }
        
        .chatbot-messages::-webkit-scrollbar {
            width: 6px;
        }
        
        .chatbot-messages::-webkit-scrollbar-track {
            background: transparent;
        }
        
        .chatbot-messages::-webkit-scrollbar-thumb {
            background: #d9d9d9;
            border-radius: 3px;
        }
        
        .chatbot-messages::-webkit-scrollbar-thumb:hover {
            background: #bfbfbf;
        }
    `;
    document.head.appendChild(styleSheet);
};

// 快捷问题列表
const quickQuestions = [
    '有什么书籍推荐？',
    '查看书籍分类',
    '搜索Python书籍',
    '50元以下的书',
];

// 欢迎消息
const welcomeMessage = {
    role: 'bot',
    content: '您好！👋 我是在线书店的智能助手。\n\n我可以帮您：\n📚 搜索书籍\n📂 查看分类\n💰 按价格筛选\n📖 推荐好书\n\n请问有什么可以帮您的？',
    timestamp: new Date(),
};

const ChatBot = () => {
    const [isOpen, setIsOpen] = useState(false);
    const [messages, setMessages] = useState([welcomeMessage]);
    const [inputValue, setInputValue] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [isOnline, setIsOnline] = useState(true);
    const [isButtonHovered, setIsButtonHovered] = useState(false);
    const messagesEndRef = useRef(null);
    const inputRef = useRef(null);

    // 注入CSS
    useEffect(() => {
        injectStyles();
    }, []);

    // 检查服务状态
    useEffect(() => {
        const checkHealth = async () => {
            const health = await checkServiceHealth();
            setIsOnline(health.overall);
        };
        
        checkHealth();
        const interval = setInterval(checkHealth, 30000); // 每30秒检查一次
        
        return () => clearInterval(interval);
    }, []);

    // 自动滚动到最新消息
    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages]);

    // 打开时聚焦输入框
    useEffect(() => {
        if (isOpen) {
            setTimeout(() => inputRef.current?.focus(), 100);
        }
    }, [isOpen]);

    // 发送消息
    const handleSend = async () => {
        const trimmedInput = inputValue.trim();
        if (!trimmedInput || isLoading) return;

        // 添加用户消息
        const userMessage = {
            role: 'user',
            content: trimmedInput,
            timestamp: new Date(),
        };
        setMessages(prev => [...prev, userMessage]);
        setInputValue('');
        setIsLoading(true);

        try {
            // 调用聊天服务
            const response = await sendMessage(trimmedInput);
            
            // 添加机器人回复
            const botMessage = {
                role: 'bot',
                content: response.reply,
                timestamp: new Date(),
            };
            setMessages(prev => [...prev, botMessage]);
        } catch (error) {
            // 错误处理
            const errorMessage = {
                role: 'bot',
                content: '抱歉，出现了一些问题。请稍后再试。',
                timestamp: new Date(),
            };
            setMessages(prev => [...prev, errorMessage]);
        } finally {
            setIsLoading(false);
        }
    };

    // 处理快捷问题点击
    const handleQuickQuestion = (question) => {
        setInputValue(question);
        setTimeout(() => handleSend(), 0);
    };

    // 处理键盘事件
    const handleKeyPress = (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            handleSend();
        }
    };

    // 渲染消息
    const renderMessage = (message, index) => {
        const isUser = message.role === 'user';
        
        return (
            <div
                key={index}
                style={{
                    ...styles.messageWrapper,
                    ...(isUser ? styles.messageWrapperUser : {}),
                }}
            >
                <div
                    style={{
                        ...styles.avatar,
                        ...(isUser ? styles.avatarUser : styles.avatarBot),
                    }}
                >
                    {isUser ? <UserOutlined /> : <RobotOutlined />}
                </div>
                <div
                    style={{
                        ...styles.messageBubble,
                        ...(isUser ? styles.messageBubbleUser : styles.messageBubbleBot),
                    }}
                >
                    {message.content}
                </div>
            </div>
        );
    };

    // 渲染加载动画
    const renderTypingIndicator = () => (
        <div style={styles.messageWrapper}>
            <div style={{ ...styles.avatar, ...styles.avatarBot }}>
                <RobotOutlined />
            </div>
            <div style={{ ...styles.messageBubble, ...styles.messageBubbleBot }}>
                <div style={styles.typingIndicator}>
                    <div style={{ ...styles.typingDot, animationDelay: '0s' }} />
                    <div style={{ ...styles.typingDot, animationDelay: '0.2s' }} />
                    <div style={{ ...styles.typingDot, animationDelay: '0.4s' }} />
                </div>
            </div>
        </div>
    );

    // 悬浮按钮
    if (!isOpen) {
        return (
            <Tooltip title="智能助手" placement="left">
                <Badge count={0} offset={[-5, 5]}>
                    <button
                        style={{
                            ...styles.floatButton,
                            ...(isButtonHovered ? styles.floatButtonHover : {}),
                        }}
                        onClick={() => setIsOpen(true)}
                        onMouseEnter={() => setIsButtonHovered(true)}
                        onMouseLeave={() => setIsButtonHovered(false)}
                    >
                        <MessageOutlined />
                    </button>
                </Badge>
            </Tooltip>
        );
    }

    // 聊天窗口
    return (
        <div style={styles.chatContainer}>
            {/* 头部 */}
            <div style={styles.header}>
                <div style={styles.headerTitle}>
                    <RobotOutlined style={{ fontSize: '20px' }} />
                    <span>书店智能助手</span>
                    <div style={styles.statusIndicator}>
                        <div
                            style={{
                                ...styles.statusDot,
                                ...(isOnline ? {} : styles.statusDotOffline),
                            }}
                        />
                        <span>{isOnline ? '在线' : '离线'}</span>
                    </div>
                </div>
                <div style={styles.headerButtons}>
                    <Tooltip title="帮助">
                        <button
                            style={styles.headerBtn}
                            onClick={() => {
                                setMessages(prev => [...prev, {
                                    role: 'bot',
                                    content: '💡 使用帮助：\n\n1. 直接输入问题即可\n2. 点击下方快捷按钮快速提问\n3. 支持书籍搜索、分类浏览、价格筛选等功能\n\n示例问题：\n- "有什么Python相关的书？"\n- "推荐一些50元以下的书"\n- "查看所有书籍分类"',
                                    timestamp: new Date(),
                                }]);
                            }}
                        >
                            <QuestionCircleOutlined />
                        </button>
                    </Tooltip>
                    <Tooltip title="最小化">
                        <button
                            style={styles.headerBtn}
                            onClick={() => setIsOpen(false)}
                        >
                            <MinusOutlined />
                        </button>
                    </Tooltip>
                    <Tooltip title="关闭">
                        <button
                            style={styles.headerBtn}
                            onClick={() => {
                                setIsOpen(false);
                                setMessages([welcomeMessage]);
                            }}
                        >
                            <CloseOutlined />
                        </button>
                    </Tooltip>
                </div>
            </div>

            {/* 消息区域 */}
            <div style={styles.messagesContainer} className="chatbot-messages">
                {messages.map((msg, idx) => renderMessage(msg, idx))}
                {isLoading && renderTypingIndicator()}
                <div ref={messagesEndRef} />
            </div>

            {/* 快捷问题 */}
            <div style={styles.quickQuestions}>
                {quickQuestions.map((q, idx) => (
                    <Button
                        key={idx}
                        size="small"
                        style={styles.quickBtn}
                        onClick={() => handleQuickQuestion(q)}
                        disabled={isLoading}
                    >
                        {q}
                    </Button>
                ))}
            </div>

            {/* 输入区域 */}
            <div style={styles.inputContainer}>
                <div style={styles.inputWrapper}>
                    <TextArea
                        ref={inputRef}
                        value={inputValue}
                        onChange={(e) => setInputValue(e.target.value)}
                        onKeyPress={handleKeyPress}
                        placeholder="输入您的问题..."
                        autoSize={{ minRows: 1, maxRows: 3 }}
                        disabled={isLoading}
                        style={{ flex: 1, resize: 'none' }}
                    />
                    <Button
                        type="primary"
                        icon={isLoading ? <Spin size="small" /> : <SendOutlined />}
                        onClick={handleSend}
                        disabled={!inputValue.trim() || isLoading}
                        style={{ height: '36px', width: '36px' }}
                    />
                </div>
            </div>
        </div>
    );
};

export default ChatBot;


