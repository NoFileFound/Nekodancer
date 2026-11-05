package org.nekodancer.packets.recv.room;

// Imports
import org.nekodancer.GamePlayer;
import org.nekodancer.GameServer;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.network.NetSession;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSPacket;
import org.psframework.network.PSReceivePacket;
import org.psframework.network.PSReceivePacketException;

@SuppressWarnings("unused")
@PSPacket(id = PacketOpcodes.RECV_ENTER_ROOM)
public final class PacketRecvEnterRoom extends PSReceivePacket<GameServer, NetSession, NetByteArray> {
    @Override
    public void handle(GameServer gameServer, NetSession client, NetByteArray psHeader, NetByteArray psData) throws PSReceivePacketException {
        String community = psData.readUTF();
        String roomName = psData.readUTF();
        boolean salonAuto = psData.readBoolean();
        GamePlayer player = client.getPlayer();
        if (roomName.isEmpty() || salonAuto) {
            player.sendEnterRoom(gameServer.getRecommendedRoom(roomName, player.getPlayerCommunity()), "");
            return;
        }

        player.sendEnterRoom(roomName, "");
    }
}