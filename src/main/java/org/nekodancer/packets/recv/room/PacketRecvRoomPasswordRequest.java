package org.nekodancer.packets.recv.room;

// Imports
import org.nekodancer.GameServer;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.network.NetSession;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSPacket;
import org.psframework.network.PSReceivePacket;
import org.psframework.network.PSReceivePacketException;

@SuppressWarnings("unused")
@PSPacket(id = PacketOpcodes.RECV_ROOM_PASSWORD_REQUEST)
public final class PacketRecvRoomPasswordRequest extends PSReceivePacket<GameServer, NetSession, NetByteArray> {
    @Override
    public void handle(GameServer gameServer, NetSession client, NetByteArray psHeader, NetByteArray psData) throws PSReceivePacketException {
        String roomPassword = psData.readUTF();
        String roomName = psData.readUTF();
        client.getPlayer().sendEnterRoom(roomName, roomPassword);
    }
}