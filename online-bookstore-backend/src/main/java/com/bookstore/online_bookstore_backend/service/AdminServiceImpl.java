package com.bookstore.online_bookstore_backend.service;

import com.bookstore.online_bookstore_backend.dao.UserDao;
import com.bookstore.online_bookstore_backend.entity.User;
import com.bookstore.online_bookstore_backend.exception.ResourceNotFoundException;
import com.bookstore.online_bookstore_backend.payload.response.MessageResponse;
import com.bookstore.online_bookstore_backend.payload.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserDao userDao;

    @Autowired
    public AdminServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        List<User> users = userDao.findAll();
        return users.stream().map(user -> new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toList()),
                user.getPhone(),
                user.getAddress(),
                user.getAvatarUrl(),
                user.isEnabled()
        )).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MessageResponse disableUser(Long userId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setEnabled(false);
        userDao.save(user);
        return new MessageResponse("User disabled successfully!");
    }

    @Override
    @Transactional
    public MessageResponse enableUser(Long userId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setEnabled(true);
        userDao.save(user);
        return new MessageResponse("User enabled successfully!");
    }
} 