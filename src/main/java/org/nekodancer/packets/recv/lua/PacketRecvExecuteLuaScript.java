package org.nekodancer.packets.recv.lua;

// Imports
import java.nio.charset.StandardCharsets;
import org.nekodancer.data.objects.StaffRoleType;
import org.nekodancer.GameServer;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.network.NetSession;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSPacket;
import org.psframework.network.PSReceivePacket;
import org.psframework.network.PSReceivePacketException;

@SuppressWarnings("unused")
@PSPacket(id = PacketOpcodes.RECV_EXECUTE_LUA_SCRIPT)
public final class PacketRecvExecuteLuaScript extends PSReceivePacket<GameServer, NetSession, NetByteArray> {
    @Override
    public void handle(GameServer gameServer, NetSession client, NetByteArray psHeader, NetByteArray psData) throws PSReceivePacketException {
        if (client.getPlayer().hasStaffPermission(StaffRoleType.ADMIN, false)) {
            int length = (psData.readUnsignedByte() << 16) | (psData.readUnsignedByte() << 8) | psData.readUnsignedByte();
            String luaScript = new String(psData.readBytes(length), StandardCharsets.UTF_8);
            client.getPlayer().getPlayerRoom().getParseLua().executeScript(luaScript, client.getPlayer());
        }
    }
}