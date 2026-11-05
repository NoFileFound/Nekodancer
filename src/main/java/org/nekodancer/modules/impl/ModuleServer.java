package org.nekodancer.modules.impl;

// Imports
import org.nekodancer.GameServer;

public abstract class ModuleServer {
    protected final GameServer server;

    /**
     * Creates a new server module.
     * @param server The game's server.
     */
    protected ModuleServer(GameServer server) {
        this.server = server;
    }
}