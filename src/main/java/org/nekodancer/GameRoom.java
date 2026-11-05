package org.nekodancer;

// Imports
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Packets
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
    }

    /**
     * Adds a player to the room.
     * @param player The player to add.
     */
    public void addPlayer(GamePlayer player) {
        this.players.put(player.getPlayerName(), player);
        player.setPlayerRoom(this);
        player.broadcastPacket(new PacketSendRoomServerType(0));
        player.broadcastPacket(new PacketSendRoomType(4));
        player.broadcastPacket(new PacketSendRoomEnter(true, this.roomName, this.roomCommunity));
        this.sendInitPlayerRound(player);
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
     * Gets the room's players.
     * @return The room's players.
     */
    public Map<String, GamePlayer> getPlayers() {
        return this.players;
    }

    /**
     * Initializes the player on the room when a new round appears.
     * @param player The player to initialize.
     */
    public void sendInitPlayerRound(GamePlayer player) {

    }
}