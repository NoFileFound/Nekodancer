package org.nekodancer.packets.send.neko;

// Imports
import org.nekodancer.database.data.PlayerEntity;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.packets.PacketOpcodes;
import org.nekodancer.utils.Utils;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

public class PacketSendNekoPlayerProfile extends PSSendPacket {
    private final PlayerEntity playerAccount;

    /**
     * Creates a new packet of SEND_NEKO_PLAYER_PROFILE.
     * @param playerAccount The player's account.
     */
    public PacketSendNekoPlayerProfile(PlayerEntity playerAccount) {
        super(PacketOpcodes.SEND_NEKO_PLAYER_PROFILE);
        this.playerAccount = playerAccount;
    }


    @Override
    public PSByteArray build() throws PSSendPacketException {
        NetByteArray packet = new NetByteArray();

        packet.writeUTF(this.playerAccount.getUsername(), true);
        packet.writeInt(this.playerAccount.getAccountId());
        packet.writeInt(this.playerAccount.getCreatedDate());
        packet.writeByte(Utils.getProfileColorId(this.playerAccount.getStaffRoles()));
        packet.writeByte(this.playerAccount.playerGender.ordinal());
        packet.writeUTF(this.playerAccount.tribeName, true);
        packet.writeUTF(this.playerAccount.soulmate, true);

        packet.writeByte(3);

        packet.writeByte(1);
        packet.writeInt(10000);

        packet.writeByte(2);
        packet.writeInt(10000);

        packet.writeByte(4);
        packet.writeInt(10000);

        packet.writeByte(this.playerAccount.currentCatFurId);
        packet.writeByte(0);
        packet.writeByte(0);
        return packet;
    }
}