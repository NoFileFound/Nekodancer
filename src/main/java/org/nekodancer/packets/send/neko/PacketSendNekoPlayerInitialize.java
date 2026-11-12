package org.nekodancer.packets.send.neko;

import org.nekodancer.network.NetByteArray;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

public class PacketSendNekoPlayerInitialize extends PSSendPacket {
    private final String playerName;
    private final String songTitle;
    private final String songUrl;
    private final int songPositionMs;
    //private final boolean isEnabled;

    /**
     * Creates a new send packet of SEND_NEKO_PLAYER_INITIALIZE.
     * @param playerName The name of the player who started the song.
     * @param songTitle The title of the song.
     * @param songUrl The song's url.
     * @param songPositionMs The song's position on the track.
     */
    public PacketSendNekoPlayerInitialize(String playerName, String songTitle, String songUrl, int songPositionMs) {
        super(PacketOpcodes.SEND_NEKO_PLAYER_INITIALIZE);
        this.playerName = playerName;
        this.songTitle = songTitle;
        this.songUrl = songUrl;
        this.songPositionMs = songPositionMs;
    }

    @Override
    public PSByteArray build() throws PSSendPacketException {
        NetByteArray packet = new NetByteArray();
        packet.writeUTF(this.playerName, true);
        packet.writeUTF(this.songTitle, true);
        packet.writeUTF(this.songUrl, true);
        packet.writeInt(this.songPositionMs + 2);
        packet.writeBoolean(true); // enabled

        packet.writeInt(100);
        packet.writeByte(0x03);
        packet.writeByte(0);

        packet.writeInt(200);
        packet.writeByte(0x0C);
        packet.writeByte(0);
        return packet;
    }
}

/// TODO: FIX THIS.