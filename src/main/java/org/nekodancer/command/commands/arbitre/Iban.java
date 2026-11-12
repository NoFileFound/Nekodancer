package org.nekodancer.command.commands.arbitre;

// Imports
import java.util.List;
import org.nekodancer.GamePlayer;
import org.nekodancer.GameServer;
import org.nekodancer.command.Command;
import org.nekodancer.command.CommandHandler;

@Command(name = "iban", usage = "[playerName] [hours] [reason]", description = "Default ban command, message is not sent in the room.", permission = {Command.CommandPermission.ARBITRE, Command.CommandPermission.MODERATOR, Command.CommandPermission.ADMINISTRATOR}, requiredArgs = 3)
@SuppressWarnings("unused")
public final class Iban implements CommandHandler {
    @Override
    public void execute(GamePlayer player, GameServer server, List<String> args) {
        try {
            server.sendPlayerBan(args.getFirst(), Integer.parseInt(args.get(1)), String.join(" ", args.subList(2, args.size())), player, true);
        } catch (NumberFormatException ignored) {
            CommandHandler.sendServerMessage(player, "Invalid number.");
        }
    }
}