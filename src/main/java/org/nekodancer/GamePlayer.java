package org.nekodancer;

// Imports
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.nekodancer.data.objects.GameCommunity;
import org.nekodancer.data.objects.StaffRoleType;
import org.nekodancer.database.data.PlayerEntity;
import org.nekodancer.network.NetSession;
import org.nekodancer.utils.Utils;
import org.psframework.network.PSSendPacket;

// Packets
import org.nekodancer.packets.send.login.PacketSendPlayerIdentity;

public final class GamePlayer {
    private static final AtomicInteger NEXT_PLAYER_ID = new AtomicInteger(0);
    private final GameServer server;
    private final NetSession session;
    private final PlayerEntity playerData;
    private final int playerId;
    private final boolean isGuest;
    private GameRoom playerRoom;
    private String roomName;
    private String playerCommunity;
    public boolean isOpenCafe;

    /**
     * Creates a new player.
     * @param session The player's session.
     * @param isGuest Is the player a guest or a registered account.
     * @param playerData The player's data.
     */
    public GamePlayer(NetSession session, boolean isGuest, PlayerEntity playerData) {
        this.server = (GameServer) session.getServer();
        this.session = session;
        this.session.setLoginAttempts(0);
        this.playerData = playerData;
        this.isGuest = isGuest;
        this.roomName = "";
        this.playerCommunity = this.session.getCurrentCommunity().toUpperCase();

        // Player identification
        this.playerId = NEXT_PLAYER_ID.incrementAndGet();
        this.server.getPlayers().put(this.getPlayerName(), this);
    }

    /**
     * Sends a packet to the player.
     * @param packet The packet to send.
     */
    public void broadcastPacket(PSSendPacket packet) {
        this.session.sendData(packet);
    }

    /**
     * Disconnects the player from the game.
     * @param isClientSide is the function being called from the client.
     */
    public void disconnect(boolean isClientSide) {
        if (isClientSide) {
            this.session.disconnect();
        } else {
            if (!this.isGuest) {
                this.server.getDatabase().savePlayer(this.playerData);
            }

            this.server.getPlayers().remove(this.getPlayerName(), this);
        }
    }

    /**
     * Gets the player's global id.
     * @return The player's global id.
     */
    public long getGlobalId() {
        return this.playerData.getAccountId();
    }

    /**
     * Checks if the player is guest.
     * @return True if the player is guest or else False.
     */
    public boolean getIsGuest() {
        return this.isGuest;
    }

    /**
     * Gets the player's community.
     * @return The player's community.
     */
    public String getPlayerCommunity() {
        return this.playerCommunity;
    }

    /**
     * Gets the player's data.
     * @return The player's data.
     */
    public PlayerEntity getPlayerData() {
        return this.playerData;
    }

    /**
     * Gets the player's name.
     * @return The player's name.
     */
    public String getPlayerName() {
        return this.playerData.getUsername();
    }

    /**
     * Gets the player's room.
     * @return The player's room.
     */
    public GameRoom getPlayerRoom() {
        return this.playerRoom;
    }

    /**
     * Gets the player's room name.
     * @return The player's room name.
     */
    public String getRoomName() {
        return this.roomName;
    }

    /**
     * Gets the player's session id.
     * @return The player's session id.
     */
    public int getSessionId() {
        return this.playerId;
    }

    /**
     * Checks if current client has permission.
     * @param staffRole The staff position.
     * @param isStaffChannelPerm Is the permission about sending messages in staff channels.
     * @return If the client has permission.
     */
    public boolean hasStaffPermission(StaffRoleType staffRole, boolean isStaffChannelPerm) {
        if (this.isGuest)
            return false;

        if (this.server.getConfig().getIsDebug())
            return true;

        if (this.playerData.getStaffRoles().contains(StaffRoleType.ADMIN))
            return true;

        if (this.playerData.hasPublicAuthorization && isStaffChannelPerm)
            return true;

        return this.playerData.getStaffRoles().contains(staffRole);
    }

    /**
     * Handles when the player logins in the game.
     * @param roomName The room's name where the player will join.
     */
    public void sendLogin(String roomName) {
        ArrayList<Integer> privileges = new ArrayList<>();
        if (this.hasStaffPermission(StaffRoleType.ADMIN, false))
            privileges.add(10);

        if (this.hasStaffPermission(StaffRoleType.ARBITRE, false))
            privileges.add(3);

        if (this.hasStaffPermission(StaffRoleType.PUBLIC_MODO, false) || this.hasStaffPermission(StaffRoleType.PRIVATE_MODO, false)) {
            privileges.add(3);
            privileges.add(5);
        }

        if (this.hasStaffPermission(StaffRoleType.MAPCREW, false))
            privileges.add(11);

        if (this.hasStaffPermission(StaffRoleType.FASHIONSQUAD, false))
            privileges.add(15);

        if (this.hasStaffPermission(StaffRoleType.LUADEV, false))
            privileges.add(12);

        if (this.hasStaffPermission(StaffRoleType.FUNCORP, false))
            privileges.add(13);

        this.broadcastPacket(new PacketSendPlayerIdentity(this.getGlobalId(), this.getPlayerName(), this.playerData.playedTime, GameCommunity.fromCode(this.playerCommunity), this.getSessionId(), !this.isGuest, privileges, this.playerData.hasPublicAuthorization));
        this.sendEnterRoom(roomName, "");
    }

    /**
     * Enters a room.
     * @param roomName The room name to enter.
     * @param password The room password if it has.
     */
    public void sendEnterRoom(String roomName, String password) {
        if (roomName.isEmpty())
            roomName = this.server.getRecommendedRoom("", this.playerCommunity);

        roomName = Utils.formatRoomName(roomName, this.playerCommunity, !this.playerData.getStaffRoles().isEmpty());
        if (this.roomName.equals(roomName))
            return;

        this.roomName = roomName;
        GameRoom roomInstance = this.server.getRooms().get(this.roomName);
        if (roomInstance == null) {
            roomInstance = new GameRoom(this.server, this.getPlayerName(), this.roomName);
        }

        roomInstance.addPlayer(this);
    }

    /**
     * Assigns the player to a room.
     * @param playerRoom The room to assign.
     */
    public void setPlayerRoom(GameRoom playerRoom) {
        this.playerRoom = playerRoom;
    }
}