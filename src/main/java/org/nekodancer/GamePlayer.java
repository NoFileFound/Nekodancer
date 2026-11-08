package org.nekodancer;

// Imports
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.nekodancer.data.objects.GameCommunity;
import org.nekodancer.data.objects.RoomPlayerInfo;
import org.nekodancer.data.objects.StaffRoleType;
import org.nekodancer.database.data.PlayerEntity;
import org.nekodancer.database.data.SanctionEntry;
import org.nekodancer.database.data.TribeEntry;
import org.nekodancer.modules.ModuleTribulle;
import org.nekodancer.network.NetSession;
import org.psframework.network.PSSendPacket;
import org.psframework.utils.PSTimer;
import org.nekodancer.utils.Utils;

// Packets
import org.nekodancer.packets.send.legacy.PacketSendPlayerBanScreenLogin;
import org.nekodancer.packets.send.login.PacketSendPlayerIdentity;
import org.nekodancer.packets.send.room.PacketSendRoomPasswordPopUp;

public final class GamePlayer {
    private static final AtomicInteger NEXT_PLAYER_ID = new AtomicInteger(0);
    private final GameServer server;
    private final NetSession session;
    private final PlayerEntity playerData;
    private final ModuleTribulle parseTribulle;
    private final PSTimer chatMessageTimer;
    private final List<String> invitedTribeHouses;
    private final int playerId;
    private final boolean isGuest;
    private final long loginTime;
    private GameRoom playerRoom;
    private String roomName;
    private String playerCommunity;
    private String lastRoomName;
    public List<String> modopwetSubscribedCommunities;
    public int silenceType;
    public boolean isDead;
    public boolean isMumuted;
    public boolean isModopwetSortedByTime;
    public boolean isModopwetByOnePersonReports;
    public boolean isModopwetForwardSorted;
    public boolean isModopwetSubscribed;
    public boolean isOpenCafe;
    public boolean isOpenFriendList;
    public boolean isOpenModopwet;
    public boolean isOpenTribe;
    public String currentTribeInvite;
    public String currentMarriageInvite;
    public String lastChatMessage;
    public String modopwetCurrentCommunity;
    public String silenceMessage;

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
        this.lastRoomName = "";
        this.playerCommunity = this.session.getCurrentCommunity().toUpperCase();
        this.loginTime = Utils.getUnixTime();
        this.invitedTribeHouses = new ArrayList<>();
        this.modopwetSubscribedCommunities = new ArrayList<>();
        this.modopwetCurrentCommunity = "ALL";
        this.lastChatMessage = "";
        this.currentTribeInvite = "";
        this.currentMarriageInvite = "";
        this.silenceMessage = "";

        // Player identification
        this.playerId = NEXT_PLAYER_ID.incrementAndGet();
        this.server.getPlayers().put(this.getPlayerName(), this);

        // Instances
        this.parseTribulle = new ModuleTribulle(this.server, this);

        // Timers
        this.chatMessageTimer = new PSTimer(this.server.getScheduler(), 3, TimeUnit.SECONDS);
    }

    /**
     * Sends a packet to the player.
     * @param packet The packet to send.
     */
    public void broadcastPacket(PSSendPacket packet) {
        this.session.sendData(packet);
    }

    /**
     * Sends a legacy packet to the player.
     * @param packet The legacy packet to send.
     */
    public void broadcastLegacyPacket(PSSendPacket packet) {
        this.session.sendOldData(packet);
    }

    /**
     * Disconnects the player from the game.
     * @param isClientSide is the function being called from the client.
     */
    public void disconnect(boolean isClientSide) {
        if (isClientSide) {
            this.session.disconnect();
        } else {
            this.chatMessageTimer.cancelTask(true);
            if (this.playerRoom != null)
                this.playerRoom.removePlayer(this);

            this.server.getParseModopwet().handlePlayerDisconnect(this);
            if (!this.isGuest) {
                this.parseTribulle.sendTribulleDestruction();
                this.playerData.playedTime += Utils.getUnixTime() - this.loginTime;
                this.playerData.lastConnectedTime = (System.currentTimeMillis() / 60000);
                this.server.getDatabase().savePlayer(this.playerData);
            }

            this.server.getPlayers().remove(this.getPlayerName(), this);
        }
    }

    /**
     * Gets the chat message timer.
     * @return The chat message timer.
     */
    public PSTimer getChatMessageTimer() {
        return this.chatMessageTimer;
    }

    /**
     * Gets the player's country code.
     * @return The player's country code.
     */
    public String getCountryCode() {
        return this.session.getCountryCode();
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
     * Gets the player's IP address.
     * @return The player's IP address.
     */
    public String getIpAddress() {
        return this.session.getIpAddress();
    }

    /**
     * Gets the tribulle's instance of the current player.
     * @return The current instance.
     */
    public ModuleTribulle getParseTribulle() {
        return this.parseTribulle;
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
     * Gets the player's last room.
     * @return The player's last room.
     */
    public String getLastRoomName() {
        return this.lastRoomName;
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
     * Gets the invited tribe houses.
     * @return The invited tribe houses.
     */
    public List<String> getInvitedTribeHouses() {
        return this.invitedTribeHouses;
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
        var mySanction = this.server.getDatabase().loadLastSanction(this.getPlayerName(), SanctionEntry.SanctionType.SANCTION_BAN);
        if (mySanction != null) {
            var state = mySanction.sanctionState;
            if (state == SanctionEntry.SanctionState.ACTIVE) {
                if (mySanction.isPermanent) {
                    this.broadcastLegacyPacket(new PacketSendPlayerBanScreenLogin(mySanction.getReason(), -1));
                    this.session.setHasBanned(true);
                    return;
                }

                long remainingTime = mySanction.getExpirationDate() - Utils.getUnixTime();
                if (remainingTime > 0) {
                    long hours = (remainingTime + 3599) / 3600;
                    this.broadcastLegacyPacket(new PacketSendPlayerBanScreenLogin(mySanction.getReason(), hours * 3600000));
                    this.session.setHasBanned(true);
                    return;
                }
            }
        }

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
        this.server.getParseModopwet().handlePlayerConnect(this);
        this.sendEnterRoom(roomName, "");
        this.parseTribulle.sendTribulleInitialization();
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
        if (this.roomName.startsWith("int-*\u0003")) {
            String tribeName = this.roomName.substring(2);
            if (!this.invitedTribeHouses.contains(tribeName) && !this.playerData.tribeName.equals(tribeName))
                this.roomName = this.server.getRecommendedRoom("", this.playerCommunity);
        }

        GameRoom roomInstance = this.server.getRooms().get(this.roomName);
        if (roomInstance != null) {
            if (!roomInstance.roomPassword.isEmpty() && !roomInstance.roomPassword.equals(password)) {
                this.broadcastPacket(new PacketSendRoomPasswordPopUp(this.roomName));
                return;
            }

            if (roomInstance.maximumPlayers > 0 && roomInstance.getPlayers().size() > roomInstance.maximumPlayers) {
                this.roomName = this.server.getRecommendedRoom(this.roomName, this.playerCommunity);
                roomInstance = this.server.getRooms().get(this.roomName);
            }
        }

        if (this.playerRoom != null) {
            this.lastRoomName = this.playerRoom.getRoomName();
            this.playerRoom.removePlayer(this);
        }

        if (!this.isGuest) {
            String playerName = this.getPlayerName();
            var players = this.server.getPlayers();
            for (String friendName : this.playerData.getFriendList()) {
                GamePlayer friend = players.get(friendName);
                if (friend != null && friend.isOpenFriendList && friend.playerData.getFriendList().contains(playerName)) {
                    friend.parseTribulle.sendFriendModification(playerName, 1);
                }
            }

            String tribeName = this.playerData.tribeName;
            if (!tribeName.isEmpty()) {
                TribeEntry tribe = this.server.getDatabase().loadTribeByName(tribeName);
                if (tribe != null) {
                    for (String memberName : tribe.getTribeMembers()) {
                        if (memberName.equals(playerName))
                            continue;

                        GamePlayer member = players.get(memberName);
                        if (member != null) {
                            member.parseTribulle.sendTribeMemberModification(playerName, -1, member.isOpenTribe);
                        }
                    }
                }
            }
        }

        if (roomInstance == null) {
            roomInstance = new GameRoom(this.server, this.getPlayerName(), this.roomName);
            this.server.getRooms().put(roomInstance.getRoomName(), roomInstance);
        }

        roomInstance.addPlayer(this);
        this.server.getParseModopwet().handlePlayerChangeRoom(this);
    }

    /**
     * Assigns the player to a room.
     * @param playerRoom The room to assign.
     */
    public void setPlayerRoom(GameRoom playerRoom) {
        this.playerRoom = playerRoom;
    }








    /**
     * Gets the player's information in the room.
     * @return The player's information in the room.
     */
    public RoomPlayerInfo getPlayerRoomInfo() {
        return new RoomPlayerInfo(
                this.getPlayerName(),
                this.getSessionId(),
                0,
                this.isDead,
                4
        );
    }
}