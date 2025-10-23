package com.bookstore.online_bookstore_backend.controller;

import com.bookstore.online_bookstore_backend.entity.CartItem;
import com.bookstore.online_bookstore_backend.entity.User;
import com.bookstore.online_bookstore_backend.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // 用于请求体映射

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // 获取用户的购物车项目
    @GetMapping
    public ResponseEntity<?> getCartItems(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
        }
        try {
            List<CartItem> cartItems = cartService.getCartItemsByUserId(currentUser.getId());
            return ResponseEntity.ok(cartItems);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "获取购物车项目时出错: " + e.getMessage()));
        }
    }

    // 添加商品到购物车
    // 请求体: { "bookId": <id>, "quantity": <qty> }
    @PostMapping("/add")
    public ResponseEntity<?> addItemToCart(@AuthenticationPrincipal User currentUser, @RequestBody Map<String, String> payload) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
        }
        try {
            Long bookId = Long.parseLong(payload.get("bookId"));
            int quantity = Integer.parseInt(payload.get("quantity"));
            CartItem addedItem = cartService.addBookToCart(currentUser.getId(), bookId, quantity);
            return ResponseEntity.status(HttpStatus.CREATED).body(addedItem);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "无效的书籍ID或数量格式。"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    // 更新购物车中商品的数量
    // 请求体: { "quantity": <qty> }
    @PutMapping("/update/{bookId}")
    public ResponseEntity<?> updateCartItemQuantity(@AuthenticationPrincipal User currentUser, @PathVariable Long bookId, @RequestBody Map<String, String> payload) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
        }
        try {
            int quantity = Integer.parseInt(payload.get("quantity"));
            // In CartService, if quantity <= 0, it will effectively remove the item.
            // So, no need to call removeBookFromCart explicitly here if that's the desired behavior.
            CartItem updatedItem = cartService.updateCartItemQuantity(currentUser.getId(), bookId, quantity);
            if (updatedItem == null && quantity <=0) { // Item was removed due to quantity <= 0
                return ResponseEntity.noContent().build(); 
            }
            return ResponseEntity.ok(updatedItem);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "无效的数量格式。"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage())); // Changed to NOT_FOUND for item not found cases
        }
    }

    // 从购物车移除商品
    @DeleteMapping("/remove/{bookId}")
    public ResponseEntity<?> removeItemFromCart(@AuthenticationPrincipal User currentUser, @PathVariable Long bookId) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
        }
        try {
            cartService.removeBookFromCart(currentUser.getId(), bookId);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    // 清空用户购物车
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户未登录"));
        }
        try {
            cartService.clearCart(currentUser.getId());
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "清空购物车时出错: " + e.getMessage()));
        }
    }
} 