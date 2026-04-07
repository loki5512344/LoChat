package com.loki.lochat.gradient.commands.handlers.color;

import org.bukkit.entity.Player;

/**
 * Интерфейс для обработчиков подкоманд /color
 */
public interface SubCommandHandler {

    /**
     * Обрабатывает подкоманду
     * @param player игрок, выполнивший команду
     * @param args аргументы команды
     * @return true если команда обработана успешно
     */
    boolean handle(Player player, String[] args);
}
