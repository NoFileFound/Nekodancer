package org.nekodancer.command.commands.modo;

// Imports
import java.util.List;
import org.nekodancer.GamePlayer;
import org.nekodancer.GameServer;
import org.nekodancer.command.Command;
import org.nekodancer.command.CommandHandler;

@Command(name = "chatlog", usage = "[playerName]", description = "Performs the same functionality as Chatlog in modopwet.", permission = {Command.CommandPermission.MODERATOR, Command.CommandPermission.ADMINISTRATOR}, requiredArgs = 1)
@SuppressWarnings("unused")
public final class Chatlog implements CommandHandler {
    @Override
    public void execute(GamePlayer player, GameServer server, List<String> args) {
        server.getParseModopwet().handleOpenChatLog(player, args.getFirst());
    }
}