package org.nekodancer.packets.send.neko;

// Imports
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

public final class PacketSendNekoPlayerRemove extends PSSendPacket {
    private final int sessionId;

    /**
     * Creates a new packet of SEND_NEKO_PLAYER_REMOVE.
     * @param sessionId The player's session id.
     */
    public PacketSendNekoPlayerRemove(int sessionId) {
        super(PacketOpcodes.SEND_NEKO_PLAYER_REMOVE);
        this.sessionId = sessionId;
    }


    @Override
    public PSByteArray build() throws PSSendPacketException {
        return new PSByteArray().writeShort((short) this.sessionId);
    }
}