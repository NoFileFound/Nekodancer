package org.nekodancer.packets.send.neko;

// Imports
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

public final class PacketSendNekoPlayerDeath extends PSSendPacket {
    private final int sessionId;

    /**
     * Creates a new packet of SEND_NEKO_PLAYER_DEATH.
     * @param sessionId The player's session id.
     */
    public PacketSendNekoPlayerDeath(int sessionId) {
        super(PacketOpcodes.SEND_NEKO_PLAYER_DEATH);
        this.sessionId = sessionId;
    }


    @Override
    public PSByteArray build() throws PSSendPacketException {
        return new PSByteArray().writeShort((short) this.sessionId);
    }
}