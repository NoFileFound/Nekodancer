package org.nekodancer.packets.send.neko;

// Imports
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

public final class PacketSendNekoPlayerAttack extends PSSendPacket {
    private final int sessionIdAttacker;
    private final int sessionIdTarget;

    /**
     * Creates a new packet of SEND_NEKO_PLAYER_ATTACK.
     * @param sessionIdAttacker The player's session id.
     * @param sessionIdTarget The second player's session id.
     */
    public PacketSendNekoPlayerAttack(int sessionIdAttacker, int sessionIdTarget) {
        super(PacketOpcodes.SEND_NEKO_PLAYER_ATTACK);
        this.sessionIdAttacker = sessionIdAttacker;
        this.sessionIdTarget = sessionIdTarget;
    }


    @Override
    public PSByteArray build() throws PSSendPacketException {
        return new PSByteArray().writeShort((short) this.sessionIdAttacker).writeShort((short) this.sessionIdTarget);
    }
}