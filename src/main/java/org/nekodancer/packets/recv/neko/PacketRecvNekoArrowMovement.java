package org.nekodancer.packets.recv.neko;

import org.nekodancer.GameServer;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.network.NetSession;
import org.psframework.network.PSPacket;
import org.psframework.network.PSReceivePacket;
import org.psframework.network.PSReceivePacketException;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

@PSPacket(id = 26373)
public class PacketRecvNekoArrowMovement extends PSReceivePacket<GameServer, NetSession, NetByteArray> {
    @Override
    public void handle(GameServer gameServer, NetSession client, NetByteArray psHeader, NetByteArray psData) throws PSReceivePacketException {
        client.getPlayer().broadcastPacket(new PSSendPacket(26384) {
            @Override
            public PSByteArray build() throws PSSendPacketException {
                NetByteArray packet = new NetByteArray();
                packet.writeByte(1);
                packet.writeInt(psData.readInt());
                packet.writeByte(psData.readByte() - 1);
                return packet;
            }
        });
    }
}