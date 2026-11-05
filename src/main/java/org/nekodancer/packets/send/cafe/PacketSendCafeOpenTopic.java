package org.nekodancer.packets.send.cafe;

// Imports
import org.nekodancer.database.data.CafeEntry;
import org.nekodancer.network.NetByteArray;
import org.nekodancer.packets.PacketOpcodes;
import org.nekodancer.utils.Utils;
import java.util.List;
import org.psframework.network.PSSendPacket;
import org.psframework.network.PSSendPacketException;
import org.psframework.utils.PSByteArray;

public final class PacketSendCafeOpenTopic extends PSSendPacket {
    private final long topicId;
    private final List<CafeEntry.CafePost> cafePosts;
    private final long playerId;
    private final boolean canReply;

    /**
     * Creates a new packet of SEND_CAFE_OPEN_TOPIC.
     * @param topicId The cafe's topic id.
     * @param posts The cafe's posts.
     * @param playerId The player's id.
     * @param canReply Can the player reply in the topic.
     */
    public PacketSendCafeOpenTopic(long topicId, List<CafeEntry.CafePost> posts, long playerId, boolean canReply) {
        super(PacketOpcodes.SEND_CAFE_OPEN_TOPIC);
        this.topicId = topicId;
        this.cafePosts = posts;
        this.playerId = playerId;
        this.canReply = canReply;
    }


    @Override
    public PSByteArray build() throws PSSendPacketException {
        NetByteArray packet = new NetByteArray();
        packet.writeBoolean(true);
        packet.writeInt(this.topicId);
        packet.writeBoolean(false);
        packet.writeBoolean(this.canReply);
        for(CafeEntry.CafePost post : this.cafePosts) {
            packet.writeInt(post.getId());
            packet.writeInt(post.getAuthorId());
            packet.writeInt(Utils.getUnixTime() - post.getDate());
            packet.writeUTF(post.getAuthor(), true);
            packet.writeUTF(post.getMessage(), true);
            packet.writeBoolean(this.playerId != -1 && !post.getVotes().contains(this.playerId));
            packet.writeShort(post.points.shortValue());
            packet.writeUTF("", true);
            packet.writeByte(0);
        }

        return packet;
    }
}