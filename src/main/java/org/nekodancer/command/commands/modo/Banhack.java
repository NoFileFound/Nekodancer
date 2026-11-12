package org.nekodancer.command.commands.modo;

// Imports
import java.util.List;
import org.nekodancer.GamePlayer;
import org.nekodancer.GameServer;
import org.nekodancer.command.Command;
import org.nekodancer.command.CommandHandler;

@Command(name = "banhack", usage = "[playerName]", description = "Special banning order for cheaters.", permission = {Command.CommandPermission.MODERATOR, Command.CommandPermission.ADMINISTRATOR}, requiredArgs = 1)
@SuppressWarnings("unused")
public final class Banhack implements CommandHandler {
    @Override
    public void execute(GamePlayer player, GameServer server, List<String> args) {
        server.getParseModopwet().handleBanHack(player, args.getFirst(), false);
    }
}