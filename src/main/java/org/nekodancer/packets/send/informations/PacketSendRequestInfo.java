package org.nekodancer.packets.send.informations;

// Imports
import org.nekodancer.network.NetByteArray;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

public final class PacketSendRequestInfo extends PSSendPacket {
    /**
     * Creates a new packet of SEND_REQUEST_INFO.
     */
    public PacketSendRequestInfo() {
        super(PacketOpcodes.SEND_REQUEST_INFO);
    }


    @Override
    public PSByteArray build() throws PSSendPacketException {
        return new NetByteArray().writeUTF("http://51.158.113.197/info.php", true);
    }
}