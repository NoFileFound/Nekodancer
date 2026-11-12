package org.nekodancer.packets.send.neko;

// Imports
import org.nekodancer.data.objects.RoomPlayerInfo;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

public final class PacketSendNekoNewPlayer extends PSSendPacket {
    private final RoomPlayerInfo playerInfo;

    /**
     * Creates a new player of SEND_NEKO_NEW_PLAYER.
     * @param playerInfo The player's room information.
     */
    public PacketSendNekoNewPlayer(RoomPlayerInfo playerInfo) {
        super(PacketOpcodes.SEND_NEKO_NEW_PLAYER);
        this.playerInfo = playerInfo;
    }


    @Override
    public PSByteArray build() throws PSSendPacketException {
        NetByteArray packet = new NetByteArray();
        packet.writeUTF(this.playerInfo.playerName, true);
        packet.writeShort((short) this.playerInfo.sessionId);
        packet.writeShort((short) this.playerInfo.positionX);
        packet.writeShort((short) this.playerInfo.positionY);
        packet.writeShort((short) this.playerInfo.playerScore);
        packet.writeBoolean(this.playerInfo.isDead);
        packet.writeByte(this.playerInfo.playerFurId);
        packet.writeByte(this.playerInfo.shopItemList.size());
        for (var shopItem : this.playerInfo.shopItemList) {
            packet.writeByte(shopItem.getItemCategory());
            packet.writeShort((short) shopItem.getItemId());
        }

        return packet;
    }
}