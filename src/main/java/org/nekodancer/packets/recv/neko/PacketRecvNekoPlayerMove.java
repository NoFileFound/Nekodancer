package org.nekodancer.packets.recv.neko;

// Imports
import org.nekodancer.GamePlayer;
import org.nekodancer.GameServer;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.network.NetSession;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSPacket;
import org.psframework.network.PSReceivePacket;
import org.psframework.network.PSReceivePacketException;

// Packets
import org.nekodancer.packets.send.neko.PacketSendNekoPlayerMove;

@SuppressWarnings("unused")
@PSPacket(id = PacketOpcodes.RECV_NEKO_PLAYER_MOVE)
public final class PacketRecvNekoPlayerMove extends PSReceivePacket<GameServer, NetSession, NetByteArray> {
    @Override
    public void handle(GameServer gameServer, NetSession client, NetByteArray psHeader, NetByteArray psData) throws PSReceivePacketException {
        int posX = psData.readShort();
        int posY = psData.readShort();
        GamePlayer player = client.getPlayer();
        if (player.getPlayerRoom().getIsCurrentlyPlay())
            return;

        player.positionX = posX;
        player.positionY = posY;
        player.getPlayerRoom().broadcastPacket(player, new PacketSendNekoPlayerMove(player.getSessionId(), posX, posY));
    }
}