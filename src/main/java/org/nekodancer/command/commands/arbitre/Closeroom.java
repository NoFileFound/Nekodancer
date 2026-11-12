package org.nekodancer.command.commands.arbitre;

// Imports
import java.util.List;
import org.nekodancer.GamePlayer;
import org.nekodancer.GameRoom;
import org.nekodancer.GameServer;
import org.nekodancer.command.Command;
import org.nekodancer.command.CommandHandler;
import org.nekodancer.data.objects.StaffRoleType;
import org.nekodancer.utils.Utils;

@Command(name = "closeroom", usage = "(roomName)", description = "Kicks everyone from the given room.", permission = {Command.CommandPermission.ARBITRE, Command.CommandPermission.MODERATOR, Command.CommandPermission.ADMINISTRATOR})
@SuppressWarnings("unused")
public final class Closeroom implements CommandHandler {
    @Override
    public void execute(GamePlayer player, GameServer server, List<String> args) {
        String roomName = (!args.isEmpty()) ? String.join(" ", args) : player.getRoomName();
        roomName = Utils.formatRoomName(roomName, player.getPlayerCommunity(), true);
        GameRoom room = server.getRooms().get(roomName);
        if (room == null) {
            CommandHandler.sendServerMessage(player, String.format("The room <BV>[%s]</BV> does not exist.", roomName));
            return;
        }

        for (GamePlayer client : room.getPlayers().values())
            client.sendEnterRoom(server.getRecommendedRoom("", client.getPlayerCommunity()), "");

        CommandHandler.sendServerMessage(player, String.format("The room <BV>[%s]</BV> got closed.", roomName));
        server.sendServerMessageOthers(player, String.format("%s closed the room [%s].", player.getPlayerName(), roomName), StaffRoleType.ARBITRE);
    }
}