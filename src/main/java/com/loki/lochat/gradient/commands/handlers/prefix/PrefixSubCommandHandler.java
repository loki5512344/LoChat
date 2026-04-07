package com.loki.lochat.gradient.commands.handlers.prefix;

import org.bukkit.entity.Player;

/**
 * Интерфейс для обработчиков подкоманд /prefix
 */
public interface PrefixSubCommandHandler {

    /**
     * Обрабатывает подкоманду
     * @param player игрок, выполнивший команду
     * @param args аргументы команды
     * @return true если команда обработана успешно
     */
    boolean handle(Player player, String[] args);
}
