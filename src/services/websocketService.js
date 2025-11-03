import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import authService from './authService';

/**
 * WebSocket服务类
 * 用于处理与后端的WebSocket连接和消息订阅
 */
class WebSocketService {
  constructor() {
    this.stompClient = null;
    this.connected = false;
    this.subscriptions = new Map();
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectInterval = 3000;
    this.userSubscribed = false;
    this.eventHandlers = new Map(); // 简单事件总线 (CONNECTION_STATUS / ORDER_STATUS_UPDATE)
  }

  // 事件发布
  publish(event, data) {
    const handlers = this.eventHandlers.get(event);
    if (handlers) {
      handlers.forEach(fn => {
        try { fn(data); } catch (e) { console.error('[WebSocketService] 事件回调异常', event, e); }
      });
    }
  }

  // 事件订阅
  subscribe(event, handler) {
    if (!this.eventHandlers.has(event)) {
      this.eventHandlers.set(event, []);
    }
    this.eventHandlers.get(event).push(handler);
    return () => {
      const arr = this.eventHandlers.get(event);
      if (!arr) return;
      const idx = arr.indexOf(handler);
      if (idx > -1) arr.splice(idx, 1);
    };
  }

  ensureUserSubscription(userId) {
    if (!this.stompClient || !this.stompClient.connected) return;
    if (this.userSubscribed) return;
    this.stompClient.subscribe(`/user/${userId}/queue/order-updates`, (message) => {
      try {
        const orderUpdate = JSON.parse(message.body);
        this.publish('ORDER_STATUS_UPDATE', orderUpdate);
      } catch (e) {
        console.error('订单更新解析失败:', e);
      }
    });
    this.userSubscribed = true;
  }

  /**
   * 连接到WebSocket服务器
   * @param {string} userId - 用户ID（确保是字符串类型）
   * @param {object} callbacks - 回调对象
   */
  connect(userId, callbacks) {
    const onConnectCb = callbacks?.onConnect || callbacks?.success;
    const onErrorCb = callbacks?.onError || callbacks?.error;
    const onCloseCb = callbacks?.onClose;

    if (this.stompClient && this.stompClient.connected) {
      console.log('WebSocket already connected');
      this.connected = true;
      this.ensureUserSubscription(userId);
      this.publish('CONNECTION_STATUS', { connected: true, reuse: true });
      if (onConnectCb) onConnectCb();
      return;
    }

    // 确保userId是字符串类型
    const userIdStr = String(userId);
    console.log('尝试连接WebSocket，用户ID:', userIdStr);
    console.log('WebSocket URL: http://localhost:8082/ws');

    try {
      // 使用token认证
      const token = authService.getToken();
      console.log('WebSocket连接使用token:', token ? '已获取' : '未获取');

      // 创建STOMP客户端
      this.stompClient = new Client({
        webSocketFactory: () => new SockJS('http://localhost:8082/ws'),
        connectHeaders: {
          'Authorization': `Bearer ${token}`
        },
        debug: (str) => {
          console.log('STOMP: ' + str);
        },
        onConnect: (frame) => {
          console.log('WebSocket连接成功:', frame);
          this.connected = true;
          this.reconnectAttempts = 0;
          this.publish('CONNECTION_STATUS', { connected: true });
          this.ensureUserSubscription(userIdStr);
          
          if (onConnectCb) {
            onConnectCb(frame);
          }
        },
        onStompError: (error) => {
          console.error('STOMP错误:', error);
          this.connected = false;
          this.userSubscribed = false;
          this.publish('CONNECTION_STATUS', { connected: false, error: error.headers });
          if (onErrorCb) {
            onErrorCb(error);
          }
        },
        onWebSocketError: (error) => {
          console.error('WebSocket错误:', error);
          this.connected = false;
          if (onErrorCb) {
            onErrorCb(error);
          }
        },
        onWebSocketClose: (event) => {
          console.log('WebSocket连接关闭:', event);
          this.connected = false;
          this.userSubscribed = false;
          this.publish('CONNECTION_STATUS', { connected: false });
          if (onCloseCb) onCloseCb();
          this.attemptReconnect(userIdStr, onConnectCb, onErrorCb);
        }
      });

      // 开始连接
      this.stompClient.activate();

    } catch (error) {
      console.error('WebSocket连接失败:', error);
      if (onErrorCb) {
        onErrorCb(error);
      }
    }
  }

  /**
   * 订阅用户私有队列
   * @param {string} userId - 用户ID（确保是字符串类型）
   */
  subscribeToUserQueue(userId) {
    if (!this.connected || !this.stompClient) {
      console.warn('WebSocket未连接，无法订阅');
      return;
    }

    // 确保userId是字符串类型
    const userIdStr = String(userId);

    const subscription = this.stompClient.subscribe(
      `/user/${userIdStr}/queue/order-updates`,
      (message) => {
        try {
          const orderStatus = JSON.parse(message.body);
          console.log('收到订单状态更新:', orderStatus);
          
          // 触发全局事件
          window.dispatchEvent(new CustomEvent('orderStatusUpdate', {
            detail: orderStatus
          }));
        } catch (error) {
          console.error('解析订单状态消息失败:', error);
        }
      }
    );

    this.subscriptions.set(`user-${userIdStr}`, subscription);
    console.log(`已订阅用户 ${userIdStr} 的订单更新队列`);
  }

  /**
   * 订阅公共主题（可选）
   */
  subscribeToPublicTopic() {
    if (!this.connected || !this.stompClient) {
      console.warn('WebSocket未连接，无法订阅');
      return;
    }

    const subscription = this.stompClient.subscribe(
      '/topic/order-updates',
      (message) => {
        try {
          const orderStatus = JSON.parse(message.body);
          console.log('收到公共订单状态更新:', orderStatus);
          
          // 触发全局事件
          window.dispatchEvent(new CustomEvent('publicOrderUpdate', {
            detail: orderStatus
          }));
        } catch (error) {
          console.error('解析公共订单状态消息失败:', error);
        }
      }
    );

    this.subscriptions.set('public', subscription);
    console.log('已订阅公共订单更新主题');
  }

  /**
   * 尝试重连
   * @param {string} userId - 用户ID
   * @param {function} onConnect - 连接成功回调
   * @param {function} onError - 连接错误回调
   */
  attemptReconnect(userId, onConnect, onError) {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('WebSocket重连次数超限，停止重连');
      return;
    }

    this.reconnectAttempts++;
    console.log(`尝试重连WebSocket (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);

    setTimeout(() => {
      this.connect(userId, { onConnect, onError });
    }, this.reconnectInterval);
  }

  /**
   * 断开连接
   */
  disconnect() {
    if (this.stompClient && this.connected) {
      // 取消所有订阅
      this.subscriptions.forEach((subscription) => {
        subscription.unsubscribe();
      });
      this.subscriptions.clear();

      // 断开连接
      this.stompClient.deactivate();
      console.log('WebSocket连接已断开');
      
      this.connected = false;
      this.stompClient = null;
      this.userSubscribed = false;
    }
  }

  /**
   * 检查连接状态
   * @returns {boolean} 是否已连接
   */
  isConnected() {
    return !!(this.stompClient && this.stompClient.connected) || this.connected;
  }

  /**
   * 发送消息到服务器（如果需要）
   * @param {string} destination - 目标地址
   * @param {object} message - 消息内容
   */
  sendMessage(destination, message) {
    if (this.connected && this.stompClient) {
      this.stompClient.publish({
        destination: destination,
        body: JSON.stringify(message)
      });
    } else {
      console.warn('WebSocket未连接，无法发送消息');
    }
  }
}

// 创建单例实例
const websocketService = new WebSocketService();

export default websocketService;
