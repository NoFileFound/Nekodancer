package org.nekodancer.packets.recv.tribe;

// Imports
import org.nekodancer.GameServer;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.network.NetSession;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSPacket;
import org.psframework.network.PSReceivePacket;
import org.psframework.network.PSReceivePacketException;

// Packets
import org.nekodancer.packets.send.informations.PacketSendTranslatedMessage;

@SuppressWarnings("unused")
@PSPacket(id = PacketOpcodes.RECV_TRIBE_HOUSE_INVITATION_ANSWER)
public final class PacketRecvTribeHouseInvitationAnswer extends PSReceivePacket<GameServer, NetSession, NetByteArray> {
    @Override
    public void handle(GameServer gameServer, NetSession client, NetByteArray psHeader, NetByteArray psData) throws PSReceivePacketException {
        var player = client.getPlayer();
        var inviter = gameServer.getPlayers().get(psData.readUTF());
        if (inviter == null) {
            player.broadcastPacket(new PacketSendTranslatedMessage("", "$InvTribu_MaisonVide"));
            return;
        }

        String tribeName = inviter.getPlayerData().tribeName;
        if (player.getInvitedTribeHouses().contains(tribeName))
            player.sendEnterRoom("*" + (char) 3 + tribeName, "");
    }
}