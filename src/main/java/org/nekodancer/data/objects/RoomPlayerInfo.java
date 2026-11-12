package org.nekodancer.data.objects;

// Imports
import java.util.List;
import org.nekodancer.database.data.PlayerEntity;

public final class RoomPlayerInfo {
    public String playerName;
    public int sessionId;
    public int positionX;
    public int positionY;
    public int playerScore;
    public boolean isDead;
    public int playerFurId;
    public List<PlayerEntity.ShopItem> shopItemList;
    public int noMissRate;
    public int totalMisses;

    /**
     * Creates a new player information in the room.
     * @param playerName The player's nickname.
     * @param sessionId The player's session id.
     * @param positionX The player's position X.
     * @param positionY The player's position Y.
     * @param playerScore The player's score.
     * @param isDead is the player dead.
     * @param playerFurId The player's fur id.
     * @param shopItemList The player's equipped shop items.
     * @param noMissRate The player's perfect percentage.
     */
    public RoomPlayerInfo(String playerName, int sessionId, int positionX, int positionY, int playerScore, boolean isDead, int playerFurId, List<PlayerEntity.ShopItem> shopItemList) {
        this.playerName = playerName;
        this.sessionId = sessionId;
        this.positionX = positionX;
        this.positionY = positionY;
        this.playerScore = playerScore;
        this.isDead = isDead;
        this.playerFurId = playerFurId;
        this.shopItemList = shopItemList;
        this.noMissRate = noMissRate;
    }
}