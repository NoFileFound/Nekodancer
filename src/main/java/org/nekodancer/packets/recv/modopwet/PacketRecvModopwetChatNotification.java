package org.nekodancer.packets.recv.modopwet;

// Imports
import java.util.ArrayList;
import java.util.List;
import org.nekodancer.GamePlayer;
import org.nekodancer.GameServer;
import org.nekodancer.data.objects.StaffRoleType;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.network.NetSession;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSPacket;
import org.psframework.network.PSReceivePacket;
import org.psframework.network.PSReceivePacketException;

@SuppressWarnings("unused")
@PSPacket(id = PacketOpcodes.RECV_MODOPWET_CHAT_NOTIFICATION)
public final class PacketRecvModopwetChatNotification extends PSReceivePacket<GameServer, NetSession, NetByteArray> {
    @Override
    public void handle(GameServer gameServer, NetSession client, NetByteArray psHeader, NetByteArray psData) throws PSReceivePacketException {
        GamePlayer player = client.getPlayer();
        if (!player.hasStaffPermission(StaffRoleType.PUBLIC_MODO, false) && !player.hasStaffPermission(StaffRoleType.PRIVATE_MODO, false))
            return;

        boolean isSubscribed = psData.readBoolean();
        int len = psData.readByte();
        List<String> communities = new ArrayList<>(len);
        for (int i = 0; i < len; i++)
            communities.add(psData.readUTF());

        gameServer.getParseModopwet().handleChatNotification(player, isSubscribed, communities);
    }
}