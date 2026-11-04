package org.nekodancer.packets.recv.language;

// Imports
import org.nekodancer.GameServer;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.network.NetSession;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSPacket;
import org.psframework.network.PSReceivePacket;
import org.psframework.network.PSReceivePacketException;

// Packets
import org.nekodancer.packets.send.language.PacketSendLanguageList;

@SuppressWarnings("unused")
@PSPacket(id = PacketOpcodes.RECV_LANGUAGE_LIST)
public final class PacketRecvLanguageList extends PSReceivePacket<GameServer, NetSession, NetByteArray> {
    @Override
    public void handle(GameServer gameServer, NetSession client, NetByteArray psHeader, NetByteArray psData) throws PSReceivePacketException {
        client.sendData(new PacketSendLanguageList(client.getCountryLanguageCode(), client.getCountryCode()));
    }
}