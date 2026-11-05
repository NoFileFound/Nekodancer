package org.nekodancer.command.commands.debug;

// Imports
import java.util.List;
import org.nekodancer.GamePlayer;
import org.nekodancer.GameServer;
import org.nekodancer.command.Command;
import org.nekodancer.command.CommandHandler;
import org.nekodancer.network.NetByteArray;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

@Command(name = "test", description = "test.", permission = Command.CommandPermission.DEBUG_ONLY)
@SuppressWarnings("unused")
public class Test implements CommandHandler {
    @Override
    public void execute(GamePlayer player, GameServer server, List<String> args) {
        CommandHandler.sendServerMessage(player, "OK");
    }
}