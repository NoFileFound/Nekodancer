package org.nekodancer.database.data;

// Imports
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import java.util.ArrayList;
import java.util.List;
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

    /**
     * Creates a new player entity.
     */
    public PlayerEntity() {
        // Required by Morphia.
        this.staffRoles = new ArrayList<>();
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