package com.bookstore.online_bookstore_backend.controller;

import com.bookstore.online_bookstore_backend.dao.UserDao;
import com.bookstore.online_bookstore_backend.entity.User;
import com.bookstore.online_bookstore_backend.payload.response.MessageResponse;
import com.bookstore.online_bookstore_backend.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    UserDao userDao;

    @Autowired
    FileStorageService fileStorageService; // Inject FileStorageService

    // 更规范的做法是创建一个 DTO，例如 UserProfileUpdateRequest
    // 但为了快速演示，这里使用 Map
    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@AuthenticationPrincipal User currentUser, 
                                             @RequestBody Map<String, String> payload) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body(new MessageResponse("Error: Unauthorized - No user logged in."));
        }

        String phone = payload.get("phone");
        String address = payload.get("address");

        boolean changed = false;
        if (phone != null) {
            currentUser.setPhone(phone);
            changed = true;
        }
        if (address != null) {
            currentUser.setAddress(address);
            changed = true;
        }

        if (changed) {
            userDao.save(currentUser);
             // 返回更新后的用户信息 (不含敏感信息如密码)
            // 可以创建一个 UserProfileResponse DTO 来精确控制返回字段
            // 为简单起见，这里只返回成功消息
            return ResponseEntity.ok(new MessageResponse("User profile updated successfully!"));
        } else {
            return ResponseEntity.ok(new MessageResponse("No changes detected in profile data."));
        }
    }
    
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@AuthenticationPrincipal User currentUser,
                                          @RequestParam("avatar") MultipartFile avatarFile) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body(new MessageResponse("Error: Unauthorized - No user logged in."));
        }
        if (avatarFile.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Avatar file is empty."));
        }

        try {
            // Store the file and get the unique filename (or path)
            String fileName = fileStorageService.storeFile(avatarFile, currentUser.getId());

            // Construct the file download URI. 
            // This assumes you will serve files from a path like "/uploads/avatars/<filename>"
            // or just "/avatars/<filename>" depending on your static resource configuration.
            // For now, let's make it relative, assuming `file.upload-dir` is served.
            // A common pattern is to return the URL from which the avatar can be fetched.
            // If `file.upload-dir` is `./uploads/avatars`, and you serve `/uploads/avatars/**` as static content,
            // then the URL might be `ServletUriComponentsBuilder.fromCurrentContextPath().path("/uploads/avatars/").path(fileName).toUriString();`
            // For simplicity now, we just store the filename or a simple relative path.
            // The actual URL construction depends on how static resources are served.
            String avatarUrl = "/uploads/avatars/" + fileName; // Example URL path

            currentUser.setAvatarUrl(avatarUrl); // Store this relative URL in the user's profile
            userDao.save(currentUser);

            // You might return the new avatar URL or the updated user details
            return ResponseEntity.ok(Map.of(
                "message", "Avatar uploaded successfully!", 
                "avatarUrl", avatarUrl
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(new MessageResponse("Error uploading avatar: " + e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserDetails(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body(new MessageResponse("Error: Unauthorized - No user logged in."));
        }
        // 重要: 不要直接返回 User 实体，因为它包含密码哈希和其他可能不应暴露的字段
        // 创建一个 UserDto 或 Map 来只返回需要的字段
        // 例如: username, email, roles, phone, address, avatarUrl
        // 这里为了简单，我们假设 User 实体直接返回是安全的（在实际项目中需要 review）
        // 或者，更好的方式是让 UserDetailsServiceImpl 返回的 User 对象（如果它是 UserDetails 的实现）
        // 已经剥离了密码等敏感信息，但这取决于你的 User 类的设计
        // User currentUserDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // 构建一个安全的响应对象
        com.bookstore.online_bookstore_backend.payload.response.UserResponse userResponse = 
            new com.bookstore.online_bookstore_backend.payload.response.UserResponse(
                currentUser.getId(),
                currentUser.getUsername(),
                currentUser.getEmail(),
                currentUser.getRoles().stream().map(role -> role.getName().name()).collect(java.util.stream.Collectors.toList()),
                currentUser.getPhone(),
                currentUser.getAddress(),
                currentUser.getAvatarUrl(),
                currentUser.isEnabled()
            );
        return ResponseEntity.ok(userResponse);
    }
} 