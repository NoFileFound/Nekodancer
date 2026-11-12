package org.nekodancer.command.commands.debug;

// Imports
import java.util.List;
import org.nekodancer.GamePlayer;
import org.nekodancer.GameServer;
import org.nekodancer.command.Command;
import org.nekodancer.command.CommandHandler;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.packets.send.neko.PacketSendNekoPlayerInitialize;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

@Command(name = "test", description = "test.", permission = Command.CommandPermission.DEBUG_ONLY)
@SuppressWarnings("unused")
public class Test implements CommandHandler {
    @Override
    public void execute(GamePlayer player, GameServer server, List<String> args) {
        player.broadcastPacket(new PacketSendNekoPlayerInitialize("", "", "", 0));

        player.broadcastPacket(new PSSendPacket(26428-5) {
            @Override
            public PSByteArray build() throws PSSendPacketException {
                NetByteArray packet = new NetByteArray();
                packet.writeInt(1000);
                packet.writeByte(1);
                packet.writeByte(2);
                packet.writeByte(1);
                packet.writeShort((short)100);
                packet.writeByte(2);
                packet.writeShort((short)200);
                packet.writeByte(1);
                packet.writeShort((short)10);
                packet.writeByte(1);
                packet.writeByte(2);
                packet.writeByte(1);
                packet.writeShort((short)100);
                packet.writeByte(2);
                packet.writeShort((short)200);
                packet.writeShort((short)1);
                packet.writeShort((short)20);
                packet.writeByte(2);
                packet.writeShort((short)500);
                packet.writeByte(2);
                packet.writeByte(3);
                packet.writeShort((short)300);
                packet.writeByte(4);
                packet.writeShort((short)400);
                return packet;
            }
        });
    }
}