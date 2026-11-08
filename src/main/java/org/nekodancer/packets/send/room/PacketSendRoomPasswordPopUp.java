package org.nekodancer.packets.send.room;

// Imports
import org.nekodancer.network.NetByteArray;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

public final class PacketSendRoomPasswordPopUp extends PSSendPacket {
    private final String roomName;

    /**
     * Creates a new packet of SEND_ROOM_PASSWORD_POPUP.
     * @param roomName The room's name.
     */
    public PacketSendRoomPasswordPopUp(String roomName) {
        super(PacketOpcodes.SEND_ROOM_PASSWORD_POPUP);
        this.roomName = roomName;
    }


    @Override
    public PSByteArray build() throws PSSendPacketException {
        return new NetByteArray().writeUTF(this.roomName, true);
    }
}