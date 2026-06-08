package com.catfeeder.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class RedisDistributedLock {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String LOCK_PREFIX = "lock:";
    private static final long DEFAULT_WAIT_TIME = 3000;
    private static final long DEFAULT_LEASE_TIME = 10000;
    private static final long RETRY_INTERVAL = 100;

    private static final int FAILURE_THRESHOLD = 5;
    private static final long RECOVERY_TIME_MS = 30000;

    private final AtomicBoolean circuitOpen = new AtomicBoolean(false);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private volatile long lastFailureTime = 0;

    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";

    @PostConstruct
    public void init() {
        log.info("Redis分布式锁初始化完成");
    }

    private boolean isCircuitOpen() {
        if (circuitOpen.get()) {
            if (System.currentTimeMillis() - lastFailureTime > RECOVERY_TIME_MS) {
                log.info("Redis熔断器半开，尝试恢复");
                circuitOpen.set(false);
                failureCount.set(0);
                return false;
            }
            return true;
        }
        return false;
    }

    private void recordFailure() {
        int count = failureCount.incrementAndGet();
        lastFailureTime = System.currentTimeMillis();
        if (count >= FAILURE_THRESHOLD && !circuitOpen.get()) {
            log.warn("Redis连续失败{}次，熔断器打开，降级跳过锁", count);
            circuitOpen.set(true);
        }
    }

    private void recordSuccess() {
        if (failureCount.get() > 0) {
            failureCount.set(0);
        }
        if (circuitOpen.get()) {
            log.info("Redis连接恢复，熔断器关闭");
            circuitOpen.set(false);
        }
    }

    public String tryLock(String lockKey) {
        return tryLock(lockKey, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, TimeUnit.MILLISECONDS);
    }

    public String tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
        if (isCircuitOpen()) {
            log.debug("Redis熔断器打开，跳过锁: key={}", lockKey);
            return UUID.randomUUID().toString();
        }

        String key = LOCK_PREFIX + lockKey;
        String requestId = UUID.randomUUID().toString();
        long waitMillis = timeUnit.toMillis(waitTime);
        long leaseMillis = timeUnit.toMillis(leaseTime);
        long startTime = System.currentTimeMillis();

        while (true) {
            try {
                Boolean acquired = redisTemplate.opsForValue()
                        .setIfAbsent(key, requestId, leaseMillis, TimeUnit.MILLISECONDS);

                recordSuccess();

                if (Boolean.TRUE.equals(acquired)) {
                    log.debug("获取锁成功: key={}, requestId={}", key, requestId);
                    return requestId;
                }
            } catch (Exception e) {
                recordFailure();
                log.warn("获取Redis锁异常，降级跳过锁: key={}, error={}", key, e.getMessage());
                return requestId;
            }

            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed >= waitMillis) {
                log.warn("获取锁超时: key={}, 等待时间={}ms", key, waitMillis);
                return null;
            }

            try {
                Thread.sleep(RETRY_INTERVAL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
    }

    public boolean unlock(String lockKey, String requestId) {
        if (requestId == null) {
            return false;
        }

        if (isCircuitOpen()) {
            log.debug("Redis熔断器打开，跳过释放锁: key={}", lockKey);
            return true;
        }

        String key = LOCK_PREFIX + lockKey;
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(UNLOCK_SCRIPT);
            script.setResultType(Long.class);

            Long result = redisTemplate.execute(
                    script,
                    Collections.singletonList(key),
                    requestId
            );

            recordSuccess();

            boolean success = result != null && result == 1;
            if (success) {
                log.debug("释放锁成功: key={}, requestId={}", key, requestId);
            } else {
                log.warn("释放锁失败: key={}, requestId={} (锁可能已过期或被其他线程持有)", key, requestId);
            }
            return success;
        } catch (Exception e) {
            recordFailure();
            log.error("释放Redis锁异常: key={}, error={}", key, e.getMessage());
            return true;
        }
    }

    public boolean isLocked(String lockKey) {
        if (isCircuitOpen()) {
            return false;
        }

        String key = LOCK_PREFIX + lockKey;
        try {
            Boolean hasKey = redisTemplate.hasKey(key);
            recordSuccess();
            return Boolean.TRUE.equals(hasKey);
        } catch (Exception e) {
            recordFailure();
            log.error("检查锁状态异常: key={}", key, e);
            return false;
        }
    }

    public boolean isAvailable() {
        return !circuitOpen.get();
    }
}
