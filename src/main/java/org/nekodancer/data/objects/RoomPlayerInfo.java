package org.nekodancer.data.objects;

public final class RoomPlayerInfo {
    public String playerName;
    public int sessionId;
    public int playerScore;
    public boolean isDead;
    public int playerFurId;

    /**
     * Creates a new player information in the room.
     * @param playerName The player's nickname.
     * @param sessionId The player's session id.
     */
    public RoomPlayerInfo(String playerName, int sessionId, int playerScore, boolean isDead, int playerFurId) {
        this.playerName = playerName;
        this.sessionId = sessionId;
        this.playerScore = playerScore;
        this.isDead = isDead;
        this.playerFurId = playerFurId;
    }
}