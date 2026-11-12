package org.nekodancer.command.commands.player;

// Imports
import java.util.List;
import org.nekodancer.GamePlayer;
import org.nekodancer.GameServer;
import org.nekodancer.command.Command;
import org.nekodancer.command.CommandHandler;
import org.nekodancer.database.data.PlayerEntity;

// Packets
import org.nekodancer.packets.send.neko.PacketSendNekoPlayerProfile;

@Command(name = "profile", usage = "[playerName]", description = "Player stats information", aliases = {"perfil", "profiel", "profil"})
@SuppressWarnings("unused")
public final class Profile implements CommandHandler {
    @Override
    public void execute(GamePlayer player, GameServer server, List<String> args) {
        String playerName = (args.isEmpty()) ? player.getPlayerName() : args.getFirst();
        PlayerEntity playerAccount = server.getDatabase().loadPlayerByName(playerName);
        if(playerAccount != null)
            player.broadcastPacket(new PacketSendNekoPlayerProfile(playerAccount));
    }
}