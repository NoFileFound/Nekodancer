package org.nekodancer.packets.send.neko;

// Imports
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

public final class PacketSendNekoPlayerScoreUpdate extends PSSendPacket {
    private final int sessionId;
    private final int playerScore;

    /**
     * Creates a new send packet of SEND_NEKO_PLAYER_SCORE_UPDATE.
     * @param sessionId The player's session id.
     * @param playerScore The player's score on the music.
     * @param bestStreak The player's best combo.
     * @param playerHealth The player's health.
     */
    public PacketSendNekoPlayerScoreUpdate(int sessionId, int playerScore, int bestStreak, int playerHealth) {
        super(PacketOpcodes.SEND_NEKO_PLAYER_SCORE_UPDATE);
        this.sessionId = sessionId;
        this.playerScore = playerScore;
    }


    @Override
    public PSByteArray build() throws PSSendPacketException {
        return new PSByteArray().writeShort((short) this.sessionId).writeShort((short) this.playerScore);
    }
}