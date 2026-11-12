package org.nekodancer.packets.send.neko;

// Imports
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

public final class PacketSendNekoPlayerDirtyMove extends PSSendPacket {
    private final int sessionIdAttacker;
    private final int sessionIdTarget;
    private final int attackType;
    private final int attackDuration;

    /**
     * Creates a new packet of SEND_NEKO_PLAYER_DIRTY_MOVE.
     * @param sessionIdAttacker The player's session id.
     * @param sessionIdTarget The second player's session id.
     */
    public PacketSendNekoPlayerDirtyMove(int sessionIdAttacker, int sessionIdTarget, int attackType, int attackDuration) {
        super(PacketOpcodes.SEND_NEKO_PLAYER_DIRTY_MOVE);
        this.sessionIdAttacker = sessionIdAttacker;
        this.sessionIdTarget = sessionIdTarget;
        this.attackType = attackType;
        this.attackDuration = attackDuration;
    }


    @Override
    public PSByteArray build() throws PSSendPacketException {
        return new PSByteArray().writeShort((short) this.sessionIdAttacker).writeShort((short) this.sessionIdTarget).writeByte(this.attackType).writeShort((short) this.attackDuration);
    }
}