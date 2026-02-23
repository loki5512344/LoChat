package com.loki.lohub.managers;

import com.loki.lohub.LoHub;
import com.loki.lohub.actions.Action;
import com.loki.lohub.actions.ActionType;
import com.loki.lohub.actions.impl.ActionbarAction;
import com.loki.lohub.actions.impl.BroadcastAction;
import com.loki.lohub.actions.impl.CloseAction;
import com.loki.lohub.actions.impl.CommandAction;
import com.loki.lohub.actions.impl.ConsoleAction;
import com.loki.lohub.actions.impl.EffectAction;
import com.loki.lohub.actions.impl.GamemodeAction;
import com.loki.lohub.actions.impl.MessageAction;
import com.loki.lohub.actions.impl.SoundAction;
import com.loki.lohub.actions.impl.TitleAction;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActionManager {

    private static final Pattern ACTION_PATTERN = Pattern.compile("\\[([A-Z]+)]\\s*(.*)");
    private final LoHub plugin;

    public ActionManager(LoHub plugin) {
        this.plugin = plugin;
    }

    public List<Action> parseActions(List<String> actionStrings) {
        List<Action> actions = new ArrayList<>();

        for (String actionString : actionStrings) {
            Action action = parseAction(actionString);
            if (action != null) {
                actions.add(action);
            }
        }

        return actions;
    }

    public Action parseAction(String actionString) {
        if (actionString == null || actionString.isEmpty()) {
            return null;
        }

        Matcher matcher = ACTION_PATTERN.matcher(actionString);
        if (!matcher.matches()) {
            plugin.getLogger().warning("Invalid action format: " + actionString);
            return null;
        }

        String typeStr = matcher.group(1);
        String data = matcher.group(2);

        ActionType type = ActionType.fromString(typeStr);

        return switch (type) {
            case MESSAGE -> new MessageAction(data);
            case BROADCAST -> new BroadcastAction(data);
            case TITLE -> new TitleAction(data);
            case ACTIONBAR -> new ActionbarAction(data);
            case SOUND -> new SoundAction(data);
            case COMMAND -> new CommandAction(data);
            case CONSOLE -> new ConsoleAction(data);
            case GAMEMODE -> new GamemodeAction(data);
            case EFFECT -> new EffectAction(data);
            case CLOSE -> new CloseAction();
            default -> {
                plugin.getLogger().warning("Unknown action type: " + typeStr);
                yield null;
            }
        };
    }

    public void executeActions(Player player, List<Action> actions) {
        for (Action action : actions) {
            try {
                action.execute(player);
            } catch (Exception e) {
                plugin.getLogger().severe("Error executing action " + action.getType() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void executeActions(Player player, List<String> actionStrings, boolean parse) {
        if (parse) {
            List<Action> actions = parseActions(actionStrings);
            executeActions(player, actions);
        } else {
            for (String actionString : actionStrings) {
                Action action = parseAction(actionString);
                if (action != null) {
                    try {
                        action.execute(player);
                    } catch (Exception e) {
                        plugin.getLogger().severe("Error executing action: " + e.getMessage());
                    }
                }
            }
        }
    }
}
