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

  const handleOrderStatusUpdate = useCallback((orderStatus) => {
    console.log('[useOrderStatus] 收到订单状态:', orderStatus);
    setOrderUpdates(prev => {
      const idx = prev.findIndex(u =>
        u.orderId === orderStatus.orderId ||
        (orderStatus.requestId && u.requestId === orderStatus.requestId)
      );
      if (idx >= 0) {
        const copy = [...prev];
        copy[idx] = { ...copy[idx], ...orderStatus };
        return copy;
      }
      return [...prev, orderStatus];
    });
    if (orderStatus.message) {
      message[orderStatus.status === 'FAILED' ? 'error' : 'success']({
        content: orderStatus.message,
        duration: 4
      });
    }
  }, []);

  const connectWebSocket = useCallback(() => {
    const uid = (user?.id || user?.userId);
    if (!uid) {
      console.warn('[useOrderStatus] 无有效用户ID，跳过连接');
      return;
    }
    websocketService.connect(
      uid.toString(),
      {
        onConnect: () => {
          console.log('[useOrderStatus] onConnect 回调触发');
          setIsConnected(true);
        },
        onError: () => {
          console.log('[useOrderStatus] onError 回调触发');
          setIsConnected(false);
        },
        onClose: () => {
          console.log('[useOrderStatus] onClose 回调触发');
          setIsConnected(false);
        }
      }
    );
    // 即时兜底
    setIsConnected(websocketService.isConnected());
  }, [user]);

  useEffect(() => {
    if (user && (user.id || user.userId)) {
      // 订阅连接状态
      const unsubConn = websocketService.subscribe('CONNECTION_STATUS', (st) => {
        console.log('[useOrderStatus] CONNECTION_STATUS 事件:', st);
        setIsConnected(!!st.connected);
      });
      // 订阅订单状态
      const unsubOrder = websocketService.subscribe('ORDER_STATUS_UPDATE', handleOrderStatusUpdate);

      connectWebSocket();

      const interval = setInterval(() => {
        const real = websocketService.isConnected();
        setIsConnected(prev => (prev === real ? prev : real));
      }, 7000);

      return () => {
        unsubConn();
        unsubOrder();
        clearInterval(interval);
      };
    }
  }, [user, connectWebSocket, handleOrderStatusUpdate]);

  const getOrderStatus = useCallback(orderId =>
    orderUpdates.find(u => u.orderId === orderId),
    [orderUpdates]
  );

  const getRequestStatus = useCallback(requestId =>
    orderUpdates.find(u => u.requestId === requestId),
    [orderUpdates]
  );

  const clearExpiredUpdates = useCallback(() => {
    const cutoff = Date.now() - 60 * 60 * 1000;
    setOrderUpdates(prev =>
      prev.filter(u => {
        if (!u.updateTime) return true;
        return new Date(u.updateTime).getTime() > cutoff;
      })
    );
  }, []);

  useEffect(() => {
    const cleanupInterval = setInterval(clearExpiredUpdates, 5 * 60 * 1000);
    return () => clearInterval(cleanupInterval);
  }, [clearExpiredUpdates]);

  return {
    orderUpdates,
    isConnected,
    getOrderStatus,
    getRequestStatus,
    clearExpiredUpdates,
    connectWebSocket,
    disconnectWebSocket: () => websocketService.disconnect()
  };
};
