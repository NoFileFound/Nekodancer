package org.nekodancer.command.commands.modo;

// Imports
import java.util.List;
import org.nekodancer.GamePlayer;
import org.nekodancer.GameServer;
import org.nekodancer.command.Command;
import org.nekodancer.command.CommandHandler;

@Command(name = "bandef", usage = "[playerName]", description = "Special banning order for cheaters permanently.", permission = {Command.CommandPermission.MODERATOR, Command.CommandPermission.ADMINISTRATOR}, requiredArgs = 1)
@SuppressWarnings("unused")
public final class Bandef implements CommandHandler {
    @Override
    public void execute(GamePlayer player, GameServer server, List<String> args) {
        server.sendPlayerBan(args.getFirst(), -1, String.join(" ", args.subList(1, args.size())), player, false);
    }
}