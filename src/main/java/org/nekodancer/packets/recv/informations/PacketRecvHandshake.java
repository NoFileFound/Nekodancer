package org.nekodancer.packets.recv.informations;

// Imports
import org.nekodancer.GameServer;
import org.nekodancer.data.objects.PlayerConnectionInfo;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.network.NetSession;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSPacket;
import org.psframework.network.PSReceivePacket;
import org.psframework.network.PSReceivePacketException;

// Packets
import org.nekodancer.packets.send.informations.PacketSendHandshake;

@SuppressWarnings("unused")
@PSPacket(id = PacketOpcodes.RECV_HANDSHAKE)
public final class PacketRecvHandshake extends PSReceivePacket<GameServer, NetSession, NetByteArray> {
    @Override
    public void handle(GameServer gameServer, NetSession client, NetByteArray psHeader, NetByteArray psData) throws PSReceivePacketException {
        int version = psData.readShort();
        String language = psData.readUTF();
        String connectionToken = psData.readUTF();
        String connectionPlatform = psData.readUTF();
        String browserInfo = psData.readUTF();
        int loaderStageSize = psData.readInt();
        String CCFData = psData.readUTF();
        String fontNameHash = psData.readUTF();
        String serverString = psData.readUTF();
        int referrerId = psData.readInt();
        int timestamp = psData.readInt();
        psData.readUTF();
        if (version != gameServer.getConfig().getSWFConfig().getVersion() || !connectionToken.equals(gameServer.getConfig().getSWFConfig().getConnectionKey())) {
            throw new PSReceivePacketException(String.format("[%s] Mismatched SWF Data: %s | %s", client.getIpAddress(), version, connectionToken));
        }

        // Handshake
        client.setConnectionData(new PlayerConnectionInfo(connectionPlatform, browserInfo, loaderStageSize, CCFData, fontNameHash, serverString, referrerId, timestamp));
        client.sendData(new PacketSendHandshake(gameServer.getPlayers().size(), client.getCountryCode(), language, gameServer.getConfig().getSWFConfig().getAuthorizationKey(), true));
        client.setHasPassedHandShake(true);

        // Verification
        client.setHasPassedBotVerification(true);
    }
}