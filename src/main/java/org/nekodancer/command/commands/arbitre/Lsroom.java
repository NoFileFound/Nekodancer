package org.nekodancer.command.commands.arbitre;

// Imports
import java.util.List;
import org.nekodancer.GamePlayer;
import org.nekodancer.GameRoom;
import org.nekodancer.GameServer;
import org.nekodancer.command.Command;
import org.nekodancer.command.CommandHandler;
import org.nekodancer.utils.Utils;

@Command(name = "lsroom", usage = "(roomName)", description = "Lists the players present in the room.", permission = {Command.CommandPermission.ARBITRE, Command.CommandPermission.MODERATOR, Command.CommandPermission.ADMINISTRATOR})
@SuppressWarnings("unused")
public final class Lsroom implements CommandHandler {
    @Override
    public void execute(GamePlayer player, GameServer server, List<String> args) {
        String roomName = (!args.isEmpty()) ? String.join(" ", args) : player.getRoomName();
        roomName = Utils.formatRoomName(roomName, player.getPlayerCommunity(), true);
        GameRoom room = server.getRooms().get(roomName);
        if (room == null) {
            CommandHandler.sendServerMessage(player, String.format("The room <BV>[%s]</BV> does not exist.", roomName));
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Players in room [%s]: %d<br>", roomName, room.getPlayers().size()));
        for (GamePlayer client : room.getPlayers().values()) {
            String ip = Utils.encodeIP(client.getIpAddress());
            String line = String.format("<BL>%s / </BL><font color='%s'>%s</font> <G>(%s)</G><br>", client.getPlayerName(), Utils.colorIP(ip), ip, Utils.getCountryNameByCode(client.getCountryCode()));
            builder.append(line);
        }

        CommandHandler.sendServerMessage(player, builder.toString());
    }
}