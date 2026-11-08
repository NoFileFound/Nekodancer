package org.nekodancer.command.commands.tribe;

// Imports
import java.util.List;
import org.nekodancer.GamePlayer;
import org.nekodancer.GameServer;
import org.nekodancer.command.Command;
import org.nekodancer.command.CommandHandler;

// Packets
import org.nekodancer.packets.send.PacketSendTribeHouseInvitation;
import org.nekodancer.packets.send.informations.PacketSendTranslatedMessage;

@Command(name = "inv", usage = "[playerName]", description = "Invites the user to your tribe house.", permission = Command.CommandPermission.TRIBE, requiredArgs = 1)
@SuppressWarnings("unused")
public final class Inv implements CommandHandler {
    @Override
    public void execute(GamePlayer player, GameServer server, List<String> args) {
        String playerName = args.getFirst();
        String tribeName = player.getPlayerData().tribeName;
        GamePlayer target = server.getPlayers().get(playerName);
        if (target == null || server.getDatabase().loadTribeByName(tribeName).getTribeMembers().contains(playerName))
            return;

        target.getInvitedTribeHouses().add(tribeName);
        player.broadcastPacket(new PacketSendTranslatedMessage("", "$InvTribu_InvitationEnvoyee", new String[]{"<V>" + playerName + "</V>"}));
        target.broadcastPacket(new PacketSendTribeHouseInvitation(player.getPlayerName(), tribeName));
    }
}