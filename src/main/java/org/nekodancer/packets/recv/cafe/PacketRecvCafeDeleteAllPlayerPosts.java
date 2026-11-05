package org.nekodancer.packets.recv.cafe;

// Imports
import org.nekodancer.GameServer;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.network.NetSession;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSPacket;
import org.psframework.network.PSReceivePacket;
import org.psframework.network.PSReceivePacketException;

@SuppressWarnings("unused")
@PSPacket(id = PacketOpcodes.RECV_CAFE_DELETE_ALL_PLAYER_POSTS)
public final class PacketRecvCafeDeleteAllPlayerPosts extends PSReceivePacket<GameServer, NetSession, NetByteArray> {
    @Override
    public void handle(GameServer gameServer, NetSession client, NetByteArray psHeader, NetByteArray psData) throws PSReceivePacketException {
        gameServer.getParseCafe().sendDeleteAllCafePlayerPosts(client.getPlayer(), psData.readInt(), psData.readUTF());
    }
}