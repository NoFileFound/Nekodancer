package org.nekodancer.config.nekoconfig;

// Imports
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class SwfConfig {
    @JsonProperty("version")
    private int version = 7;
    @JsonProperty("ports")
    private List<Integer> ports = List.of(14801, 15801);
    @JsonProperty("connection_key")
    private String connection_key = "ytVOiM";
    @JsonProperty("packet_keys")
    private List<Integer> packet_keys = List.of();
    @JsonProperty("login_keys")
    private List<Integer> login_keys = List.of(16777216, 134217728, 256, 16384, 2048, 14126415, 8655365);
    @JsonProperty("authorization_key")
    private long authorization_key = 0;
    @JsonProperty("swf_url")
    private String swf_url = "";

    /**
     * Creates a new config of the swf.
     */
    public SwfConfig() {

    }

    /**
     * Gets the swf's version.
     * @return The swf's version.
     */
    public int getVersion() {
        return this.version;
    }

    /**
     * Gets the swf's ports.
     * @return The swf's ports.
     */
    public List<Integer> getPorts() {
        return this.ports;
    }

    /**
     * Gets the swf's connection key.
     * @return The swf's connection key.
     */
    public String getConnectionKey() {
        return this.connection_key;
    }

    /**
     * Gets the swf's packet keys.
     * @return The swf's packet keys.
     */
    public List<Integer> getPacketKeys() {
        return this.packet_keys;
    }

    /**
     * Gets the swf's login keys.
     * @return The swf's login keys.
     */
    public List<Integer> getLoginKeys() {
        return this.login_keys;
    }

    /**
     * Gets the swf's authorization key.
     * @return The swf's authorization key.
     */
    public long getAuthorizationKey() {
        return this.authorization_key;
    }

    /**
     * Gets the swf's url.
     * @return The swf's url.
     */
    public String getSwfUrl() {
        return this.swf_url;
    }
}