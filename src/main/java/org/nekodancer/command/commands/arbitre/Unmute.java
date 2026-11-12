package org.nekodancer.command.commands.arbitre;

// Imports
import java.util.List;
import org.nekodancer.GamePlayer;
import org.nekodancer.GameServer;
import org.nekodancer.command.Command;
import org.nekodancer.command.CommandHandler;

@Command(name = "unmute", usage = "[playerName] (reason)", description = "Removes a player's mute.", permission = {Command.CommandPermission.ARBITRE, Command.CommandPermission.MODERATOR, Command.CommandPermission.ADMINISTRATOR}, requiredArgs = 1, aliases = {"demute", "desmute"})
@SuppressWarnings("unused")
public final class Unmute implements CommandHandler {
    @Override
    public void execute(GamePlayer player, GameServer server, List<String> args) {
        server.sendPlayerUnmute(args.getFirst(), args.size() == 1 ? "Unspecified" : String.join(" ", args.subList(1, args.size())), player);
    }
}