package org.nekodancer;

// Imports
import org.nekodancer.network.NetSession;

public class GamePlayer {
    private final GameServer server;
    private final NetSession session;

    /**
     * Creates a new player.
     * @param session The player's session.
     * @param isGuest Is the player a guest or a registered account.
     */
    public GamePlayer(NetSession session, boolean isGuest) {
        this.server = (GameServer) session.getServer();
        this.session = session;
    }

    /**
     * Disconnects the player from the game.
     * @param isClientSide is the function being called from the client.
     */
    public void disconnect(boolean isClientSide) {
        if (isClientSide) {
            this.session.disconnect();
        } else {

        }
    }
}