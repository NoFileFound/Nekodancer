package org.nekodancer.packets.recv.tribulle;

// Imports
import org.nekodancer.GameServer;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.network.NetSession;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSPacket;
import org.psframework.network.PSReceivePacket;
import org.psframework.network.PSReceivePacketException;

@SuppressWarnings("unused")
@PSPacket(id = PacketOpcodes.RECV_HANDLE_NEW_TRIBULLE_PROTOCOL)
public final class RecvHandleNewTribulleProtocol extends PSReceivePacket<GameServer, NetSession, NetByteArray> {
    @Override
    public void handle(GameServer gameServer, NetSession client, NetByteArray psHeader, NetByteArray psData) throws PSReceivePacketException {
        if (client.getPlayer().getIsGuest()) {
            client.disconnect();
            return;
        }

        psData.decryptPacket(gameServer.getConfig().getSWFConfig().getPacketKeys(), psHeader.readByte());
        int tribulleCode = psData.readShort();
        int tribulleId = psData.readInt();
        client.getPlayer().getParseTribulle().handleTribullePacket(tribulleCode, tribulleId, psData);
    }
}