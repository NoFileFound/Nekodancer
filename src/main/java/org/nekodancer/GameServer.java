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
import org.nekodancer.modules.ModuleCafe;
import org.nekodancer.network.NetDecoder;
import org.nekodancer.network.NetHandler;
import org.nekodancer.network.NetSession;
import org.nekodancer.utils.PlayerQueue;
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
import org.nekodancer.packets.send.chat.PacketSendStaffChatMessage;

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
    private final Pattern chatFilterPattern;
    private final Map<ChannelId, NetSession> clientSessions;
    private final Map<String, PSTimer> accountCreationTimer;
    private final Map<String, Long> tempBannedIPs;
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
        this.tempBannedIPs = new ConcurrentHashMap<>();
        this.players = new ConcurrentHashMap<>();
        this.rooms = new ConcurrentHashMap<>();
        this.playerQueue = new PlayerQueue(this.nekodancerConfig.getGameConfig().getMaxPlayers());
        this.chatFilterPattern = Pattern.compile("(?iu)(" + this.nekodancerConfig.getGameConfig().getChatFilter().stream().map(word -> word.codePoints().mapToObj(cp -> Pattern.quote(new String(Character.toChars(cp)))).collect(Collectors.joining("[\\p{P}\\p{S}]*"))).collect(Collectors.joining("|")) + ")");

        // Instances
        this.commandManager = new CommandLoader(this);
        this.parseCafe = new ModuleCafe(this);

        // Timers
        this.accountCreationTimer = new HashMap<>();
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
     * Gets the server's scheduler.
     * @return The server's scheduler.
     */
    public ScheduledExecutorService getScheduler() {
        return this.scheduler;
    }

    /**
     * Gets the server's temperately banned IP Addresses.
     * @return The server's temp banned IPs.
     */
    public Map<String, Long> getTempBannedIPs() {
        return this.tempBannedIPs;
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










    public String getRecommendedRoom(String roomName, String playerCommunity) {
        ///  TODO: IMPLEMENT
        return "JP-1";
    }
}