package org.nekodancer.database;

// Imports
import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.nekodancer.database.data.*;
import org.nekodancer.utils.Utils;
import org.psframework.database.providers.PSMongoDatabase;

public final class MongoGameDatabaseManager implements GameDatabaseManager {
    private final Datastore datastore;
    private final Map<String, PlayerEntity> playerCache;
    private final Map<String, EnumMap<SanctionEntry.SanctionType, SanctionEntry>> latestSanctionCache;
    private final Map<String, TribeEntry> tribeCache;
    private final boolean saveDatabase;

    /**
     * Creates the MongoDB database manager.
     * @param database The MongoDB's database instance.
     */
    public MongoGameDatabaseManager(PSMongoDatabase database, final boolean isDebug) {
        this.datastore = database.getDatastore();
        this.playerCache = new HashMap<>();
        this.latestSanctionCache = new HashMap<>();
        this.tribeCache = new HashMap<>();
        this.saveDatabase = !isDebug;
    }

    /**
     * Initializes the Mongo database.
     */
    @Override
    public void initialize() {

    }

    /**
     * Checks if the ip address is blacklisted.
     * @param ipAddress The given ip address.
     * @return True if the ip address is blacklisted or else False.
     */
    @Override
    public boolean isIpBlacklisted(String ipAddress) {
        IpTempBanEntry entry = this.datastore.find(IpTempBanEntry.class).filter(Filters.eq("ipAddress", ipAddress)).first();
        if (entry == null)
            return false;

        if (Utils.getUnixTime() >= entry.getStartTime() + entry.getDuration() * 3600L) {
            this.datastore.delete(entry);
            return false;
        }

        return true;
    }

    /**
     * Deletes a cafe topic.
     * @param cafeData The cafe topic's data.
     */
    @Override
    public void deleteCafe(CafeEntry cafeData) {
        if (!this.saveDatabase)
            return;

        this.datastore.delete(cafeData);
    }

    /**
     * Deletes a report.
     * @param reportEntry The report's data.
     */
    @Override
    public void deleteReport(ReportEntry reportEntry) {
        this.datastore.delete(reportEntry);
    }

    /**
     * Deletes a tribe.
     * @param tribeData The tribe's object.
     */
    @Override
    public void deleteTribe(TribeEntry tribeData) {
        if (!this.saveDatabase)
            return;

        this.datastore.delete(tribeData);
        this.tribeCache.remove(tribeData.getTribeName(), tribeData);
    }

    /**
     * Loads the cafe topics from the given community.
     * @param playerCommunity The given community.
     * @return List of cafe topics.
     */
    @Override
    public List<CafeEntry> loadCafeByCommunity(String playerCommunity) {
        try (var stream = this.datastore.find(CafeEntry.class).filter(Filters.eq("community", playerCommunity)).stream()) {
            return stream.toList();
        }
    }

    /**
     * Loads a cafe topic by topic id.
     * @param topicId The topic's id.
     * @return A cafe topic.
     */
    @Override
    public CafeEntry loadCafeTopicById(long topicId) {
        return this.datastore.find(CafeEntry.class).filter(Filters.eq("id", topicId)).first();
    }

    /**
     * Loads a cafe topic by post id.
     * @param postId The post's id.
     * @return A cafe topic.
     */
    @Override
    public CafeEntry loadCafeTopicByPostId(long postId) {
        return this.datastore.find(CafeEntry.class).filter(Filters.eq("posts._id", postId)).first();
    }

    /**
     * Loads the every sanction of the given player.
     * @param playerName The player's name.
     * @return List of his sanctions.
     */
    @Override
    public List<SanctionEntry> loadSanctionsByPlayer(String playerName) {
        return this.datastore.find(SanctionEntry.class).filter(Filters.eq("playerName", playerName)).iterator().toList();
    }

    /**
     * Loads the last active sanction by the player.
     * @param playerName The player's name.
     * @param sanctionType The sanction's type.
     * @return The last sanction.
     */
    @Override
    public synchronized SanctionEntry loadLastSanction(String playerName, SanctionEntry.SanctionType sanctionType) {
        if (playerName == null || playerName.isBlank())
            return null;

        EnumMap<SanctionEntry.SanctionType, SanctionEntry> latestSanctions = this.latestSanctionCache.get(playerName);
        if (latestSanctions != null && latestSanctions.containsKey(sanctionType)) {
            SanctionEntry cached = latestSanctions.get(sanctionType);
            if (cached == null)
                return null;

            if (cached.getExpirationDate() != -1L && cached.getExpirationDate() <= Utils.getUnixTime()) {
                cached.sanctionState = SanctionEntry.SanctionState.EXPIRED;
                this.datastore.save(cached);
                latestSanctions.put(sanctionType, null);
                return null;
            }

            return cached;
        }

        SanctionEntry sanction;
        try (var stream = this.datastore.find(SanctionEntry.class).filter(Filters.eq("playerName", playerName), Filters.eq("sanctionType", sanctionType), Filters.eq("sanctionState", SanctionEntry.SanctionState.ACTIVE)).stream()) {
            sanction = stream.max(Comparator.comparingLong(SanctionEntry::getCreatedDate)).orElse(null);
        }

        if (sanction == null) {
            this.latestSanctionCache.computeIfAbsent(playerName, _ -> new EnumMap<>(SanctionEntry.SanctionType.class)).put(sanctionType, null);
            return null;
        }

        if (sanction.getExpirationDate() != -1L && sanction.getExpirationDate() <= Utils.getUnixTime()) {
            sanction.sanctionState = SanctionEntry.SanctionState.EXPIRED;
            this.datastore.save(sanction);
            this.latestSanctionCache.computeIfAbsent(playerName, _ -> new EnumMap<>(SanctionEntry.SanctionType.class)).put(sanctionType, null);
            return null;
        }

        this.latestSanctionCache.computeIfAbsent(playerName, _ -> new EnumMap<>(SanctionEntry.SanctionType.class)).put(sanctionType, sanction);
        return sanction;
    }

    /**
     * Loads the player from the database.
     * @param nickname The player's username.
     * @return The player data.
     */
    @Override
    public PlayerEntity loadPlayerByName(String nickname) {
        if (this.playerCache.containsKey(nickname))
            return this.playerCache.get(nickname);

        PlayerEntity entity = this.datastore.find(PlayerEntity.class).filter(Filters.eq("username", nickname)).first();
        if (entity != null)
            this.playerCache.put(entity.getUsername(), entity);

        return entity;
    }

    /**
     * Loads all players with the given email address.
     * @param email The given email address.
     * @param password The given password. If the password is {@code  ""} then it will get every player with email address with any password.
     * @return List of players.
     */
    @Override
    public List<PlayerEntity> loadPlayersByEmail(String email, String password) {
        var players = this.datastore.find(PlayerEntity.class).filter(Filters.eq("emailAddress", email)).iterator().toList();
        if (password.isEmpty())
            return players;

        return players.stream().anyMatch(player -> password.equals(player.password)) ? players : List.of();
    }

    /**
     * Loads the reports.
     * @return The reports.
     */
    @Override
    public List<ReportEntry> loadReports() {
        try (var stream = this.datastore.find(ReportEntry.class).stream()) {
            return stream.toList();
        }
    }

    /**
     * Loads the tribe from the database.
     * @param tribeName The tribe's name.
     * @return Tribe data from the database.
     */
    @Override
    public TribeEntry loadTribeByName(String tribeName) {
        if (this.tribeCache.containsKey(tribeName))
            return this.tribeCache.get(tribeName);

        TribeEntry entity = this.datastore.find(TribeEntry.class).filter(Filters.eq("tribeName", tribeName)).first();
        if (entity != null)
            this.tribeCache.put(entity.getTribeName(), entity);

        return entity;
    }

    /**
     * Saves the cafe topic. in the database.
     * @param cafeData The cafe topic's data.
     */
    @Override
    public void saveCafe(CafeEntry cafeData) {
        if (!this.saveDatabase)
            return;

        this.datastore.save(cafeData);
    }

    /**
     * Saves the connection log in the database.
     * @param connectionLogEntry The connection log's data.
     */
    @Override
    public void saveConnectionLog(ConnectionLogEntry connectionLogEntry) {
        this.datastore.save(connectionLogEntry);
    }

    /**
     * Saves the player in the database.
     * @param playerData The player's data.
     */
    @Override
    public void savePlayer(PlayerEntity playerData) {
        if (!this.saveDatabase) {
            this.playerCache.put(playerData.getUsername(), playerData);
            return;
        }

        this.datastore.save(playerData);
        this.playerCache.put(playerData.getUsername(), playerData);
    }

    /**
     * Saves the report in the database.
     * @param reportData The report's data.
     */
    @Override
    public void saveReport(ReportEntry reportData) {
        this.datastore.save(reportData);
    }

    /**
     * Saves the sanction in the database.
     * @param mySanction The sanction's data.
     */
    @Override
    public void saveSanction(SanctionEntry mySanction) {
        if (!this.saveDatabase)
            return;

        this.datastore.save(mySanction);
    }

    /**
     * Saves the tribe in the database.
     * @param tribeData The tribe's data.
     */
    @Override
    public void saveTribe(TribeEntry tribeData) {
        if (!this.saveDatabase) {
            this.tribeCache.put(tribeData.getTribeName(), tribeData);
            return;
        }

        this.datastore.save(tribeData);
        this.tribeCache.put(tribeData.getTribeName(), tribeData);
    }

    /**
     * Saves the ip temp ban.
     * @param ipTempBanEntry The ip temp ban's data.
     */
    @Override
    public void saveIpTempBan(IpTempBanEntry ipTempBanEntry) {
        this.datastore.save(ipTempBanEntry);
    }
}