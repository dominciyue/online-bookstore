import React, { createContext, useState, useContext, useEffect } from 'react';
import cartService from '../services/cartService'; // 引入 cartService
import { message } from 'antd'; // 用于显示提示信息

// 创建购物车上下文
const CartContext = createContext();

// 购物车提供者组件
export const CartProvider = ({ children }) => {
  const [cartItems, setCartItems] = useState([]);
  const [loadingCart, setLoadingCart] = useState(true);

  // 从后端加载购物车数据
  const fetchCartItems = async () => {
    setLoadingCart(true);
    try {
      const items = await cartService.getCartItems();
      setCartItems(items || []); // 如果API返回null或undefined，则设置为空数组
    } catch (error) {
      console.error("Failed to fetch cart items:", error);
      message.error('加载购物车失败，请稍后重试');
      setCartItems([]); // 出错时设置为空数组
    } finally {
      setLoadingCart(false);
    }
  };

  useEffect(() => {
    fetchCartItems();
  }, []);

  // 添加商品到购物车
  const addToCart = async (book, quantity = 1) => {
    try {
      // 注意: book 对象现在应该包含 id, title, price, cover
      // 后端 addBookToCart 只需要 bookId 和 quantity
      // 但为了乐观更新或在失败时能回滚，我们可能需要完整 book 信息
      // 这里假设 book 包含了 id
      const addedItem = await cartService.addToCart(book.id, quantity);
      if (addedItem) {
        // 更新购物车状态，后端会返回完整的 CartItem 对象 (包含 title, price, cover)
        setCartItems(prevItems => {
          const existingItem = prevItems.find(item => item.bookId === addedItem.bookId);
          if (existingItem) {
            return prevItems.map(item =>
              item.bookId === addedItem.bookId ? { ...item, quantity: addedItem.quantity } : item
            );
          } else {
            return [...prevItems, addedItem];
          }
        });
        message.success(`${addedItem.title} 已添加到购物车`);
      } else {
        // 如果后端因某种原因没有返回 addedItem (例如库存不足且未抛出特定错误而是返回null)
        // 或者逻辑上不应发生，但作为防御性编程
        message.warning('添加到购物车时遇到问题，请重试');
        fetchCartItems(); // 重新从服务器同步状态
      }
    } catch (error) {
      console.error("Error adding to cart:", error);
      message.error(`添加失败: ${error.message}`);
    }
  };

  // 从购物车移除商品
  const removeFromCart = async (bookId) => {
    try {
      await cartService.removeFromCart(bookId);
      setCartItems(prevItems => prevItems.filter(item => item.bookId !== bookId));
      message.success('商品已从购物车移除');
    } catch (error) {
      console.error("Error removing from cart:", error);
      message.error(`移除失败: ${error.message}`);
    }
  };

  // 更新购物车中商品的数量
  const updateQuantity = async (bookId, quantity) => {
    if (quantity <= 0) {
      removeFromCart(bookId); // 如果数量小于等于0，则直接移除
      return;
    }
    try {
      const updatedItem = await cartService.updateCartItemQuantity(bookId, quantity);
      if (updatedItem) {
        setCartItems(prevItems =>
          prevItems.map(item => (item.bookId === bookId ? { ...item, ...updatedItem } : item))
        );
        // message.success('数量已更新'); // 这个提示可能过于频繁
      } else {
        // 可能因为数量为0而被删除，或者其他情况
        fetchCartItems(); // 重新同步
      }
    } catch (error) {
      console.error("Error updating quantity:", error);
      message.error(`更新数量失败: ${error.message}`);
    }
  };

  // 清空购物车 (通常在结算后调用)
  const clearCart = async () => {
    try {
      await cartService.clearCart();
      setCartItems([]);
      // message.success('购物车已清空'); // 这个消息通常在Cart.js的结算函数中给出
    } catch (error) {
      console.error("Error clearing cart:", error);
      message.error(`清空购物车失败: ${error.message}`);
    }
  };

  // 仅清空前端购物车状态（不调用后端API）
  const clearCartLocal = () => {
    setCartItems([]);
  };

  return (
    <CartContext.Provider value={{ cartItems, addToCart, removeFromCart, updateQuantity, clearCart, clearCartLocal, loadingCart, fetchCartItems }}>
      {children}
    </CartContext.Provider>
  );
};

// 自定义钩子，方便在组件中使用购物车上下文
export const useCart = () => {
  return useContext(CartContext);
}; 