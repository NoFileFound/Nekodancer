package org.nekodancer.packets.send.neko;

// Imports
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

public final class PacketSendNekoSushiRewardDisplay extends PSSendPacket {
    private final boolean isEnabled;

    /**
     * Creates a new packet of SEND_NEKO_SUSHI_REWARD_DISPLAY.
     * @param isEnabled Is the award activated.
     */
    public PacketSendNekoSushiRewardDisplay(boolean isEnabled) {
        super(PacketOpcodes.SEND_NEKO_SUSHI_REWARD_DISPLAY);
        this.isEnabled = isEnabled;
    }


    @Override
    public PSByteArray build() throws PSSendPacketException {
        return new PSByteArray().writeBoolean(this.isEnabled);
    }
}