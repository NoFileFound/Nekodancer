package org.nekodancer.packets.recv.neko;

// Imports
import org.nekodancer.GamePlayer;
import org.nekodancer.GameServer;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.network.NetSession;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSPacket;
import org.psframework.network.PSReceivePacket;
import org.psframework.network.PSReceivePacketException;

// Packets
import org.nekodancer.packets.send.neko.PacketSendNekoPlayerPersonalMusicPlaylist;

@SuppressWarnings("unused")
@PSPacket(id = PacketOpcodes.RECV_NEKO_PLAYER_DELETE_MUSIC_FROM_PERSONAL_MUSIC_PLAYLIST)
public final class PacketRecvNekoPlayerDeleteMusicFromPersonalMusicPlaylist extends PSReceivePacket<GameServer, NetSession, NetByteArray> {
    @Override
    public void handle(GameServer gameServer, NetSession client, NetByteArray psHeader, NetByteArray psData) throws PSReceivePacketException {
        int musicId = psData.readInt();
        GamePlayer player = client.getPlayer();

        if (musicId == player.getPlayerRoom().lastMusicId)
            return;

        player.getPlayerData().getPersonalMusicPlaylist().removeIf(music -> music.getMusicId() == musicId);
        player.broadcastPacket(new PacketSendNekoPlayerPersonalMusicPlaylist(player.getPlayerData().getPersonalMusicPlaylist(), player.canSelectMusic, player.getPlayerRoom().lastMusicId));
    }
}