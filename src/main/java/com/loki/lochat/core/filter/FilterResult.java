package com.loki.lochat.core.filter;

public record FilterResult(boolean allowed, String filteredMessage, String blockReason) {
    
    public static FilterResult ok(String message) {
        return new FilterResult(true, message, null);
    }
    
    public static FilterResult blocked(String reason) {
        return new FilterResult(false, null, reason);
    }
    
    public boolean isAllowed() {
        return allowed;
    }
    
    public String getMessage() {
        return filteredMessage;
    }
}
