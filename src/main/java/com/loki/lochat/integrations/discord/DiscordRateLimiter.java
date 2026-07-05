package com.loki.lochat.integrations.discord;

public class DiscordRateLimiter {

    private final double refillRate;
    private final double capacity;
    private double tokens;
    private long lastRefillTime;

    public DiscordRateLimiter(double maxRequestsPerSecond, double burst) {
        this.refillRate = maxRequestsPerSecond;
        this.capacity = burst;
        this.tokens = burst;
        this.lastRefillTime = System.nanoTime();
    }

    public synchronized boolean tryAcquire() {
        refill();
        if (tokens >= 1.0) {
            tokens -= 1.0;
            return true;
        }
        return false;
    }

    public void waitIfNeeded() {
        long sleepMs;
        while (true) {
            synchronized (this) {
                refill();
                if (tokens >= 1.0) {
                    tokens -= 1.0;
                    return;
                }
                double waitTimeMs = ((1.0 - tokens) / refillRate) * 1000.0;
                sleepMs = Math.min((long) Math.ceil(waitTimeMs), 100);
            }
            try {
                Thread.sleep(Math.max(sleepMs, 1));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void refill() {
        long now = System.nanoTime();
        double elapsed = (now - lastRefillTime) / 1_000_000_000.0;
        tokens = Math.min(capacity, tokens + elapsed * refillRate);
        lastRefillTime = now;
    }
}
