package org.nekodancer;

// Imports
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.fusesource.jansi.AnsiConsole;
import org.nekodancer.command.CommandLoader;
import org.nekodancer.config.NekodancerConfig;
import org.nekodancer.data.objects.StaffRoleType;
import org.nekodancer.database.GameDatabaseManager;
import org.nekodancer.database.GameDatabaseManagerFactory;
import org.nekodancer.database.data.IpTempBanEntry;
import org.nekodancer.database.data.PlayerEntity;
import org.nekodancer.database.data.SanctionEntry;
import org.nekodancer.modules.ModuleCafe;
import org.nekodancer.modules.ModuleModopwet;
import org.nekodancer.network.NetDecoder;
import org.nekodancer.network.NetHandler;
import org.nekodancer.network.NetSession;
import org.nekodancer.utils.PlayerQueue;
import org.nekodancer.utils.Utils;
import org.psframework.admin.PSAdminConfig;
import org.psframework.database.PSDatabaseConfig;
import org.psframework.game.PSGameServer;
import org.psframework.logging.PSLogger;
import org.psframework.network.PSPacketHandler;
import org.psframework.network.PSSendPacket;
import org.psframework.utils.PSGeoIP;
import org.psframework.utils.PSRandom;
import org.psframework.utils.PSTimer;

// Packets
import org.nekodancer.packets.send.chat.PacketSendServerMessage;
import org.nekodancer.packets.send.chat.PacketSendStaffChatMessage;
import org.nekodancer.packets.send.informations.PacketSendTranslatedMessage;

public final class GameServer extends PSGameServer {
    private static final char[] FILTER_CHARS = "!@#$%&*?".toCharArray();
    private final NekodancerConfig nekodancerConfig;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final List<Channel> channels;
    private final PSPacketHandler generalPacketHandler;
    private final CommandLoader commandManager;
    private final PlayerQueue playerQueue;
    private final ModuleCafe parseCafe;
    private final ModuleModopwet parseModopwet;
    private final Pattern chatFilterPattern;
    private final Map<ChannelId, NetSession> clientSessions;
    private final Map<String, PSTimer> accountCreationTimer;
    private final Map<String, PSTimer> marriageTimers;
    private final Map<String, List<String>> chats;
    private final Map<String, Map<String, Deque<String[]>>> chatMessages;
    private final Map<String, Map<String, Deque<String[]>>> whisperMessages;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final Map<String, GamePlayer> players;
    private final Map<String, GameRoom> rooms;
    private GameDatabaseManager databaseManager;
    private PSGeoIP geoIP;

    /**
     * Creates a new game server.
     * @param gamePorts The game server's ports.
     * @param databaseConfig The database's configuration.
     * @param adminConfig The admin panel's configuration.
     * @param logger The game server's logger.
     */
    public GameServer(ArrayList<Integer> gamePorts, PSDatabaseConfig databaseConfig, PSAdminConfig adminConfig, PSLogger logger, NekodancerConfig nekodancerConfig) {
        super(gamePorts, databaseConfig, adminConfig, logger);
        this.nekodancerConfig = nekodancerConfig;
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
        this.channels = new ArrayList<>();
        this.generalPacketHandler = new PSPacketHandler("org.nekodancer.packets.recv");
        this.clientSessions = new ConcurrentHashMap<>();
        this.players = new ConcurrentHashMap<>();
        this.rooms = new ConcurrentHashMap<>();
        this.chats = new HashMap<>();
        this.chatMessages = new HashMap<>();
        this.whisperMessages = new HashMap<>();
        this.playerQueue = new PlayerQueue(this.nekodancerConfig.getGameConfig().getMaxPlayers());
        this.chatFilterPattern = Pattern.compile("(?iu)(" + this.nekodancerConfig.getGameConfig().getChatFilter().stream().map(word -> word.codePoints().mapToObj(cp -> Pattern.quote(new String(Character.toChars(cp)))).collect(Collectors.joining("[\\p{P}\\p{S}]*"))).collect(Collectors.joining("|")) + ")");

        // Instances
        this.commandManager = new CommandLoader(this);
        this.parseCafe = new ModuleCafe(this);
        this.parseModopwet = new ModuleModopwet(this);

        // Timers
        this.accountCreationTimer = new HashMap<>();
        this.marriageTimers = new ConcurrentHashMap<>();
    }

    /**
     * Loads the database in the server.
     */
    @Override
    public void loadDatabase() {
        try {
            this.geoIP = new PSGeoIP(Path.of("./config/GeoIP.dat"));
            this.logger.debug("[GeoIP] GeoIP Loaded successfully.");
            this.database.connect();
            this.logger.info(String.format("[Database] Successfully connected at the database: %s", this.database.getConnectionUrl()));
            this.databaseManager = GameDatabaseManagerFactory.create(this.database, this.nekodancerConfig.getIsDebug());
            this.databaseManager.initialize();
            this.logger.info("[Database] Main Database initialized.");
            this.parseModopwet.loadDatabase();
            this.generalPacketHandler.registerPackets();
            this.logger.debug("[PacketHandler] Registered successfully.");
            if (this.nekodancerConfig.getIsDebug())
                this.logger.warn("[Debug] The server is in debug-mode. [All players from now on will have admin rights.]");
        } catch (Exception ex) {
            this.logger.error("Unable to initialize the game server. It is not safe to continue.", ex);
            System.exit(1);
        }
    }

    /**
     * Saves the database.
     */
    @Override
    public void saveDatabase() {
        // Saves the player data.
        for (GamePlayer player : this.players.values()) {
            try {
                if (player.getIsGuest())
                    continue;

                this.databaseManager.savePlayer(player.getPlayerData());
            } catch (Exception ex) {
                this.logger.error(String.format("[Database] Failed to save player %s.", player.getPlayerName()), ex);
            }
        }

        // saves the modopwet reports.
        this.parseModopwet.saveDatabase();
        this.database.disconnect();
    }

    /**
     * Starts the game server.
     */
    @Override
    public void start() {
        AnsiConsole.systemInstall();
        this.loadDatabase();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(this.bossGroup, this.workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast("decoder", new NetDecoder());
                            pipeline.addLast("handler", new NetHandler(GameServer.this));
                        }
                    });

            for (Integer port : this.ports) {
                Channel channel = bootstrap.bind(new InetSocketAddress(port)).sync().channel();
                this.channels.add(channel);
            }

            this.logger.info(String.format("[GameServer] The server is listening on ports: %s", this.ports));
            this.logger.info("The server is started.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Stops the game server.
     */
    @Override
    public void stop() {
        this.logger.info("[GameServer] Shutting down the game server.");
        for (Channel channel : this.channels)
            channel.close();

        for (Channel channel : this.channels)
            channel.closeFuture().syncUninterruptibly();

        this.saveDatabase();
        this.bossGroup.shutdownGracefully().syncUninterruptibly();
        this.workerGroup.shutdownGracefully().syncUninterruptibly();
        this.logger.info("[GameServer] Shutdown completed.");
        AnsiConsole.systemUninstall();
    }

    /**
     * Sends a packet to everyone in the server.
     * @param packet The packet to send.
     */
    public void broadcastPacket(PSSendPacket packet) {
        for (GamePlayer player : this.players.values())
            player.broadcastPacket(packet);
    }

    /**
     * Removes the bad words from the message.
     * @param message The message.
     * @return The formatted message.
     */
    public String formatText(String message) {
        Matcher matcher = this.chatFilterPattern.matcher(message);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            StringBuilder replacement = new StringBuilder();
            for (int i = 0; i < matcher.group().length(); i++)
                replacement.append(FILTER_CHARS[PSRandom.nextInt(FILTER_CHARS.length)]);

            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement.toString()));
        }

        return matcher.appendTail(result).toString();
    }

    /**
     * Gets the account creation timer.
     * @return The account creation timer.
     */
    public PSTimer getAccountCreationTimer(String ipAddress) {
        return this.accountCreationTimer.computeIfAbsent(ipAddress, _ -> new PSTimer(this.scheduler, 24, TimeUnit.HOURS));
    }

    /**
     * Gets the global chats.
     * @return The global chats.
     */
    public Map<String, List<String>> getChats() {
        return this.chats;
    }

    /**
     * Gets the chat messages.
     * @return The chat messages.
     */
    public Map<String, Map<String, Deque<String[]>>> getChatMessages() {
        return this.chatMessages;
    }

    /**
     * Gets the list of connected sessions to the server.
     * @return The list of connected sessions.
     */
    public Map<ChannelId, NetSession> getClientSessions() {
        return this.clientSessions;
    }

    /**
     * Gets the command handler.
     * @return The command handler.
     */
    public CommandLoader getCommandManager() {
        return this.commandManager;
    }

    /**
     * Gets the server config.
     * @return The server config.
     */
    public NekodancerConfig getConfig() {
        return this.nekodancerConfig;
    }

    /**
     * Gets the Database instance.
     * @return The Database instance.
     */
    public GameDatabaseManager getDatabase() {
        return this.databaseManager;
    }

    /**
     * Gets the GeoIP instance.
     * @return The GeoIP instance.
     */
    public PSGeoIP getGeoIP() {
        return this.geoIP;
    }

    /**
     * Gets the marriage timer based on the player.
     * @param username The player.
     * @return The player's marriage timer.
     */
    public PSTimer getMarriageTimer(String username) {
        return this.marriageTimers.computeIfAbsent(username, _ -> new PSTimer(this.scheduler, 1, TimeUnit.HOURS));
    }

    /**
     * Gets the modopwet's instance.
     * @return The modopwet's instance.
     */
    public ModuleModopwet getParseModopwet() {
        return this.parseModopwet;
    }

    /**
     * Gets the next counter by variable name from the database.
     * @param variable The variable.
     * @return The next counter starts from 1.
     */
    public int getNextDatabaseCounterId(String variable) {
        return this.database.getNextCounterId(variable);
    }

    /**
     * Gets the packet handler.
     * @return The packet handler.
     */
    public PSPacketHandler getPacketHandler() {
        return this.generalPacketHandler;
    }

    /**
     * Gets the cafe's module.
     * @return The cafe's module.
     */
    public ModuleCafe getParseCafe() {
        return this.parseCafe;
    }

    /**
     * Gets the player queue.
     * @return The player queue.
     */
    public PlayerQueue getPlayerQueue() {
        return this.playerQueue;
    }

    /**
     * Gets the list of players.
     * @return The list of players.
     */
    public Map<String, GamePlayer> getPlayers() {
        return this.players;
    }

    /**
     * Gets the list of rooms.
     * @return The list of rooms.
     */
    public Map<String, GameRoom> getRooms() {
        return this.rooms;
    }

    /**
     * Gets the all rooms that are in the given community or international rooms and are in the same game mode.
     * @param community The community.
     * @return A list of rooms.
     */
    public List<GameRoom> getRoomList(String community) {
        community = Utils.getCommunityFromLanguage(community);
        List<GameRoom> roomList = new ArrayList<>();
        for (GameRoom room : this.rooms.values()) {
            if (room.getRoomCommunity().equals("INT") && !room.getRoomNameSuffix().startsWith("@") && room.roomPassword.isEmpty()) {
                roomList.add(room);
            }
        }

        for (GameRoom room : this.rooms.values()) {
            if (room.getRoomCommunity().equals(community) && !room.getRoomNameSuffix().startsWith("@") && room.roomPassword.isEmpty()) {
                roomList.add(room);
            }
        }

        return roomList;
    }

    /**
     * Checks if given room has enough players and recommends a new one if there are many players.
     *
     * @param roomName  The player's room name.
     * @return The room that has enough players.
     */
    public String getRecommendedRoom(String roomName, String community) {
        if (roomName.isEmpty())
            roomName = community + "-1";

        String roomKey = community + "-" + roomName;
        GameRoom room = this.rooms.get(roomKey);
        if (room == null || room.maximumPlayers == 0 || room.getPlayers().size() < room.maximumPlayers)
            return roomName;

        int x = 0;
        while (true) {
            x++;
            room = this.rooms.get(roomKey + x);
            if (room == null || room.maximumPlayers == 0 || room.getPlayers().size() < room.maximumPlayers)
                return roomName + x;
        }
    }

    /**
     * Gets the server's scheduler.
     * @return The server's scheduler.
     */
    public ScheduledExecutorService getScheduler() {
        return this.scheduler;
    }

    /**
     * Gets the whisper messages.
     * @return The whisper messages.
     */
    public Map<String, Map<String, Deque<String[]>>> getWhisperMessages() {
        return this.whisperMessages;
    }

    /**
     * Bans an ip address.
     * @param ipAddress The given ip address.
     * @param banHours The ban hours.
     * @param reason The reason.
     */
    public void sendIPBan(String ipAddress, int banHours, String reason) {
        if (this.databaseManager.isIpBlacklisted(ipAddress))
            return;

        this.databaseManager.saveIpTempBan(new IpTempBanEntry(ipAddress, banHours, reason));
    }

    /**
     * Bans the player.
     * @param playerName The player's name.
     * @param banHours The ban hours.
     * @param banReason The ban's reason.
     * @param moderatorPlayer The moderator's instance.
     * @param isSilent Is the ban message silent.
     * @return True if the ban was successful or else False.
     */
    public boolean sendPlayerBan(String playerName, int banHours, String banReason, GamePlayer moderatorPlayer, boolean isSilent) {
        if (playerName.equals(moderatorPlayer.getPlayerName()))
            return false;

        if (banHours <= 0) {
            moderatorPlayer.broadcastPacket(new PacketSendServerMessage(true, "Invalid ban duration."));
            return false;
        }

        GamePlayer player = this.players.get(playerName);
        if (player != null && player.getIsGuest()) {
            banHours = Math.max(banHours, 24);
            this.sendIPBan(player.getIpAddress(), banHours, banReason);
            moderatorPlayer.broadcastPacket(new PacketSendServerMessage(true, String.format("The player %s got banned.", playerName)));
            this.sendServerMessageOthers(moderatorPlayer, String.format("%s banned the player %s for %sh (%s).", moderatorPlayer.getPlayerName(), playerName, banHours, banReason), StaffRoleType.PUBLIC_MODO);
            return true;
        }

        PlayerEntity playerEntity = this.getDatabase().loadPlayerByName(playerName);
        if (playerEntity == null) {
            moderatorPlayer.broadcastPacket(new PacketSendServerMessage(true, "The supplied argument isn't a valid nickname."));
            return false;
        }

        SanctionEntry lastSanction = this.getDatabase().loadLastSanction(playerName, SanctionEntry.SanctionType.SANCTION_BAN);
        if (lastSanction != null) {
            moderatorPlayer.broadcastPacket(new PacketSendServerMessage(true, String.format("Player [%s] is already banned, please wait.", playerName)));
            return false;
        }

        long endDate = Utils.getUnixTime() + (banHours * 3600L);
        SanctionEntry sanction = new SanctionEntry(this.getNextDatabaseCounterId("lastSanctionId"), playerName, player != null ? player.getIpAddress() : "", SanctionEntry.SanctionType.SANCTION_BAN, moderatorPlayer.getPlayerName(), banReason, endDate);
        this.getDatabase().saveSanction(sanction);
        if (player != null) {
            if (!isSilent)
                player.getPlayerRoom().broadcastPacket(player, new PacketSendStaffChatMessage(0, "", "$Message_Ban", new ArrayList<>(List.of(playerName, String.valueOf(banHours), banReason)), true));

            this.sendIPBan(player.getIpAddress(), banHours, banReason);
            player.disconnect(true);
        }

        moderatorPlayer.broadcastPacket(new PacketSendServerMessage(true, String.format("The player %s got banned.", playerName)));
        this.sendServerMessageOthers(moderatorPlayer, String.format("%s %s banned the player %s for %sh (%s).", moderatorPlayer.getPlayerName(), (player != null ? "" : "offline"), playerName, banHours, banReason), StaffRoleType.PUBLIC_MODO);
        return true;
    }

    /**
     * Unbans an ip address.
     * @param ipAddress The given ip address.
     * @param player The moderator's instance.
     */
    public void sendIPUnban(String ipAddress, GamePlayer player) {
        var info = this.databaseManager.loadTempIpBanByIp(ipAddress);
        if (info == null) {
            player.broadcastPacket(new PacketSendServerMessage(true, String.format("The provided IP address (%s) is not banned", ipAddress)));
            return;
        }

        this.databaseManager.deleteIpTempBan(info);
    }

    /**
     * Kicks the player.
     * @param playerName The player's name.
     * @param kickReason The kick's reason.
     * @param moderatorPlayer The kick's moderator.
     */
    public void sendPlayerKick(String playerName, String kickReason, GamePlayer moderatorPlayer) {
        if (playerName.equals(moderatorPlayer.getPlayerName()))
            return;

        GamePlayer player = this.players.get(playerName);
        if (player == null) {
            moderatorPlayer.broadcastPacket(new PacketSendServerMessage(true, "The player is not connected."));
            return;
        }

        player.disconnect(true);
        moderatorPlayer.broadcastPacket(new PacketSendServerMessage(true, String.format("The player %s got kicked.", playerName)));
        this.sendServerMessageOthers(moderatorPlayer, String.format("%s kicked the player %s (%s).", moderatorPlayer.getPlayerName(), playerName, kickReason), StaffRoleType.PUBLIC_MODO);
    }

    /**
     * Mutes the player.
     * @param playerName The player's name.
     * @param muteHours The mute's duration.
     * @param muteReason The mute's reason.
     * @param moderatorPlayer The mute's moderator.
     * @param isSilent is the mute message silent.
     */
    public void sendPlayerMute(String playerName, int muteHours, String muteReason, GamePlayer moderatorPlayer, boolean isSilent) {
        if (playerName.equals(moderatorPlayer.getPlayerName()))
            return;

        if (muteHours <= 0) {
            moderatorPlayer.broadcastPacket(new PacketSendServerMessage(true, "Invalid mute duration."));
            return;
        }

        GamePlayer player = this.players.get(playerName);
        PlayerEntity playerEntity = this.getDatabase().loadPlayerByName(playerName);
        if (playerEntity == null) {
            moderatorPlayer.broadcastPacket(new PacketSendServerMessage(true, "The supplied argument isn't a valid nickname."));
            return;
        }

        SanctionEntry lastSanction = this.getDatabase().loadLastSanction(playerName, SanctionEntry.SanctionType.SANCTION_MUTE);
        if (lastSanction != null) {
            moderatorPlayer.broadcastPacket(new PacketSendServerMessage(true, String.format("Player [%s] is already muted, please wait.", playerName)));
            return;
        }

        long endDate = Utils.getUnixTime() + (muteHours * 3600L);
        SanctionEntry sanction = new SanctionEntry(this.getNextDatabaseCounterId("lastSanctionId"), playerName, player != null ? player.getIpAddress() : "", SanctionEntry.SanctionType.SANCTION_MUTE, moderatorPlayer.getPlayerName(), muteReason, endDate);
        this.getDatabase().saveSanction(sanction);
        if (player != null) {
            if (!isSilent)
                player.getPlayerRoom().broadcastPacket(player, new PacketSendTranslatedMessage("", "<ROSE>$MuteInfo2</ROSE>", new String[]{playerName, String.valueOf(muteHours), muteReason}));

            player.broadcastPacket(new PacketSendTranslatedMessage("", "<ROSE>$MuteInfo1</ROSE>", new String[]{String.valueOf(muteHours), muteReason}));
        }

        moderatorPlayer.broadcastPacket(new PacketSendServerMessage(true, String.format("The player %s got muted.", playerName)));
        this.sendServerMessageOthers(moderatorPlayer, String.format("%s %s muted the player %s for %sh (%s)", moderatorPlayer.getPlayerName(), (player != null ? "" : "offline"), playerName, muteHours, muteReason), StaffRoleType.PUBLIC_MODO);
    }

    /**
     * Unbans the player.
     * @param playerName The player's name.
     * @param unbanReason The unban reason.
     * @param moderatorPlayer The unban's moderator.
     */
    public void sendPlayerUnban(String playerName, String unbanReason, GamePlayer moderatorPlayer) {
        PlayerEntity playerEntity = this.getDatabase().loadPlayerByName(playerName);
        if (playerEntity == null) {
            moderatorPlayer.broadcastPacket(new PacketSendServerMessage(true, "The supplied argument isn't a valid nickname."));
            return;
        }

        SanctionEntry lastSanction = this.getDatabase().loadLastSanction(playerName, SanctionEntry.SanctionType.SANCTION_BAN);
        if (lastSanction == null) {
            moderatorPlayer.broadcastPacket(new PacketSendServerMessage(true, String.format("The player %s is not banned yet.", playerName)));
            return;
        }

        lastSanction.sanctionState = SanctionEntry.SanctionState.CANCELED;
        lastSanction.cancelDate = Utils.getUnixTime();
        lastSanction.cancelAuthor = moderatorPlayer.getPlayerName();
        lastSanction.cancelReason = unbanReason;
        this.getDatabase().saveSanction(lastSanction);
        moderatorPlayer.broadcastPacket(new PacketSendServerMessage(true, String.format("The player %s got unbanned.", playerName)));
        this.sendServerMessageOthers(moderatorPlayer, String.format("%s unbanned the player %s (%s).", moderatorPlayer.getPlayerName(), playerName, unbanReason), StaffRoleType.PUBLIC_MODO);
    }

    /**
     * Unmutes the player.
     * @param playerName The player's name.
     * @param unmuteReason The unmute's reason.
     * @param moderatorPlayer The unmute's moderator.
     */
    public void sendPlayerUnmute(String playerName, String unmuteReason, GamePlayer moderatorPlayer) {
        PlayerEntity playerEntity = this.getDatabase().loadPlayerByName(playerName);
        if (playerEntity == null) {
            moderatorPlayer.broadcastPacket(new PacketSendServerMessage(true, "The supplied argument isn't a valid nickname."));
            return;
        }

        SanctionEntry lastSanction = this.getDatabase().loadLastSanction(playerName, SanctionEntry.SanctionType.SANCTION_MUTE);
        if (lastSanction == null) {
            moderatorPlayer.broadcastPacket(new PacketSendServerMessage(true, String.format("The player %s is not muted yet.", playerName)));
            return;
        }

        lastSanction.sanctionState = SanctionEntry.SanctionState.CANCELED;
        lastSanction.cancelDate = Utils.getUnixTime();
        lastSanction.cancelAuthor = moderatorPlayer.getPlayerName();
        lastSanction.cancelReason = unmuteReason;
        this.getDatabase().saveSanction(lastSanction);
        moderatorPlayer.broadcastPacket(new PacketSendServerMessage(true, String.format("The player %s got unmuted.", playerName)));
        this.sendServerMessageOthers(moderatorPlayer, String.format("%s unmuted the player %s (%s).", moderatorPlayer.getPlayerName(), playerName, unmuteReason), StaffRoleType.PUBLIC_MODO);
    }

    /**
     * Sends a message in the staff channel.
     * @param channelId The staff channel id.
     * @param channelMessage The message to send.
     * @param isInternational Send the message in every staff player (Exceptions are #Modo and #Arbitre).
     * @param playerName Sender's nickname.
     * @param playerCommunity Sender's community.
     */
    public void sendStaffChannelMessage(byte channelId, String channelMessage, boolean isInternational, String playerName, String playerCommunity) {
        GamePlayer sender = this.players.get(playerName);
        if (channelId == 0 || channelId == 6) {
            for (GamePlayer player : sender.getPlayerRoom().getPlayers().values()) {
                if (channelId == 0 || player.getPlayerCommunity().equals(sender.getPlayerCommunity())) {
                    player.broadcastPacket(new PacketSendStaffChatMessage(0, "", channelMessage));
                }
            }

            return;
        }

        if (channelId == 1 || channelId == 11) {
            this.broadcastPacket(new PacketSendStaffChatMessage(channelId, playerName, channelMessage));
            return;
        }

        for (GamePlayer player : this.players.values()) {
            boolean hasPermission = switch (channelId) {
                case 3, 4 -> player.hasStaffPermission(StaffRoleType.PUBLIC_MODO, true) || player.hasStaffPermission(StaffRoleType.PRIVATE_MODO, true);
                case 2, 5 -> player.hasStaffPermission(StaffRoleType.ARBITRE, true) || player.hasStaffPermission(StaffRoleType.PUBLIC_MODO, true) || player.hasStaffPermission(StaffRoleType.PRIVATE_MODO, true);
                case 8 -> player.hasStaffPermission(StaffRoleType.LUADEV, true);
                case 7 -> player.hasStaffPermission(StaffRoleType.MAPCREW, true);
                case 9 -> player.hasStaffPermission(StaffRoleType.FUNCORP, true);
                case 10 -> player.hasStaffPermission(StaffRoleType.FASHIONSQUAD, true);
                default -> false;
            };

            if (!hasPermission || channelId <= 5 && !isInternational && !player.getPlayerCommunity().equals(playerCommunity))
                continue;

            player.broadcastPacket(new PacketSendStaffChatMessage(channelId, playerName, channelMessage));
        }
    }

    /**
     * Sends a #Server message to every else in the server.
     * @param player The excluded player.
     * @param message The message to send.
     * @param staffRole The minimum required staff role.
     */
    public void sendServerMessageOthers(GamePlayer player, String message, StaffRoleType staffRole) {
        for (GamePlayer target : this.players.values()) {
            if (target == player)
                continue;

            List<StaffRoleType> targetRoles = target.getPlayerData().getStaffRoles();
            boolean isAdmin = targetRoles.contains(StaffRoleType.ADMIN);
            boolean isModerator = isAdmin || targetRoles.contains(StaffRoleType.PUBLIC_MODO) || targetRoles.contains(StaffRoleType.PRIVATE_MODO);
            boolean canSee = isAdmin || (staffRole == StaffRoleType.MAPCREW && (isModerator || targetRoles.contains(StaffRoleType.MAPCREW))) || ((staffRole == StaffRoleType.PUBLIC_MODO || staffRole == StaffRoleType.PRIVATE_MODO) && isModerator);
            if (canSee)
                target.broadcastPacket(new PacketSendServerMessage(false, message));
        }
    }
}