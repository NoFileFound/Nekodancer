package org.nekodancer.packets.send.legacy;

// Imports
import org.nekodancer.network.NetByteArray;
import org.nekodancer.packets.LegacyPacketOpcodes;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

public final class PacketSendPlayerBanScreenLogin extends PSSendPacket {
    private final String reason;
    private final long duration;

    /**
     * Creates a legacy packet of SendPlayerBanScreen.
     * @param reason The ban reason.
     * @param duration The ban duration.
     */
    public PacketSendPlayerBanScreenLogin(String reason, long duration) {
        super(LegacyPacketOpcodes.SendPlayerBanScreenLogin);
        this.reason = reason;
        this.duration = (duration == -1 ? 0 : duration);
    }


    @Override
    public PSByteArray build() throws PSSendPacketException {
        NetByteArray packet = new NetByteArray();
        if (this.duration > 0) {
            packet.writeUTF(String.valueOf(this.duration), false);
            packet.writeByte(1);
        }

        packet.writeUTF(this.reason, false);
        return packet;
    }
}