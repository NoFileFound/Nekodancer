package org.nekodancer.packets.send.login;

// Imports
import java.util.List;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;
import org.nekodancer.GameRoom;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.packets.PacketOpcodes;

public final class PacketSendRoomList extends PSSendPacket {
    private final List<GameRoom> roomList;
    private final int gameModeId;
    private final String defaultRoomName;
    private final int defaultRoomPlayers;
    private final byte[] gameModeList = new byte[]{50};

    /**
     * Creates a new packet of SEND_ROOM_LIST.
     * @param roomList The room's list.
     * @param defaultRoomName The default's room name.
     * @param defaultRoomPlayers The default's room players.
     */
    public PacketSendRoomList(int gameModeId, List<GameRoom> roomList, String defaultRoomName, int defaultRoomPlayers) {
        super(PacketOpcodes.SEND_ROOM_LIST);
        this.roomList = roomList;
        this.gameModeId = gameModeId;
        this.defaultRoomName = defaultRoomName;
        this.defaultRoomPlayers = defaultRoomPlayers;
    }


    @Override
    public PSByteArray build() throws PSSendPacketException {
        NetByteArray packet = new NetByteArray();
        packet.writeByte(this.gameModeList.length);
        for (byte roomType : this.gameModeList)
            packet.writeByte(roomType);

        packet.writeByte(this.gameModeId);
        for (GameRoom room : this.roomList) {
            if (room.getRoomName().equals("INT-" + this.defaultRoomName))
                continue;

            packet.writeByte(0);
            packet.writeUTF(room.getRoomCommunity(), true);
            packet.writeUTF(room.getRoomCommunity(), true);
            packet.writeUTF(room.getRoomNameSuffix(), true);
            packet.writeUnsignedShort(room.getPlayers().size());
            packet.writeUnsignedByte(room.maximumPlayers);
            packet.writeBoolean(false);
        }

        if (this.gameModeId != 18) {
            packet.writeByte(1);
            packet.writeUTF("INT", true);
            packet.writeUTF("INT", true);
            packet.writeUTF(this.defaultRoomName, true);
            packet.writeUTF(String.valueOf(this.defaultRoomPlayers), true);
            packet.writeUTF("", true);
            packet.writeUTF("", true);
        }

        return packet;
    }
}