package org.nekodancer.packets.send;

// Imports
import org.nekodancer.network.NetByteArray;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

public final class PacketSendTribeHouseInvitation extends PSSendPacket {
    private final String inviter;
    private final String inviterTribe;

    /**
     * Creates a new packet of SEND_TRIBE_HOUSE_INVITATION.
     * @param inviter The inviter's player name.
     * @param inviterTribe The inviter's tribe name.
     */
    public PacketSendTribeHouseInvitation(String inviter, String inviterTribe) {
        super(PacketOpcodes.SEND_TRIBE_HOUSE_INVITATION);
        this.inviter = inviter;
        this.inviterTribe = inviterTribe;
    }


    @Override
    public PSByteArray build() throws PSSendPacketException {
        NetByteArray packet = new NetByteArray();
        packet.writeUTF(this.inviter, true);
        packet.writeUTF(this.inviterTribe, true);
        return packet;
    }
}