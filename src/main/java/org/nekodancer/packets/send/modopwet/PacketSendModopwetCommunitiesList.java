package org.nekodancer.packets.send.modopwet;

// Imports
import java.util.List;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

public final class PacketSendModopwetCommunitiesList extends PSSendPacket {
    private final List<String> communities;

    /**
     * Creates a new packet of SEND_MODOPWET_COMMUNITIES_LIST.
     * @param communities The communities list.
     */
    public PacketSendModopwetCommunitiesList(List<String> communities) {
        super(PacketOpcodes.SEND_MODOPWET_COMMUNITIES_LIST);
        this.communities = communities;
    }


    @Override
    public PSByteArray build() throws PSSendPacketException {
        NetByteArray packet = new NetByteArray();
        packet.writeUnsignedShort(this.communities.size());
        for (String community : this.communities)
            packet.writeUTF(community, true);

        return packet;
    }
}