package com.bookstore.online_bookstore_backend.service;

import java.util.Map;

public interface TimerService {
    void startTimer(Long userId);
    long stopTimer(Long userId);
    Map<Long, Long> getAllTimers();
}

