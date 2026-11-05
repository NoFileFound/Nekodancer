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
import org.nekodancer.packets.send.informations.PacketSendRequestInfo;

@SuppressWarnings("unused")
@PSPacket(id = PacketOpcodes.RECV_REQUEST_BROWSER_AGENT)
public final class PacketRecvRequestBrowserAgent extends PSReceivePacket<GameServer, NetSession, NetByteArray> {
    @Override
    public void handle(GameServer gameServer, NetSession client, NetByteArray psHeader, NetByteArray psData) throws PSReceivePacketException {
        client.getPlayer().broadcastPacket(new PacketSendRequestInfo());
    }
}