package org.nekodancer.command.commands.debug;

// Imports
import java.util.List;
import org.nekodancer.GamePlayer;
import org.nekodancer.GameServer;
import org.nekodancer.command.Command;
import org.nekodancer.command.CommandHandler;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.packets.send.neko.PacketSendNekoPlayerInitialize;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

@Command(name = "test", description = "test.", permission = Command.CommandPermission.DEBUG_ONLY)
@SuppressWarnings("unused")
public class Test implements CommandHandler {
    @Override
    public void execute(GamePlayer player, GameServer server, List<String> args) {

    }
}



/*
SEND_NEKO_PLAY_PERSONAL_MUSIC_PLAYLIST:

hasCustomImage.
if (hasCustomImage)
    unk
    song title
    song url ("x_mondes/song_icon.jpg")
    is the next music to play
else
    unk
    song title.
 */