package org.nekodancer.command.commands.modo;

// Imports
import java.util.List;
import org.nekodancer.GamePlayer;
import org.nekodancer.GameServer;
import org.nekodancer.command.Command;
import org.nekodancer.command.CommandHandler;
import org.nekodancer.data.objects.StaffRoleType;
import org.nekodancer.utils.Utils;

@Command(name = "kickip", usage = "[IP]", description = "Disconnects the every player that has the specific ip address.", permission = {Command.CommandPermission.MODERATOR, Command.CommandPermission.ADMINISTRATOR}, requiredArgs = 1)
@SuppressWarnings("unused")
public final class Kickip implements CommandHandler {
    @Override
    public void execute(GamePlayer player, GameServer server, List<String> args) {
        try {
            String ipAddress = Utils.decodeIP(args.getFirst());
            int cnt = 0;
            for (GamePlayer client : server.getPlayers().values()) {
                if (client.getIpAddress().equals(ipAddress)) {
                    client.disconnect(true);
                    cnt++;
                }
            }

            if (cnt > 0) {
                CommandHandler.sendServerMessage(player, String.format("The IP %s got kicked.", args.getFirst()));
                server.sendServerMessageOthers(player, String.format("%s kicked everyone with the IP address: %s.", player.getPlayerName(), args.getFirst()), StaffRoleType.PUBLIC_MODO);
            } else {
                CommandHandler.sendServerMessage(player, "The supplied argument isn't a valid IP address.");
            }
        } catch (Exception ignored) {
            CommandHandler.sendServerMessage(player, "The supplied argument isn't a valid IP address.");
        }
    }
}