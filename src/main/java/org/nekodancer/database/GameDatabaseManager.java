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
     * Checks if the ip address is blacklisted.
     * @param ipAddress The given ip address.
     * @return True if the ip address is blacklisted or else False.
     */
    boolean isIpBlacklisted(String ipAddress);

    /**
     * Deletes a cafe topic.
     * @param cafeData The cafe topic's data.
     */
    void deleteCafe(CafeEntry cafeData);

    /**
     * Deletes a ip temp ban.
     * @param ipTempBanData The ip temp's data.
     */
    void deleteIpTempBan(IpTempBanEntry ipTempBanData);

    /**
     * Deletes a report.
     * @param report The report's data.
     */
    void deleteReport(ReportEntry report);

    /**
     * Deletes a tribe.
     * @param tribeData The tribe's object.
     */
    void deleteTribe(TribeEntry tribeData);

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
     * Loads command logs by player name.
     * @param playerName The player's name.
     * @return A list of recently entered commands.
     */
    List<CommandLogEntry> loadCommandLogByPlayer(String playerName);

    /**
     * Loads a connection logs by IP address.
     * @param ipAddress The IP address.
     * @return List of recently connections.
     */
    List<ConnectionLogEntry> loadConnectionLogsByIPAddress(String ipAddress);

    /**
     * Loads a connection logs by player name.
     * @param playerName The player's name.
     * @return List of his recently connections.
     */
    List<ConnectionLogEntry> loadConnectionLogsByPlayer(String playerName);

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
     * Loads the every sanction of the given player.
     * @param playerName The player's name.
     * @return List of his sanctions.
     */
    List<SanctionEntry> loadSanctionsByPlayer(String playerName);

    /**
     * Loads the last active sanction by the player.
     * @param playerName The player's name.
     * @param sanctionType The sanction's type.
     * @return The last sanction.
     */
    SanctionEntry loadLastSanction(String playerName, SanctionEntry.SanctionType sanctionType);

    /**
     * Loads the reports.
     * @return The reports.
     */
    List<ReportEntry> loadReports();

    /**
     * Loads temp ip ban info.
     * @param ipAddress The given ip address.
     * @return The temp ip ban info.
     */
    IpTempBanEntry loadTempIpBanByIp(String ipAddress);

    /**
     * Loads the tribe from the database.
     * @param tribeName The tribe's name.
     * @return Tribe data from the database.
     */
    TribeEntry loadTribeByName(String tribeName);

    /**
     * Saves the cafe topic. in the database.
     * @param cafeData The cafe topic's data.
     */
    void saveCafe(CafeEntry cafeData);

    /**
     * Saves the command log.
     * @param commandLogEntry The command log's data.
     */
    void saveCommandLog(CommandLogEntry commandLogEntry);

    /**
     * Saves the connection log.
     * @param connectionLogEntry The connection log's data.
     */
    void saveConnectionLog(ConnectionLogEntry connectionLogEntry);

    /**
     * Saves the player in the database.
     * @param playerData The player's data.
     */
    void savePlayer(PlayerEntity playerData);

    /**
     * Saves the reports in the database.
     * @param report The report's data.
     */
    void saveReport(ReportEntry report);

    /**
     * Saves the sanction in the database.
     * @param mySanction The sanction's data.
     */
    void saveSanction(SanctionEntry mySanction);

    /**
     * Saves the tribe in the database.
     * @param tribeData The tribe's data.
     */
    void saveTribe(TribeEntry tribeData);

    /**
     * Saves the ip temp ban.
     * @param ipTempBanEntry The ip temp ban's data.
     */
    void saveIpTempBan(IpTempBanEntry ipTempBanEntry);
}