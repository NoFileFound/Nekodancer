package org.nekodancer.command.commands.arbitre;

// Imports
import java.util.List;
import org.nekodancer.GamePlayer;
import org.nekodancer.GameServer;
import org.nekodancer.command.Command;
import org.nekodancer.command.CommandHandler;
import org.nekodancer.data.objects.StaffRoleType;
import org.nekodancer.database.data.PlayerEntity;

@Command(name = "resign", description = "It serves to resign as an arbiter.", permission = {Command.CommandPermission.ARBITRE})
@SuppressWarnings("unused")
public final class Resign implements CommandHandler {
    @Override
    public void execute(GamePlayer player, GameServer server, List<String> args) {
        PlayerEntity entity = player.getPlayerData();
        if (!entity.getStaffRoles().contains(StaffRoleType.ARBITRE))
            return;

        entity.getStaffRoles().remove(StaffRoleType.ARBITRE);
        server.sendStaffChannelMessage((byte) 2, String.format("%s has resigned.", player.getPlayerName()), true, player.getPlayerName(), player.getPlayerCommunity());
        player.disconnect(true);
    }
}