package org.nekodancer.packets.send.neko;

// Imports
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

public final class PacketSendNekoPlayerMove extends PSSendPacket {
    private final int sessionId;
    private final int posX;
    private final int posY;

    /**
     * Creates a new packet of SEND_NEKO_PLAYER_MOVE.
     * @param sessionId The player's session id.
     * @param posX The player's position on X axis.
     * @param posY The player's position on Y axis.
     */
    public PacketSendNekoPlayerMove(int sessionId, int posX, int posY) {
        super(PacketOpcodes.SEND_NEKO_PLAYER_MOVE);
        this.sessionId = sessionId;
        this.posX = posX;
        this.posY = posY;
    }


    @Override
    public PSByteArray build() throws PSSendPacketException {
        return new PSByteArray().writeShort((short) this.sessionId).writeShort((short) this.posX).writeShort((short) this.posY);
    }
}