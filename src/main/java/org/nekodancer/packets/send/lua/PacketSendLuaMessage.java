package org.nekodancer.packets.send.lua;

// Imports
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.packets.PacketOpcodes;

public final class PacketSendLuaMessage extends PSSendPacket {
    private final String message;

    /**
     * Creates a new packet of SEND_LUA_MESSAGE.
     * @param message The message to send.
     */
    public PacketSendLuaMessage(String message) {
        super(PacketOpcodes.SEND_LUA_MESSAGE);
        this.message = message;
    }


    @Override
    public PSByteArray build() throws PSSendPacketException {
        return new NetByteArray().writeUTF(this.message, true);
    }
}