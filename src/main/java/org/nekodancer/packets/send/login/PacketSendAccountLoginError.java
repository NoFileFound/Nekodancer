package org.nekodancer.packets.send.login;

// Imports
import org.nekodancer.data.objects.AccountErrorType;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

public final class PacketSendAccountLoginError extends PSSendPacket {
    private final AccountErrorType errorType;
    private final String suggestedNames;
    private final String emailAddress2FA;

    /**
     * Creates a new packet of SEND_ACCOUNT_LOGIN_ERROR.
     * @param errorType The account login error type.
     * @param suggestedNames The account's suggested names (If email address was found.
     * @param emailAddress2FA The account's email address (FOR 2FA verification).
     */
    public PacketSendAccountLoginError(AccountErrorType errorType, String suggestedNames, String emailAddress2FA) {
        super(PacketOpcodes.SEND_ACCOUNT_LOGIN_ERROR);
        this.errorType = errorType;
        this.suggestedNames = suggestedNames;
        this.emailAddress2FA = emailAddress2FA;
    }


    @Override
    public PSByteArray build() throws PSSendPacketException {
        NetByteArray packet = new NetByteArray();
        packet.writeByte(this.errorType.getId());
        packet.writeUTF(this.suggestedNames, true);
        packet.writeUTF(this.emailAddress2FA, true);
        return packet;
    }
}