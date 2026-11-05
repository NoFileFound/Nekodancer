package org.nekodancer.packets.recv.chat;

// Imports
import org.nekodancer.GameServer;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.network.NetSession;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSPacket;
import org.psframework.network.PSReceivePacket;
import org.psframework.network.PSReceivePacketException;

@SuppressWarnings("unused")
@PSPacket(id = PacketOpcodes.RECV_CHAT_COMMAND)
public final class PacketRecvChatCommand extends PSReceivePacket<GameServer, NetSession, NetByteArray> {
    @Override
    public void handle(GameServer gameServer, NetSession client, NetByteArray psHeader, NetByteArray psData) throws PSReceivePacketException {
        psData.decryptPacket(gameServer.getConfig().getSWFConfig().getPacketKeys(), psHeader.readByte());
        gameServer.getCommandManager().invokeGameCommand(client.getPlayer(), psData.readUTF(), false);
    }
}