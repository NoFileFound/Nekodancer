package org.nekodancer.packets.send.login;

// Imports
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

public final class PacketSendStaffServerPing extends PSSendPacket {
    /**
     * Creates a new value of SEND_STAFF_SERVER_PING.
     */
    public PacketSendStaffServerPing() {
        super(PacketOpcodes.SEND_STAFF_SERVER_PING);
    }


    @Override
    public PSByteArray build() throws PSSendPacketException {
        return new PSByteArray();
    }
}