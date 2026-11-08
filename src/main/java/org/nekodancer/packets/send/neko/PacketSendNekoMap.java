package org.nekodancer.packets.send.neko;

import org.nekodancer.network.NetByteArray;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

public class PacketSendNekoMap extends PSSendPacket {
    private final int mapId;
    private final boolean hasOverlay;

    /**
     * Creates a new send packet of SEND_NEKO_MAP.
     * @param mapId The map's id.
     * @param hasOverlay Has the map an overlay.
     */
    public PacketSendNekoMap(int mapId, boolean hasOverlay) {
        super(PacketOpcodes.SEND_NEKO_MAP);
        this.mapId = mapId;
        this.hasOverlay = hasOverlay;
    }

    @Override
    public PSByteArray build() throws PSSendPacketException {
        NetByteArray packet = new NetByteArray();
        packet.writeUnsignedShort(0);
        packet.writeShort((short) this.mapId);
        packet.writeBoolean(this.hasOverlay);
        packet.writeShort((short) 3);
        packet.writeShort((short) 4);
        packet.writeShort((short) 5);
        packet.writeShort((short) 6);
        packet.writeShort((short) 7);
        packet.writeByte(8);
        return packet;
    }
}

/// TODO: FIX THIS.