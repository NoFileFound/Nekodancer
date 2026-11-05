package org.nekodancer.config.nekoconfig;

// Imports
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class GameConfig {
    @JsonProperty("login_attempts")
    private int login_attempts = 25;
    @JsonProperty("max_accounts_per_email")
    private int max_accounts_per_email = 8;
    @JsonProperty("max_players")
    private int max_players = 1000;
    @JsonProperty("chatfilter")
    private List<String> chatfilter;

    /**
     * Creates a new game config.
     */
    public GameConfig() {

    }

    /**
     * Gets the maximum login attempts.
     * @return The maximum login attempts.
     */
    public int getLoginAttempts() {
        return this.login_attempts;
    }

    /**
     * Gets the maximum accounts that one email address can have.
     * @return The maximum accounts that one email address can have.
     */
    public int getMaxAccountsPerEmail() {
        return this.max_accounts_per_email;
    }

    /**
     * Gets the maximum players to enter in the game.
     * @return The maximum players that can play on this server.
     */
    public int getMaxPlayers() {
        return this.max_players;
    }

    /**
     * Gets the server's blacklisted words.
     * @return The server's blacklisted words.
     */
    public List<String> getChatFilter() {
        return this.chatfilter;
    }
}