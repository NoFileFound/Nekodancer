package org.nekodancer;

// Imports
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.nekodancer.data.objects.RoomPlayerInfo;
import org.nekodancer.modules.ModuleLua;
import org.psframework.network.PSSendPacket;

// Packets
import org.nekodancer.packets.send.chat.PacketSendChatMessage;
import org.nekodancer.packets.send.neko.PacketSendNekoMap;
import org.nekodancer.packets.send.neko.PacketSendNekoNewPlayer;
import org.nekodancer.packets.send.neko.PacketSendNekoPlayerInitialize;
import org.nekodancer.packets.send.neko.PacketSendNekoPlayerList;
import org.nekodancer.packets.send.neko.PacketSendNekoPlayerRemove;
import org.nekodancer.packets.send.room.PacketSendRoomEnter;
import org.nekodancer.packets.send.room.PacketSendRoomServerType;
import org.nekodancer.packets.send.room.PacketSendRoomType;
import org.psframework.utils.PSTimer;

public final class GameRoom {
    private final GameServer server;
    private final String roomFullName;
    private final String roomCommunity;
    private final String roomName;
    private final String roomAuthor;
    private final Map<String, GamePlayer> players;
    private final ModuleLua parseLua;
    private final PSTimer startTimerLeft;
    private boolean isCurrentlyPlay;
    public int maximumPlayers;
    public int lastMusicId;
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

        // Instances
        this.parseLua = new ModuleLua(this.server, this);

        // Timers
        this.startTimerLeft = new PSTimer(this.server.getScheduler());
        this.sendChangeMap();
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
        this.parseLua.handleEventNewPlayer(player);
    }

    /**
     * Removes a player from the room.
     * @param player The player to remove.
     */
    public void removePlayer(GamePlayer player) {
        this.players.remove(player.getPlayerName(), player);
        this.sendResetPlayerRound(player);
        this.broadcastPacket(new PacketSendNekoPlayerRemove(player.getSessionId()));
        player.setPlayerRoom(null);
        this.parseLua.handleEventLeavePlayer(player);
        if (this.players.isEmpty()) {
            this.startTimerLeft.cancelTask(false);
            this.parseLua.sendRoomDestruction();
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
     * Checks if the round has started.
     * @return True if the round has started or else False.
     */
    public boolean getIsCurrentlyPlay() {
        return this.isCurrentlyPlay;
    }

    /**
     * Gets the lua module.
     * @return The lua module.
     */
    public ModuleLua getParseLua() {
        return this.parseLua;
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
     * Gets the room's creator.
     * @return The room's creator.
     */
    public String getRoomAuthor() {
        return this.roomAuthor;
    }

    /**
     * Gets the room's community.
     * @return The room's community.
     */
    public String getRoomCommunity() {
        return this.roomCommunity;
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
     * Sends the current map in the room.
     */
    public void sendChangeMap() {
        this.startTimerLeft.cancelTask(false);
        this.isCurrentlyPlay = false;
        this.lastMusicId = -1;
        this.players.values().forEach(this::sendResetPlayerRound);
        this.players.values().forEach(this::sendInitPlayerRound);
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
        if (player != null) {
            this.parseLua.handleChatMessage(player, message);
            if (message.startsWith("!")) {
                this.parseLua.handleChatCommand(player, message.substring(1));
                if (this.parseLua.getDisableChatCommandListDisplay().contains(message.substring(1).split(" ")[0])) {
                    return;
                }
            }
        }

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
        player.canSelectMusic = this.getHighScore().equals(player);
        player.broadcastPacket(new PacketSendNekoMap(4, false));
        player.broadcastPacket(new PacketSendNekoPlayerList(this.getPlayerRoomInfos()));
    }

    /**
     * Reset's the player's round.
     * @param player The given player.
     */
    public void sendResetPlayerRound(GamePlayer player) {
        /// TODO: player.isAfk = true;
        player.playerScore = 0;
        player.isDead = false;
    }

    /**
     * Initializes the room for starting the music.
     */
    public void sendInitRoomMusic() {
        for (GamePlayer player : this.players.values())
            player.broadcastPacket(new PacketSendNekoPlayerInitialize("", "", "", 0));

        this.startTimerLeft.schedule(() -> this.isCurrentlyPlay = true, 15, TimeUnit.SECONDS);
    }

    /**
     * Retrieves the player with the highest score in the room.
     * @return The Player object.
     */
    private GamePlayer getHighScore() {
        GamePlayer best = null;
        for (GamePlayer player : this.players.values())
            if ((best == null || (player.playerScore >= best.playerScore && !player.getIsGuest())))
                best = player;

        return best;
    }
}