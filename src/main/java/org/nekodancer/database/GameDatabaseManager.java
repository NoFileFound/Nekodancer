package org.nekodancer.database;

// Imports
import java.util.List;
import org.nekodancer.database.data.*;

public interface GameDatabaseManager {
    /**
     * Initializes the database.
     */
    void initialize();

    /**
     * Deletes a cafe topic.
     * @param cafeData The cafe topic's data.
     */
    void deleteCafe(CafeEntry cafeData);

    /**
     * Loads the cafe topics from the given community.
     * @param playerCommunity The given community.
     * @return List of cafe topics.
     */
    List<CafeEntry> loadCafeByCommunity(String playerCommunity);

    /**
     * Loads a cafe topic by topic id.
     * @param topicId The topic's id.
     * @return A cafe topic.
     */
    CafeEntry loadCafeTopicById(long topicId);

    /**
     * Loads a cafe topic by post id.
     * @param postId The post's id.
     * @return A cafe topic.
     */
    CafeEntry loadCafeTopicByPostId(long postId);

    /**
     * Loads the player from the database.
     * @param nickname The player's username.
     * @return Player data from the database.
     */
    PlayerEntity loadPlayerByName(String nickname);

    /**
     * Loads all players with the given email address.
     * @param email The given email address.
     * @param password The given password. If the password is {@code  ""} then it will get every player with email address with any password.
     * @return List of players.
     */
    List<PlayerEntity> loadPlayersByEmail(String email, String password);

    /**
     * Loads the last active sanction by the player.
     * @param playerName The player's name.
     * @param sanctionType The sanction's type.
     * @return The last sanction.
     */
    SanctionEntry loadLastSanction(String playerName, SanctionEntry.SanctionType sanctionType);

    /**
     * Saves the cafe topic. in the database.
     * @param cafeData The cafe topic's data.
     */
    void saveCafe(CafeEntry cafeData);

    /**
     * Saves the player in the database.
     * @param playerData The player's data.
     */
    void savePlayer(PlayerEntity playerData);

    /**
     * Saves the sanction in the database.
     * @param mySanction The sanction's data.
     */
    void saveSanction(SanctionEntry mySanction);
}