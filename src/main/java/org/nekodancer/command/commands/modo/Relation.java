package org.nekodancer.command.commands.modo;

// Imports
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.nekodancer.GamePlayer;
import org.nekodancer.GameServer;
import org.nekodancer.command.Command;
import org.nekodancer.command.CommandHandler;
import org.nekodancer.database.data.ConnectionLogEntry;
import org.nekodancer.utils.Utils;

@Command(name = "relation", usage = "[playerName]", description = "Gives list of related accounts since the last reboot.", permission = {Command.CommandPermission.MODERATOR, Command.CommandPermission.ADMINISTRATOR}, requiredArgs = 1)
@SuppressWarnings("unused")
public final class Relation implements CommandHandler {
    @Override
    public void execute(GamePlayer player, GameServer server, List<String> args) {
        String playerName = args.getFirst();
        List<ConnectionLogEntry> logs = server.getDatabase().loadConnectionLogsByPlayer(playerName);
        if (logs.isEmpty()) {
            CommandHandler.sendServerMessage(player, String.format("The player <BV>%s</BV> hasn't logged in since the last reboot.", playerName));
            return;
        }

        Map<String, String> unique = new LinkedHashMap<>();
        for (ConnectionLogEntry log : logs) {
            if (log.getIpAddress() != null)
                unique.putIfAbsent(log.getPlayerName(), log.getIpAddress());
        }

        StringBuilder message = new StringBuilder(String.format("The player <BV>%s</BV> has the following relations:", playerName));
        for (Map.Entry<String, String> entry : unique.entrySet()) {
            String username = entry.getKey();
            String ip = Utils.encodeIP(entry.getValue());
            message.append(String.format("<br> - <BV>%s</BV> : <font color='%s'>%s</font> %s", username, Utils.colorIP(ip), ip, server.getPlayers().containsKey(username) ? "(current IP)" : ""));
        }

        CommandHandler.sendServerMessage(player, message.toString());
    }
}