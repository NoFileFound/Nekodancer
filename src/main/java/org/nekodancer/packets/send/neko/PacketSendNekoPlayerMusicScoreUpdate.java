package org.nekodancer.packets.send.neko;

// Imports
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

public final class PacketSendNekoPlayerMusicScoreUpdate extends PSSendPacket {
    private final int playerScore;
    private final int scoreAccuracy;
    private final int bestStreak;
    private final int playerHealth;

    /**
     * Creates a new send packet of SEND_NEKO_PLAYER_MUSIC_SCORE_UPDATE.
     * @param playerScore The player's score on the music.
     * @param scoreAccuracy The player's score accuracy.
     * @param bestStreak The player's best streak.
     * @param playerHealth The player's health.
     */
    public PacketSendNekoPlayerMusicScoreUpdate(int playerScore, int scoreAccuracy, int bestStreak, int playerHealth) {
        super(PacketOpcodes.SEND_NEKO_PLAYER_MUSIC_SCORE_UPDATE);
        this.playerScore = playerScore;
        this.scoreAccuracy = scoreAccuracy;
        this.bestStreak = bestStreak;
        this.playerHealth = playerHealth;
    }


    @Override
    public PSByteArray build() throws PSSendPacketException {
        return new PSByteArray().writeShort((short) this.playerScore).writeByte(this.scoreAccuracy).writeShort((short) this.bestStreak).writeByte(this.playerHealth);
    }
}