package org.nekodancer.database.data;

// Imports
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import java.util.ArrayList;
import java.util.List;
import org.nekodancer.data.objects.PlayerGender;
import org.nekodancer.data.objects.StaffRoleType;

@Entity("accounts")
public class PlayerEntity {
    @Id private int accountId;
    private String username;
    public String emailAddress;
    public String password;
    public long playedTime;
    private List<StaffRoleType> staffRoles;
    public boolean hasPublicAuthorization;
    private List<String> friendList;
    private List<String> ignoredList;
    public PlayerGender playerGender;
    public long lastConnectedTime;
    public int shopSushi;
    public String soulmate;
    public String tribeName;
    public TribeEntry.TribeRank tribeRank;
    public int playerKarma;
    public int cafeReputation;

    /**
     * Creates a new player entity.
     */
    public PlayerEntity() {
        // Required by Morphia.
        this.staffRoles = new ArrayList<>();
        this.friendList = new ArrayList<>();
        this.ignoredList = new ArrayList<>();
        this.tribeRank = new TribeEntry.TribeRank();
    }

    /**
     * Creates a new player entity.
     * @param username The player's username.
     * @param password The player's password.
     * @param email The player's email address.
     */
    public PlayerEntity(final int accountId, final String username, final String password, final String email, final String ipAddress) {
        this();
        this.accountId = accountId;
        this.username = username;
        this.emailAddress = email;
        this.password = password;
        this.hasPublicAuthorization = false;
        this.playerGender = PlayerGender.UNSPECIFIED;
        this.soulmate = "";
        this.tribeName = "";
    }

    /**
     * Creates a new player entity.
     * @param username The player's username.
     */
    public PlayerEntity(String username, String ipAddress) {
        this(0, username, "", "", ipAddress);
    }

    /**
     * Gets the player's account id.
     * @return The player's account id.
     */
    public int getAccountId() {
        return this.accountId;
    }

    /**
     * Gets the player's friend list.
     * @return The player's friend list.
     */
    public List<String> getFriendList() {
        return this.friendList;
    }

    /**
     * Gets the player's ignored list.
     * @return The player's blocked list.
     */
    public List<String> getIgnoredList() {
        return this.ignoredList;
    }

    /**
     * Gets the player's staff roles.
     * @return The player's staff roles.
     */
    public List<StaffRoleType> getStaffRoles() {
        return this.staffRoles;
    }

    /**
     * Gets the player's name.
     * @return The player's name.
     */
    public String getUsername() {
        return this.username;
    }
}