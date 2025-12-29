package ru.lovar.gradientnick.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private String prefix;
    private List<String> colors;
    private boolean colorEnabled;
    private boolean prefixEnabled;
    private boolean prefixPurchased;
    private long lastColorChange;
    private long lastPrefixChange;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.prefix = null;
        this.colors = new ArrayList<>();
        this.colorEnabled = true;
        this.prefixEnabled = true;
        this.prefixPurchased = false;
        this.lastColorChange = 0;
        this.lastPrefixChange = 0;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public List<String> getColors() {
        return colors;
    }

    public void setColors(List<String> colors) {
        this.colors = colors != null ? new ArrayList<>(colors) : new ArrayList<>();
    }

    public boolean hasPrefix() {
        return prefix != null && !prefix.isEmpty();
    }

    public boolean hasColors() {
        return colors != null && !colors.isEmpty();
    }

    public boolean isColorEnabled() {
        return colorEnabled;
    }

    public void setColorEnabled(boolean colorEnabled) {
        this.colorEnabled = colorEnabled;
    }

    public boolean isPrefixEnabled() {
        return prefixEnabled;
    }

    public void setPrefixEnabled(boolean prefixEnabled) {
        this.prefixEnabled = prefixEnabled;
    }

    public boolean isPrefixPurchased() {
        return prefixPurchased;
    }

    public void setPrefixPurchased(boolean prefixPurchased) {
        this.prefixPurchased = prefixPurchased;
    }

    public long getLastColorChange() {
        return lastColorChange;
    }

    public void setLastColorChange(long lastColorChange) {
        this.lastColorChange = lastColorChange;
    }

    public long getLastPrefixChange() {
        return lastPrefixChange;
    }

    public void setLastPrefixChange(long lastPrefixChange) {
        this.lastPrefixChange = lastPrefixChange;
    }
}
