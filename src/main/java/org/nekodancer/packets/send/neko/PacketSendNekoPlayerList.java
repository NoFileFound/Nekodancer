package org.nekodancer.packets.send.neko;

// Imports
import java.util.List;
import org.nekodancer.data.objects.RoomPlayerInfo;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

public final class PacketSendNekoPlayerList extends PSSendPacket {
    private final List<RoomPlayerInfo> roomPlayerList;

    /**
     * Creates a new packet of SEND_NEKO_PLAYER_LIST.
     * @param roomPlayerList The player's list.
     */
    public PacketSendNekoPlayerList(List<RoomPlayerInfo> roomPlayerList) {
        super(PacketOpcodes.SEND_NEKO_PLAYER_LIST);
        this.roomPlayerList = roomPlayerList;
    }


    @Override
    public PSByteArray build() throws PSSendPacketException {
        NetByteArray packet = new NetByteArray();
        packet.writeShort((short) this.roomPlayerList.size());
        for (var info : this.roomPlayerList) {
            packet.writeUTF(info.playerName, true);
            packet.writeShort((short) info.sessionId);
            packet.writeShort((short) info.positionX);
            packet.writeShort((short) info.positionY);
            packet.writeShort((short) info.playerScore);
            packet.writeBoolean(info.isDead);
            packet.writeByte(info.playerFurId);
            packet.writeByte(info.shopItemList.size());
            for (var shopItem : info.shopItemList) {
                packet.writeByte(shopItem.getItemCategory());
                packet.writeShort((short) shopItem.getItemId());
            }
        }

        return packet;
    }
}