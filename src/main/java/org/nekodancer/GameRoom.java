package org.nekodancer;

// Imports
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.nekodancer.data.objects.RoomPlayerInfo;
import org.psframework.network.PSSendPacket;

// Packets
import org.nekodancer.packets.send.chat.PacketSendChatMessage;
import org.nekodancer.packets.send.neko.PacketSendNekoMap;
import org.nekodancer.packets.send.neko.PacketSendNekoNewPlayer;
import org.nekodancer.packets.send.neko.PacketSendNekoPlayerList;
import org.nekodancer.packets.send.room.PacketSendRoomEnter;
import org.nekodancer.packets.send.room.PacketSendRoomServerType;
import org.nekodancer.packets.send.room.PacketSendRoomType;

public final class GameRoom {
    private final GameServer server;
    private final String roomFullName;
    private final String roomCommunity;
    private final String roomName;
    private final String roomAuthor;
    private final Map<String, GamePlayer> players;
    public int maximumPlayers;
    private boolean isCurrentlyPlay;
    public String roomPassword;

    /**
     * Creates a new room name.
     * @param server The game server's instance.
     * @param roomAuthor The room author's name.
     * @param roomName The room's name.
     */
    public GameRoom(GameServer server, String roomAuthor, String roomName) {
        this.server = server;
        this.roomFullName = roomName;
        this.roomCommunity = this.roomFullName.substring(0, this.roomFullName.indexOf('-'));
        this.roomName = this.roomFullName.substring(this.roomFullName.indexOf('-') + 1);
        this.roomAuthor = roomAuthor;
        this.players = new ConcurrentHashMap<>();
        this.roomPassword = "";
    }

    /**
     * Adds a player to the room.
     * @param player The player to add.
     */
    public void addPlayer(GamePlayer player) {
        this.players.put(player.getPlayerName(), player);
        player.setPlayerRoom(this);
        player.isDead = this.isCurrentlyPlay;
        player.broadcastPacket(new PacketSendRoomServerType(0));
        player.broadcastPacket(new PacketSendRoomType(4));
        player.broadcastPacket(new PacketSendRoomEnter(true, this.roomName, this.roomCommunity));
        this.sendInitPlayerRound(player);
        this.broadcastPacket(player, new PacketSendNekoNewPlayer(player.getPlayerRoomInfo()));
    }

    /**
     * Removes a player from the room.
     * @param player The player to remove.
     */
    public void removePlayer(GamePlayer player) {
        this.players.remove(player.getPlayerName(), player);
        player.setPlayerRoom(null);
        if (this.players.isEmpty()) {
            this.server.getRooms().remove(this.roomFullName, this);
        }
    }

    /**
     * Broadcasts a packet for everyone in the room.
     * @param packet The packet to send.
     */
    public void broadcastPacket(PSSendPacket packet) {
        for (GamePlayer player : this.players.values()) {
            player.broadcastPacket(packet);
        }
    }

    /**
     * Broadcasts a packet for everyone in the room except the given player.
     * @param senderPlayer The given player.
     * @param packet The packet to send.
     */
    public void broadcastPacket(GamePlayer senderPlayer, PSSendPacket packet) {
        for (GamePlayer player : this.players.values()) {
            if (!player.equals(senderPlayer)) {
                player.broadcastPacket(packet);
            }
        }
    }

    /**
     * Gets the room's players.
     * @return The room's players.
     */
    public Map<String, GamePlayer> getPlayers() {
        return this.players;
    }

    /**
     * Gets the room information for every player in the current room.
     * @return The players' room information.
     */
    private List<RoomPlayerInfo> getPlayerRoomInfos() {
        List<RoomPlayerInfo> data = new ArrayList<>(this.players.size());
        for (GamePlayer player : this.players.values())
            data.add(player.getPlayerRoomInfo());

        return data;
    }

    /**
     * Gets the room's name.
     * @return The room's name.
     */
    public String getRoomName() {
        return this.roomFullName;
    }

    /**
     * Gets the room's name without the community prefix.
     * @return The room's name wtihout the community prefix.
     */
    public String getRoomNameSuffix() {
        return this.roomName;
    }

    /**
     * Sends a chat message.
     * @param playerName The message's author.
     * @param message The message's context.
     * @param isMumuted Is the player going to talk to himself.
     */
    public void sendChatMessage(String playerName, String message, boolean isMumuted) {
        PSSendPacket packet = new PacketSendChatMessage(playerName, message);
        GamePlayer player = this.players.get(playerName);
        if (isMumuted) {
            if(player != null) {
                player.broadcastPacket(packet);
            }
        } else {
            this.broadcastPacket(packet);
        }
    }

    /**
     * Initializes the player on the room when a new round appears.
     * @param player The player to initialize.
     */
    public void sendInitPlayerRound(GamePlayer player) {
        player.broadcastPacket(new PacketSendNekoMap(4, false));
        player.broadcastPacket(new PacketSendNekoPlayerList(this.getPlayerRoomInfos()));
    }
}