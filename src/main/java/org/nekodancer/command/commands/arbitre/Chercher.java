package org.nekodancer.command.commands.arbitre;

// Imports
import java.util.List;
import org.nekodancer.GamePlayer;
import org.nekodancer.GameServer;
import org.nekodancer.command.Command;
import org.nekodancer.command.CommandHandler;

@Command(name = "chercher", usage = "[Text]", description = "Gives the name of the room where a player is located.", permission = {Command.CommandPermission.ARBITRE, Command.CommandPermission.MODERATOR, Command.CommandPermission.ADMINISTRATOR}, aliases = {"search", "find"}, requiredArgs = 1)
@SuppressWarnings("unused")
public final class Chercher implements CommandHandler {
    @Override
    public void execute(GamePlayer player, GameServer server, List<String> args) {
        String text = String.join(" ", args);
        boolean check = false;
        for (GamePlayer client : server.getPlayers().values()) {
            if(client.getPlayerName().contains(text)) {
                check = true;
                CommandHandler.sendServerMessage(player, String.format("<BV>%s</BV> -> %s<br>", client.getPlayerName(), client.getRoomName()));
            }
        }

        if(!check) {
            CommandHandler.sendServerMessage(player, "No results were found.");
        }
    }
}