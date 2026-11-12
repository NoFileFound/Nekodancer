package org.nekodancer.database.data;

// Imports
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import java.util.ArrayList;
import java.util.List;
import org.nekodancer.data.objects.PlayerGender;
import org.nekodancer.data.objects.StaffRoleType;
import org.nekodancer.utils.Utils;

@Entity("accounts")
public class PlayerEntity {
    @Id private int accountId;
    private String username;
    public String emailAddress;
    public String password;
    public long playedTime;
    private long createdDate;
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
    public int currentCatFurId;
    private List<MusicInfo> personalMusicPlaylist;
    private List<ShopItem> shopItems;

    /**
     * Creates a new player entity.
     */
    public PlayerEntity() {
        // Required by Morphia.
        this.staffRoles = new ArrayList<>();
        this.friendList = new ArrayList<>();
        this.ignoredList = new ArrayList<>();
        this.tribeRank = new TribeEntry.TribeRank();
        this.personalMusicPlaylist = new ArrayList<>();
        this.shopItems = new ArrayList<>();
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
        this.createdDate = Utils.getUnixTime();
        this.hasPublicAuthorization = false;
        this.playerGender = PlayerGender.FEMALE;
        this.soulmate = "";
        this.tribeName = "";
        this.currentCatFurId = 1;

        // [0, 1] - head
        // 3 - t-shirt
        // 7 - pants
        // 9 - shoes
        // 10 - eyes

        this.shopItems.add(new ShopItem(0, 3003)); // HEAD
        this.shopItems.add(new ShopItem(1, 3003)); // HEAD
        this.shopItems.add(new ShopItem(3, 13)); // T-SHIRT
        this.shopItems.add(new ShopItem(10, 1014)); // EYES
        this.shopItems.add(new ShopItem(18, 6000)); // SKIRT
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
     * Gets the player's created date.
     * @return The player's created date.
     */
    public long getCreatedDate() {
        return this.createdDate;
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
     * Gets the player's shop items.
     * @return The player's shop items.
     */
    public List<ShopItem> getShopItems() {
        return this.shopItems;
    }

    /**
     * Gets the player's personal music playlist.
     * @return The player's personal music playlist.
     */
    public List<MusicInfo> getPersonalMusicPlaylist() {
        return this.personalMusicPlaylist;
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

    /**
     * Creates a music info.
     */
    @Embedded
    public static class MusicInfo {
        @Id private int musicId;
        private String musicTitle;
        private String musicBackgroundImage;
        private String ytId;

        /**
         * Creates a new music info.
         */
        public MusicInfo() {
            // Required by Morphia.
        }

        /**
         * Creates a new music info.
         * @param musicId The music's id.
         * @param musicTitle The music's title.
         * @param musicBackgroundImage The music's background image.
         */
        public MusicInfo(int musicId, String musicTitle, String musicBackgroundImage, String ytId) {
            this();
            this.musicId = musicId;
            this.musicTitle = musicTitle;
            this.musicBackgroundImage = musicBackgroundImage;
            this.ytId = ytId;
        }

        /**
         * Gets the music's id.
         * @return The music's id.
         */
        public int getMusicId() {
            return this.musicId;
        }

        /**
         * Gets the music's title name.
         * @return The music's title name.
         */
        public String getMusicTitle() {
            return this.musicTitle;
        }

        /**
         * Gets the music's background image.
         * @return The music's background image.
         */
        public String getMusicBackgroundImage() {
            return this.musicBackgroundImage;
        }

        /**
         * Gets the youtube's video id.
         * @return The youtube's video id.
         */
        public String getYoutubeVideoId() {
            return this.ytId;
        }
    }

    /**
     * Creates a new shop item.
     */
    @Embedded
    public static class ShopItem {
        private int itemCategory;
        private int itemId;

        /**
         * Creates a new shop item.
         */
        public ShopItem() {
            // Required by Morphia.
        }

        /**
         * Creates a new shop item.
         * @param itemCategory The item's category.
         * @param itemId The item's id.
         */
        public ShopItem(int itemCategory, int itemId) {
            this();
            this.itemCategory = itemCategory;
            this.itemId = itemId;
        }

        /**
         * Gets the item's id.
         * @return The item's id.
         */
        public int getItemId() {
            return this.itemId;
        }

        /**
         * Gets the item's category.
         * @return The item's category.
         */
        public int getItemCategory() {
            return this.itemCategory;
        }
    }
}