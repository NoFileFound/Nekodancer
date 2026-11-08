package org.nekodancer.packets.send.tribulle;

// Imports
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

public final class PacketSendTribulleSwitchProtocol extends PSSendPacket {
    private final boolean enableNewProtocol;

    /**
     * Creates a new packet of SEND_TRIBULLE_SWITCH_PROTOCOL.
     * @param enableNewProtocol True if should switch to newest tfm protocol or use the legacy one.
     */
    public PacketSendTribulleSwitchProtocol(boolean enableNewProtocol) {
        super(PacketOpcodes.SEND_TRIBULLE_SWITCH_PROTOCOL);
        this.enableNewProtocol = true;
    }


    @Override
    public PSByteArray build() throws PSSendPacketException {
        return new PSByteArray().writeBoolean(this.enableNewProtocol);
    }
}