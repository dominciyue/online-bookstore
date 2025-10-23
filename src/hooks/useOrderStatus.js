import { useState, useEffect, useCallback } from 'react';
import { message } from 'antd';
import websocketService from '../services/websocketService';
import { useAuth } from '../contexts/AuthContext';

/**
 * 订单状态管理Hook
 * 处理WebSocket连接和订单状态更新
 */
export const useOrderStatus = () => {
  const [orderUpdates, setOrderUpdates] = useState([]);
  const [isConnected, setIsConnected] = useState(false);
  const { user } = useAuth();

  // 处理订单状态更新
  const handleOrderStatusUpdate = useCallback((orderStatus) => {
    console.log('处理订单状态更新:', orderStatus);
    
    setOrderUpdates(prev => {
      // 更新或添加订单状态
      const existingIndex = prev.findIndex(update => 
        update.orderId === orderStatus.orderId || 
        update.requestId === orderStatus.requestId
      );
      
      if (existingIndex >= 0) {
        // 更新现有订单状态
        const updated = [...prev];
        updated[existingIndex] = { ...updated[existingIndex], ...orderStatus };
        return updated;
      } else {
        // 添加新订单状态
        return [...prev, orderStatus];
      }
    });

    // 显示通知消息
    if (orderStatus.message) {
      const messageType = orderStatus.status === 'FAILED' ? 'error' : 'success';
      message[messageType]({
        content: orderStatus.message,
        duration: 4,
      });
    }
  }, []);

  // 处理公共订单更新
  const handlePublicOrderUpdate = useCallback((orderStatus) => {
    console.log('处理公共订单更新:', orderStatus);
    // 可以在这里处理管理员监控等逻辑
  }, []);

  // 连接WebSocket
  const connectWebSocket = useCallback(() => {
    if (!user || !user.id) {
      console.warn('用户未登录，无法连接WebSocket', { user });
      return;
    }

    console.log('准备连接WebSocket，用户信息:', user);
    console.log('用户ID:', user.id, '类型:', typeof user.id);

    websocketService.connect(
      user.id.toString(),
      (frame) => {
        console.log('WebSocket连接成功');
        setIsConnected(true);
      },
      (error) => {
        console.error('WebSocket连接失败:', error);
        setIsConnected(false);
        message.error('实时通知连接失败，请刷新页面重试');
      }
    );
  }, [user]);

  // 断开WebSocket连接
  const disconnectWebSocket = useCallback(() => {
    websocketService.disconnect();
    setIsConnected(false);
  }, []);

  // 获取特定订单的状态
  const getOrderStatus = useCallback((orderId) => {
    return orderUpdates.find(update => update.orderId === orderId);
  }, [orderUpdates]);

  // 获取特定请求的状态
  const getRequestStatus = useCallback((requestId) => {
    return orderUpdates.find(update => update.requestId === requestId);
  }, [orderUpdates]);

  // 清除过期的订单状态（可选）
  const clearExpiredUpdates = useCallback(() => {
    const now = new Date();
    const oneHourAgo = new Date(now.getTime() - 60 * 60 * 1000);
    
    setOrderUpdates(prev => 
      prev.filter(update => {
        if (!update.updateTime) return true;
        const updateTime = new Date(update.updateTime);
        return updateTime > oneHourAgo;
      })
    );
  }, []);

  // WebSocket连接和事件监听
  useEffect(() => {
    // 启用WebSocket连接
    if (user && user.id) {
      connectWebSocket();
    }

    // 监听订单状态更新事件
    const handleOrderUpdate = (event) => {
      handleOrderStatusUpdate(event.detail);
    };

    const handlePublicUpdate = (event) => {
      handlePublicOrderUpdate(event.detail);
    };

    window.addEventListener('orderStatusUpdate', handleOrderUpdate);
    window.addEventListener('publicOrderUpdate', handlePublicUpdate);

    // 定期清理过期更新
    const cleanupInterval = setInterval(clearExpiredUpdates, 5 * 60 * 1000); // 每5分钟清理一次

    return () => {
      window.removeEventListener('orderStatusUpdate', handleOrderUpdate);
      window.removeEventListener('publicOrderUpdate', handlePublicUpdate);
      clearInterval(cleanupInterval);
      // 组件卸载时不断开WebSocket，保持全局连接
    };
  }, [user, handleOrderStatusUpdate, handlePublicOrderUpdate, clearExpiredUpdates, connectWebSocket]);

  return {
    orderUpdates,
    isConnected,
    getOrderStatus,
    getRequestStatus,
    clearExpiredUpdates,
    connectWebSocket,
    disconnectWebSocket
  };
};
