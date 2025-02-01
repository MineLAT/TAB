package me.neznamy.tab.shared.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.config.MessageFile;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Abstract class representing a subcommand of "/tab" command
 */
@Getter
@RequiredArgsConstructor
public abstract class SubCommand {

    //subcommands of this command
    private final Map<String, SubCommand> subcommands = new HashMap<>();

    //name of this subcommand
    private final String name;

    //permission required to run this command
    private final String permission;

    /**
     * Registers new subcommand
     *
     * @param   subcommand
     *          subcommand to register
     */
    public void registerSubCommand(@NotNull SubCommand subcommand) {
        subcommands.put(subcommand.name, subcommand);
    }

    /**
     * Returns whether player has permission to run this command or not
     *
     * @param   sender
     *          player who ran command or null if console
     * @return  true if sender has permission or is console, false otherwise
     */
    public boolean hasPermission(@Nullable TabPlayer sender) {
        return hasPermission(sender, permission);
    }

    /**
     * Returns whether player has given permission or not
     *
     * @param   sender
     *          player who ran command or null if console
     * @param   permission
     *          permission to check for
     * @return  true if sender has permission or is console, false otherwise
     */
    public boolean hasPermission(@Nullable TabPlayer sender, @Nullable String permission) {
        if (permission == null) return true; //no permission required
        if (sender == null) return true; //console
        if (sender.hasPermission(TabConstants.Permission.COMMAND_ALL)) return true;
        return sender.hasPermission(permission);
    }

    /**
     * Sends messages to the command sender with colors translated
     *
     * @param   sender
     *          player or console to send the message to
     * @param   messages
     *          messages to send
     */
    public void sendMessages(@Nullable TabPlayer sender, @NotNull List<String> messages) {
        for (String message : messages) {
            sendMessage(sender, message);
        }
    }

    /**
     * Sends message to the command sender with colors translated
     *
     * @param   sender
     *          player or console to send the message to
     * @param   message
     *          the message to send
     */
    public void sendMessage(@Nullable TabPlayer sender, @NotNull String message) {
        if (message.isEmpty()) return;
        if (sender != null) {
            sender.sendMessage(message);
        } else {
            TAB.getInstance().getPlatform().logInfo(TabComponent.fromColoredText(message));
        }
    }

    /**
     * Sends the message component to the player.
     * If player is {@code null}, the message is sent to the console instead.
     *
     * @param   sender
     *          player or console to send the message to
     * @param   message
     *          the message to send
     */
    public void sendMessage(@Nullable TabPlayer sender, @NotNull TabComponent message) {
        if (sender != null) {
            sender.sendMessage(message);
        } else {
            TAB.getInstance().getPlatform().logInfo(message);
        }
    }

    /**
     * Returns all players whose name start with given string
     *
     * @param   nameStart
     *          beginning of the name
     * @return  List of compatible players
     */
    public @NotNull List<String> getOnlinePlayers(@NotNull String nameStart) {
        List<String> suggestions = new ArrayList<>();
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all.getName().toLowerCase().startsWith(nameStart.toLowerCase())) suggestions.add(all.getName());
        }
        return suggestions;
    }

    public @NotNull List<String> getStartingArgument(@NotNull Collection<String> values, @NotNull String argument) {
        return values.stream().filter(value -> value.toLowerCase().startsWith(argument.toLowerCase())).collect(Collectors.toList());
    }

    /**
     * Performs command complete and returns list of arguments to be shown
     *
     * @param   sender
     *          command sender
     * @param   arguments
     *          arguments inserted in chat so far
     * @return  List of possible arguments
     */
    public @NotNull List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
        String argument;
        if (arguments.length == 0) {
            argument = "";
        } else {
            argument = arguments[0].toLowerCase();
        }
        if (arguments.length < 2) {
            List<String> suggestions = new ArrayList<>();
            for (String subcommand : subcommands.keySet()) {
                if (subcommand.startsWith(argument)) suggestions.add(subcommand);
            }
            return suggestions;
        }
        SubCommand subcommand = subcommands.get(argument);
        if (subcommand != null) {
            return subcommand.complete(sender, Arrays.copyOfRange(arguments, 1, arguments.length));
        }
        return Collections.emptyList();
    }

    public @NotNull MessageFile getMessages() {
        return TAB.getInstance().getConfiguration().getMessages();
    }

    /**
     * Performs the command
     *
     * @param   sender
     *          command sender or null if console
     * @param   args
     *          arguments of the command
     */
    public abstract void execute(@Nullable TabPlayer sender, @NotNull String[] args);
}