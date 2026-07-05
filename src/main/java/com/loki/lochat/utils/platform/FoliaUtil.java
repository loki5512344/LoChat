package com.loki.lochat.utils.platform;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Утилита для совместимости с Folia
 */
public class FoliaUtil {
    private FoliaUtil() {
    }

    private static boolean isFolia;

    static {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia = true;
        } catch (ClassNotFoundException e) {
            isFolia = false;
        }
    }

    public static boolean isFolia() {
        return isFolia;
    }

    public static void runAsync(JavaPlugin plugin, Runnable task) {
        if (isFolia) {
            Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    public static void runEntityTask(JavaPlugin plugin, Entity entity, Runnable task) {
        if (isFolia) {
            entity.getScheduler().run(plugin, scheduledTask -> task.run(), null);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * Запускает глобальную повторяющуюся задачу
     *
     * @param plugin      плагин
     * @param task        задача
     * @param delayTicks  задержка перед первым запуском (в тиках)
     * @param periodTicks период повторения (в тиках)
     */
    public static void runTimerAsync(JavaPlugin plugin, Runnable task, long delayTicks, long periodTicks) {
        if (isFolia) {
            // В Folia используем AsyncScheduler для глобальных задач
            long delayMs = delayTicks * 50; // 1 тик = 50мс
            long periodMs = periodTicks * 50;
            Bukkit.getAsyncScheduler().runAtFixedRate(
                plugin, scheduledTask -> task.run(), delayMs, periodMs,
                java.util.concurrent.TimeUnit.MILLISECONDS
            );
        } else {
            // В Paper/Spigot используем обычный scheduler
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
        }
    }
}
