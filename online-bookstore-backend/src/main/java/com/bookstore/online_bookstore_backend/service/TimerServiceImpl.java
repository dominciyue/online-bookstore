package com.bookstore.online_bookstore_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ApplicationScope
public class TimerServiceImpl implements TimerService {

    // 使用线程安全的ConcurrentHashMap来存储所有用户的计时器
    private Map<Long, Long> userTimers = new ConcurrentHashMap<>();

    @Override
    public void startTimer(Long userId) {
        long startTime = System.currentTimeMillis();
        userTimers.put(userId, startTime);
        System.out.println("=== TIMER DEBUG: startTimer ===");
        System.out.println("User ID: " + userId);
        System.out.println("Start time set: " + startTime);
        System.out.println("Current timers count: " + userTimers.size());
        System.out.println("All timers: " + userTimers);
    }

    @Override
    public long stopTimer(Long userId) {
        System.out.println("=== TIMER DEBUG: stopTimer ===");
        System.out.println("User ID: " + userId);
        System.out.println("Current timers before stop: " + userTimers);

        Long startTime = userTimers.get(userId);
        System.out.println("Start time found: " + startTime);

        if (startTime != null) {
            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime - startTime;
            userTimers.remove(userId);

            System.out.println("Current time: " + currentTime);
            System.out.println("Elapsed time: " + elapsed + "ms");
            System.out.println("Timers after removal: " + userTimers);

            return elapsed;
        } else {
            System.out.println("No start time found for user ID: " + userId);
            System.out.println("Returning 0 as session duration");
        }
        return 0;
    }

    @Override
    public Map<Long, Long> getAllTimers() {
        return new HashMap<>(userTimers);
    }
}
