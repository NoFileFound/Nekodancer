package org.nekodancer.command.commands.modo;

// Imports
import java.util.List;
import org.nekodancer.GamePlayer;
import org.nekodancer.GameServer;
import org.nekodancer.command.Command;
import org.nekodancer.command.CommandHandler;
import org.nekodancer.data.objects.StaffRoleType;
import org.nekodancer.utils.Utils;

@Command(name = "banip", usage = "[IP]", description = "Bans the selected IP address.", permission = {Command.CommandPermission.MODERATOR, Command.CommandPermission.ADMINISTRATOR}, requiredArgs = 3)
@SuppressWarnings("unused")
public final class Banip implements CommandHandler {
    @Override
    public void execute(GamePlayer player, GameServer server, List<String> args) {
        try {
            server.sendIPBan(Utils.decodeIP(args.getFirst()), Integer.parseInt(args.get(1)), String.join(" ", args.subList(2, args.size())));
            for (GamePlayer client : server.getPlayers().values()) {
                if (client.getIpAddress().equals(Utils.decodeIP(args.getFirst())))
                    client.disconnect(true);
            }

            CommandHandler.sendServerMessage(player, String.format("The IP %s got banned.", args.getFirst()));
            server.sendServerMessageOthers(player, String.format("%s banned the IP address: %s.", player.getPlayerName(), args.getFirst()), StaffRoleType.PUBLIC_MODO);
        } catch (NumberFormatException ignored) {
            CommandHandler.sendServerMessage(player, "The supplied argument isn't a valid IP address.");
        }
    }
}