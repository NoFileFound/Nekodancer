package org.nekodancer.packets.recv.login;

// Imports
import org.nekodancer.GamePlayer;
import org.nekodancer.GameServer;
import org.nekodancer.data.objects.AccountErrorType;
import org.nekodancer.database.data.IpTempBanEntry;
import org.nekodancer.database.data.PlayerEntity;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.network.NetSession;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSPacket;
import org.psframework.network.PSReceivePacket;
import org.psframework.network.PSReceivePacketException;
import org.psframework.utils.PSRandom;

// Packets
import org.nekodancer.packets.send.login.PacketSendAccountLoginError;
import org.nekodancer.packets.send.login.PacketSendLoginQueuePosition;

@SuppressWarnings("unused")
@PSPacket(id = PacketOpcodes.RECV_LOGIN_PLAYER)
public final class PacketRecvLogin extends PSReceivePacket<GameServer, NetSession, NetByteArray> {
    @Override
    public void handle(GameServer gameServer, NetSession client, NetByteArray psHeader, NetByteArray psData) throws PSReceivePacketException {
        NetByteArray originalData = new NetByteArray(psData.toByteArray());
        psData = psData.decryptIdentification(gameServer.getConfig().getSWFConfig().getPacketKeys(), "identification");
        String nickname = psData.readUTF();
        String password = psData.readUTF();
        String swfUrl = psData.readUTF();
        String startRoom = psData.readUTF();
        int authKey = psData.readInt();
        for (int key : gameServer.getConfig().getSWFConfig().getLoginKeys())
            authKey ^= key;

        if (authKey != gameServer.getConfig().getSWFConfig().getAuthorizationKey())
            throw new PSReceivePacketException(String.format("[%s][PacketRecvLogin] Mismatch between the authorization key. %d != %d", client.getIpAddress(), authKey, gameServer.getConfig().getSWFConfig().getAuthorizationKey()));

        if (!gameServer.getConfig().getSWFConfig().getSwfUrl().isEmpty() && !gameServer.getConfig().getSWFConfig().getSwfUrl().equals(swfUrl))
            throw new PSReceivePacketException(String.format("[%s][PacketRecvLogin] Tried to log in with unofficial SWF %s.", client.getIpAddress(), swfUrl));

        if (password.isEmpty()) {
            do {
                nickname = "*Souris_" + PSRandom.nextString(5);
            } while (gameServer.getPlayers().containsKey(nickname));
            try {
                if (!gameServer.getPlayerQueue().join(client, psHeader, originalData)) {
                    client.sendData(new PacketSendLoginQueuePosition(gameServer.getPlayerQueue().getQueuePosition(client)));
                    return;
                }

                client.setPlayer(new GamePlayer(client, true, new PlayerEntity(nickname, client.getIpAddress())));
                client.getPlayer().sendLogin(startRoom);
            } catch (Exception ex) {
                client.sendData(new PacketSendAccountLoginError(AccountErrorType.INTERNAL_ERROR, "", ""));
                throw new RuntimeException(ex);
            }
            return;
        }

        if (client.getLoginAttempts() >= gameServer.getConfig().getGameConfig().getLoginAttempts()) {
            gameServer.getDatabase().saveIpTempBan(new IpTempBanEntry(client.getIpAddress(), 24, "Bruteforcing accounts"));
            client.disconnect();
            return;
        }

        try {
            PlayerEntity entity = gameServer.getDatabase().loadPlayerByName(nickname);
            if (entity == null || !entity.password.equals(password)) {
                client.setLoginAttempts(client.getLoginAttempts() + 1);
                client.sendData(new PacketSendAccountLoginError(AccountErrorType.WRONG_PASSWORD, "", ""));
                return;
            }

            if (gameServer.getPlayers().containsKey(entity.getUsername())) {
                client.sendData(new PacketSendAccountLoginError(AccountErrorType.ALREADY_CONNECTED, "", ""));
                return;
            }

            if (!gameServer.getPlayerQueue().join(client, psHeader, originalData)) {
                client.sendData(new PacketSendLoginQueuePosition(gameServer.getPlayerQueue().getQueuePosition(client)));
                return;
            }

            client.setPlayer(new GamePlayer(client, false, entity));
            client.getPlayer().sendLogin(startRoom);
        } catch (Exception ex) {
            client.sendData(new PacketSendAccountLoginError(AccountErrorType.INTERNAL_ERROR, "", ""));
            throw new RuntimeException(ex);
        }
    }
}