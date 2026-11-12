package org.nekodancer.packets.send.neko;

// Imports
import java.util.List;
import org.nekodancer.database.data.PlayerEntity;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

public final class PacketSendNekoPlayerPersonalMusicPlaylist extends PSSendPacket {
    private final List<PlayerEntity.MusicInfo> personalMusicPlaylist;
    private final boolean canSelectMusic;
    private final int lastMusicId;

    /**
     * Creates a new send packet of SEND_NEKO_PLAYER_PERSONAL_MUSIC_PLAYLIST.
     * @param personalMusicPlaylist The player's personal music playlist.
     * @param canSelectMusic Can the player select a music for the next round.
     * @param lastMusicId The last's music id.
     */
    public PacketSendNekoPlayerPersonalMusicPlaylist(List<PlayerEntity.MusicInfo> personalMusicPlaylist, boolean canSelectMusic, int lastMusicId) {
        super(PacketOpcodes.SEND_NEKO_PLAYER_PERSONAL_MUSIC_PLAYLIST);
        this.personalMusicPlaylist = personalMusicPlaylist;
        this.canSelectMusic = canSelectMusic;
        this.lastMusicId = lastMusicId;
    }

    @Override
    public PSByteArray build() throws PSSendPacketException {
        NetByteArray packet = new NetByteArray();
        packet.writeByte(this.personalMusicPlaylist.size());
        for (var info : this.personalMusicPlaylist) {
            packet.writeBoolean(this.canSelectMusic);
            packet.writeInt(info.getMusicId());
            packet.writeUTF(info.getMusicTitle(), true);
            if (this.canSelectMusic) {
                packet.writeUTF(info.getMusicBackgroundImage(), true);
                packet.writeBoolean(info.getMusicId() == this.lastMusicId);
            }
        }

        return packet;
    }
}