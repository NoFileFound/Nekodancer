package org.nekodancer.packets.recv.login;

// Imports
import org.nekodancer.GameServer;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.network.NetSession;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSPacket;
import org.psframework.network.PSReceivePacket;
import org.psframework.network.PSReceivePacketException;

// Packets
import org.nekodancer.packets.send.login.PacketSendStaffServerPing;

@SuppressWarnings("unused")
@PSPacket(id = PacketOpcodes.RECV_STAFF_SERVER_PING)
public final class PacketRecvStaffServerPing extends PSReceivePacket<GameServer, NetSession, NetByteArray> {
    @Override
    public void handle(GameServer gameServer, NetSession client, NetByteArray psHeader, NetByteArray psData) throws PSReceivePacketException {
        if (client.getPlayer().getPlayerData().getStaffRoles().isEmpty() && !gameServer.getConfig().getIsDebug())
            return;

        client.getPlayer().broadcastPacket(new PacketSendStaffServerPing());
    }
}