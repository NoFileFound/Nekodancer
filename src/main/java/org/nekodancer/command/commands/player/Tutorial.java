package org.nekodancer.command.commands.player;

// Imports
import java.util.List;
import org.nekodancer.GamePlayer;
import org.nekodancer.GameServer;
import org.nekodancer.command.Command;
import org.nekodancer.command.CommandHandler;

@Command(name = "tutorial", description = "Sends you to the tutorial.")
@SuppressWarnings("unused")
public final class Tutorial implements CommandHandler {
    @Override
    public void execute(GamePlayer player, GameServer server, List<String> args) {
        player.sendEnterRoom("\u0003[Tutorial] " + player.getPlayerName(), "");
    }
}