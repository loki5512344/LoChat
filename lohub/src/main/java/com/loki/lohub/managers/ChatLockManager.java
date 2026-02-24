package com.loki.lohub.managers;

import com.loki.lohub.LoHub;

public class ChatLockManager {

    private final LoHub plugin;

    public ChatLockManager(LoHub plugin) {
        this.plugin = plugin;
    }

    public boolean isChatLocked() {
        return plugin.getConfigManager().getData().getBoolean("chat_locked", false);
    }

    public void setChatLocked(boolean locked) {
        plugin.getConfigManager().getData().set("chat_locked", locked);
        plugin.getConfigManager().saveData();
    }

    public void toggle() {
        setChatLocked(!isChatLocked());
    }
}
