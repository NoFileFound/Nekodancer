package org.nekodancer.command.commands.player;

// Imports
import java.util.List;
import java.util.regex.Pattern;
import org.nekodancer.GamePlayer;
import org.nekodancer.GameServer;
import org.nekodancer.command.Command;
import org.nekodancer.command.CommandHandler;

@Command(name = "pw", description = "Changes the password of current room.", permission = Command.CommandPermission.ROOM_OWNER)
@SuppressWarnings("unused")
public final class Pw implements CommandHandler {
    private static final Pattern ROOM_NAME_PATTERN = Pattern.compile("^.*[^\\d\\s].*$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @Override
    public void execute(GamePlayer player, GameServer server, List<String> args) {
        String password = String.join(" ", args);
        if (ROOM_NAME_PATTERN.matcher(player.getPlayerRoom().getRoomNameSuffix()).matches() && !player.getPlayerRoom().getRoomNameSuffix().matches("\\d+")) {
            player.getPlayerRoom().roomPassword = password;
            CommandHandler.sendTranslatedMessage(player, "", (password.isEmpty()) ? "$MDP_Desactive" : "$Mot_De_Passe : " + password, new String[]{});
        }
    }
}