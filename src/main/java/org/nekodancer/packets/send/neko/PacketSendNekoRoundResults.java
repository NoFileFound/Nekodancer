package org.nekodancer.packets.send.neko;

// Imports
import java.util.List;
import org.nekodancer.data.objects.RoomPlayerInfo;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

public final class PacketSendNekoRoundResults extends PSSendPacket {
    private final List<RoomPlayerInfo> playerInfoList;

    /**
     * Creates a new send packet of SEND_NEKO_ROUND_RESULTS.
     * @param playerInfoList The list of players.
     */
    public PacketSendNekoRoundResults(List<RoomPlayerInfo> playerInfoList) {
        super(PacketOpcodes.SEND_NEKO_ROUND_RESULTS);
        this.playerInfoList = playerInfoList;
    }


    @Override
    public PSByteArray build() throws PSSendPacketException {
        NetByteArray packet = new NetByteArray();
        packet.writeByte(this.playerInfoList.size());
        for (var info : this.playerInfoList) {
            packet.writeShort((short) info.sessionId);
            packet.writeShort((short) info.playerScore);
            packet.writeByte(info.noMissRate);
            packet.writeShort((short) info.totalMisses);
        }

        return packet;
    }
}