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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.fusesource.jansi.AnsiConsole;
import org.nekodancer.config.NekodancerConfig;
import org.nekodancer.network.NetDecoder;
import org.nekodancer.network.NetHandler;
import org.nekodancer.network.NetSession;
import org.psframework.admin.PSAdminConfig;
import org.psframework.database.PSDatabaseConfig;
import org.psframework.game.PSGameServer;
import org.psframework.logging.PSLogger;
import org.psframework.network.PSPacketHandler;
import org.psframework.utils.PSGeoIP;

public final class GameServer extends PSGameServer {
    private final NekodancerConfig nekodancerConfig;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final List<Channel> channels;
    private final PSPacketHandler generalPacketHandler;
    private final Map<ChannelId, NetSession> clientSessions;
    private final Map<String, GamePlayer> players;
    private final Map<String, GameRoom> rooms;
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
            this.generalPacketHandler.registerPackets();
            this.logger.debug("[PacketHandler] Registered successfully.");
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
     * Gets the list of connected sessions to the server.
     * @return The list of connected sessions.
     */
    public Map<ChannelId, NetSession> getClientSessions() {
        return this.clientSessions;
    }

    /**
     * Gets the server config.
     * @return The server config.
     */
    public NekodancerConfig getConfig() {
        return this.nekodancerConfig;
    }

    /**
     * Gets the GeoIP instance.
     * @return The GeoIP instance.
     */
    public PSGeoIP getGeoIP() {
        return this.geoIP;
    }

    /**
     * Gets the packet handler.
     * @return The packet handler.
     */
    public PSPacketHandler getPacketHandler() {
        return this.generalPacketHandler;
    }

    /**
     * Gets the list of players.
     * @return The list of players.
     */
    public Map<String, GamePlayer> getPlayers() {
        return this.players;
    }
}