package org.nekodancer.command.commands.modo;

// Imports
import java.util.List;
import org.nekodancer.GamePlayer;
import org.nekodancer.GameServer;
import org.nekodancer.command.Command;
import org.nekodancer.command.CommandHandler;
import org.nekodancer.data.objects.StaffRoleType;
import org.nekodancer.utils.Utils;

@Command(name = "unbanip", usage = "[IP]", description = "Unbans the selected IP address.", permission = {Command.CommandPermission.MODERATOR, Command.CommandPermission.ADMINISTRATOR}, requiredArgs = 1, aliases = {"desbanip", "debanip"})
@SuppressWarnings("unused")
public final class Unbanip implements CommandHandler {
    @Override
    public void execute(GamePlayer player, GameServer server, List<String> args) {
        try {
            server.sendIPUnban(Utils.decodeIP(args.getFirst()), player);
            CommandHandler.sendServerMessage(player, String.format("The IP %s got unbanned.", args.getFirst()));
            server.sendServerMessageOthers(player, String.format("%s unbanned the IP address: %s.", player.getPlayerName(), args.getFirst()), StaffRoleType.PUBLIC_MODO);
        } catch (NumberFormatException ignored) {
            CommandHandler.sendServerMessage(player, "The supplied argument isn't a valid IP address.");
        }
    }
}