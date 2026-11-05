package org.nekodancer.command.commands.luadev;

// Imports
import java.util.ArrayList;
import java.util.List;
import org.nekodancer.GamePlayer;
import org.nekodancer.GameServer;
import org.nekodancer.command.Command;
import org.nekodancer.command.CommandHandler;
import org.nekodancer.data.objects.StaffRoleType;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

@Command(name = "lslua", description = "Lists connected lua developers and their channels.", permission = {Command.CommandPermission.LUADEV, Command.CommandPermission.MODERATOR, Command.CommandPermission.ADMINISTRATOR})
@SuppressWarnings("unused")
public final class Lslua implements CommandHandler {
    @Override
    public void execute(GamePlayer player, GameServer server, List<String> args) {
        List<String[]> staffInfo = new ArrayList<>();
        for (GamePlayer client : server.getPlayers().values()) {
            if (client.getPlayerData().getStaffRoles().contains(StaffRoleType.LUADEV)) {
                staffInfo.add(new String[]{client.getPlayerCommunity(), client.getPlayerName(), client.getRoomName()});
            }
        }

        NetByteArray packet = new NetByteArray();
        packet.writeUnsignedByte(8);
        packet.writeUnsignedByte(staffInfo.size());
        for (String[] info : staffInfo) {
            packet.writeUTF(info[0], true);
            packet.writeUTF(info[1], true);
            packet.writeUTF(info[2], true);
        }

        packet.writeBoolean(true);
        player.broadcastPacket(new PSSendPacket(PacketOpcodes.SEND_STAFF_LIST) {
            @Override
            public PSByteArray build() throws PSSendPacketException {
                return packet;
            }
        });
    }
}