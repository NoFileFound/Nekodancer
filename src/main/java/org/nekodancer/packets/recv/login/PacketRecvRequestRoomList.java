package org.nekodancer.packets.recv.login;

// Imports
import org.nekodancer.GamePlayer;
import org.nekodancer.GameRoom;
import org.nekodancer.GameServer;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.network.NetSession;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSPacket;
import org.psframework.network.PSReceivePacket;
import org.psframework.network.PSReceivePacketException;

// Packets
import org.nekodancer.packets.send.login.PacketSendRoomList;

@SuppressWarnings("unused")
@PSPacket(id = PacketOpcodes.RECV_REQUEST_ROOM_LIST)
public final class PacketRecvRequestRoomList extends PSReceivePacket<GameServer, NetSession, NetByteArray> {
    @Override
    public void handle(GameServer gameServer, NetSession client, NetByteArray psHeader, NetByteArray psData) throws PSReceivePacketException {
        int gameModeId = psData.readByte();
        GamePlayer player = client.getPlayer();
        String defaultRoomName = "*1";
        GameRoom defaultRoom = gameServer.getRooms().get("INT-" + defaultRoomName);
        int roomPlayers = (defaultRoom != null ? defaultRoom.getPlayers().size() : 0);
        player.broadcastPacket(new PacketSendRoomList(gameModeId, gameServer.getRoomList(player.getPlayerCommunity()), defaultRoomName, roomPlayers));
    }
}