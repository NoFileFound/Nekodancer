package org.nekodancer.packets.send.neko;

// Imports
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

public final class PacketSendNekoPlayerDanceMove extends PSSendPacket {
    private final int sessionId;
    private final int direction;
    private final int type;

    /**
     * Creates a new packet of SEND_NEKO_PLAYER_DANCE_MOVE.
     * @param sessionId The player's session id.
     * @param direction The dance move's direction.
     * @param type The dance move's type.
     */
    public PacketSendNekoPlayerDanceMove(int sessionId, int direction, int type) {
        super(PacketOpcodes.SEND_NEKO_PLAYER_DANCE_MOVE);
        this.sessionId = sessionId;
        this.direction = direction;
        this.type = type;
    }


    @Override
    public PSByteArray build() throws PSSendPacketException {
        return new PSByteArray().writeShort((short) this.sessionId).writeByte(this.direction).writeByte(this.type);
    }
}