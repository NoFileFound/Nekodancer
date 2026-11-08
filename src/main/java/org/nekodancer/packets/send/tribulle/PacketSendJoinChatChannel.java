package org.nekodancer.packets.send.tribulle;

// Imports
import org.nekodancer.network.NetByteArray;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

public final class PacketSendJoinChatChannel extends PSSendPacket {
    private final String channelName;

    /**
     * Creates a new packet of SEND_JOIN_CHAT_CHANNEL.
     * @param channelName The chat's channel name.
     */
    public PacketSendJoinChatChannel(String channelName) {
        super(PacketOpcodes.SEND_JOIN_CHAT_CHANNEL);
        this.channelName = channelName;
    }


    @Override
    public PSByteArray build() throws PSSendPacketException {
        return new NetByteArray().writeUTF(this.channelName, true);
    }
}