package org.nekodancer.packets.recv.neko;

// Imports
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import org.nekodancer.GamePlayer;
import org.nekodancer.GameServer;
import org.nekodancer.database.data.PlayerEntity;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.network.NetSession;
import org.nekodancer.packets.PacketOpcodes;
import org.nekodancer.utils.Utils;
import org.psframework.network.PSPacket;
import org.psframework.network.PSReceivePacket;
import org.psframework.network.PSReceivePacketException;
import org.psframework.utils.PSJson;

// Packets
import org.nekodancer.packets.send.neko.PacketSendNekoPlayerPersonalMusicPlaylist;
import org.nekodancer.packets.send.chat.PacketSendServerMessage;

@SuppressWarnings("unused")
@PSPacket(id = PacketOpcodes.RECV_NEKO_PLAYER_ADD_MUSIC_TO_PERSONAL_MUSIC_PLAYLIST)
public final class PacketRecvNekoPlayerAddMusicToPersonalMusicPlaylist extends PSReceivePacket<GameServer, NetSession, NetByteArray> {
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    @Override
    public void handle(GameServer gameServer, NetSession client, NetByteArray psHeader, NetByteArray psData) throws PSReceivePacketException {
        String url = psData.readUTF();
        GamePlayer player = client.getPlayer();
        String ytId = Utils.getYoutubeID(url);
        if (ytId == null) {
            player.broadcastPacket(new PacketSendServerMessage(true, "Error 1000: The provided url is not a valid YouTube url."));
            return;
        }

        try {
            URI apiUri = URI.create("https://www.googleapis.com/youtube/v3/videos?id=" + ytId + "&key=" + gameServer.getConfig().getGameConfig().getYoutubeApiKey() + "&part=snippet,contentDetails");
            HttpRequest request = HttpRequest.newBuilder(apiUri).timeout(Duration.ofSeconds(5)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                gameServer.getLogger().warn("[MusicHandler] Please renew your youtube api key.");
                player.broadcastPacket(new PacketSendServerMessage(true, "Error 1001: The server's YouTube api key is not working."));
                return;
            }

            JsonNode jsonResponse = PSJson.read(response.body());
            JsonNode items = jsonResponse.path("items");
            if (!items.isArray() || items.isEmpty()) {
                player.broadcastPacket(new PacketSendServerMessage(true, "Error 1002: The YouTube video is not found."));
                return;
            }

            JsonNode videoInfo = items.get(0);
            JsonNode snippet = videoInfo.path("snippet");
            JsonNode contentDetails = videoInfo.path("contentDetails");
            JsonNode status = videoInfo.path("status");
            JsonNode statistics = videoInfo.path("statistics");
            if (status.path("privacyStatus").asText().equals("private")) {
                player.broadcastPacket(new PacketSendServerMessage(true, "Error 1003: The video is classified as private. You cannot play this video."));
                return;
            }

            if (!status.path("embeddable").asBoolean(true)) {
                player.broadcastPacket(new PacketSendServerMessage(true, "Error 1004: Video embedding is disabled."));
                return;
            }

            if (contentDetails.path("contentRating").path("ytRating").asText().equals("ytAgeRestricted")) {
                player.broadcastPacket(new PacketSendServerMessage(true, "Error 1005: Video is age restricted."));
                return;
            }

            String durationStr = contentDetails.path("duration").asText();
            String title = snippet.path("title").asText();
            if (durationStr.isEmpty() || title.isEmpty())
                return;

            List<PlayerEntity.MusicInfo> playlist = player.getPlayerData().getPersonalMusicPlaylist();
            if (playlist.stream().noneMatch(music -> music.getYoutubeVideoId().equals(ytId)))
                playlist.add(new PlayerEntity.MusicInfo(gameServer.getNextDatabaseCounterId("lastMusicId"), title, "https://img.youtube.com/vi/" + ytId + "/maxresdefault.jpg", ytId));

        } catch (IOException e) {
            player.broadcastPacket(new PacketSendServerMessage(true, e.getMessage()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            player.broadcastPacket(new PacketSendServerMessage(true, e.getMessage()));
        }

        player.broadcastPacket(new PacketSendNekoPlayerPersonalMusicPlaylist(player.getPlayerData().getPersonalMusicPlaylist(), player.canSelectMusic, player.getPlayerRoom().lastMusicId));
    }
}