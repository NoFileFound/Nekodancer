package org.nekodancer.packets.recv.cafe;

// Imports
import org.nekodancer.GamePlayer;
import org.nekodancer.GameServer;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.network.NetSession;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSPacket;
import org.psframework.network.PSReceivePacket;
import org.psframework.network.PSReceivePacketException;
import org.psframework.utils.PSTimer;

@SuppressWarnings("unused")
@PSPacket(id = PacketOpcodes.RECV_CAFE_TOPIC_LIST)
public final class PacketRecvCafeTopicList extends PSReceivePacket<GameServer, NetSession, NetByteArray> {
    @Override
    public void handle(GameServer gameServer, NetSession client, NetByteArray psHeader, NetByteArray psData) throws PSReceivePacketException {
        GamePlayer player = client.getPlayer();
        PSTimer timer = gameServer.getParseCafe().getCreateCafeReloadTimer(player.getPlayerName());
        if (timer.getRemainingTime() < 0) {
            gameServer.getParseCafe().reloadCafeTopics(player);
            timer.schedule();
        }
    }
}