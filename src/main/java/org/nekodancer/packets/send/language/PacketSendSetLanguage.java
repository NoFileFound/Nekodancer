package org.nekodancer.packets.send.language;

// Imports
import org.nekodancer.data.LanguageInfo;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.packets.PacketOpcodes;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

public final class PacketSendSetLanguage extends PSSendPacket {
    private final String language;
    private final String ipCountryCode;
    private final String ipCountryLanguageCode;

    /**
     * Creates a new packet of SEND_SET_LANGUAGE.
     * @param language The language code.
     */
    public PacketSendSetLanguage(String language, String ipCountryCode, String ipCountryLanguageCode) {
        super(PacketOpcodes.SEND_SET_LANGUAGE);
        this.language = language;
        this.ipCountryCode = ipCountryCode;
        this.ipCountryLanguageCode = ipCountryLanguageCode;
    }


    @Override
    public PSByteArray build() throws PSSendPacketException {
        LanguageInfo language = LanguageInfo.fromCode(this.language);
        NetByteArray packet = new NetByteArray();
        packet.writeUTF(language.getCode().toUpperCase(), true);
        packet.writeUTF((this.language.equals(this.ipCountryLanguageCode) ? this.ipCountryCode : language.getCountryCode()), true);
        packet.writeBoolean(language.isRightToLeft());
        packet.writeBoolean(language.hasSpecialLayout());
        packet.writeUTF(language.getFont(), true);
        return packet;
    }
}