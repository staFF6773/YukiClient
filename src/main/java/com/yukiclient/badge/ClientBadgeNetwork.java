package com.yukiclient.badge;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Client-side networking for YukiClient presence.
 *
 * <p>When the server also runs the YukiClient mod, this sends an announce
 * packet on join and receives updates about other YukiClient players.</p>
 *
 * <p>Protocol:</p>
 * <ul>
 *   <li>Client -&gt; Server 0x01: announce the local player is using YukiClient.</li>
 *   <li>Server -&gt; Client 0x02: a player has joined/is using YukiClient.</li>
 *   <li>Server -&gt; Client 0x03: a player has left/stopped using YukiClient.</li>
 *   <li>Server -&gt; Client 0x04: full YukiClient user set.</li>
 * </ul>
 */
public final class ClientBadgeNetwork {

    private static final byte PACKET_ANNOUNCE = 0x01;
    private static final byte PACKET_ADD = 0x02;
    private static final byte PACKET_REMOVE = 0x03;
    private static final byte PACKET_LIST = 0x04;

    private final FMLEventChannel eventChannel;

    public ClientBadgeNetwork(FMLEventChannel eventChannel) {
        this.eventChannel = eventChannel;
        this.eventChannel.register(this);
    }

    /**
     * Registers lifecycle listeners on the Forge event bus.
     */
    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Clears the tracked user set when disconnecting.
     */
    @SubscribeEvent
    public void onDisconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        ClientBadgeManager.clear();
    }

    /**
     * Announces this client as a YukiClient user and always marks self locally.
     */
    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.entity instanceof EntityPlayer) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null && event.entity == mc.thePlayer) {
                sendAnnounce();
                ClientBadgeManager.addUser(mc.thePlayer.getUniqueID());
            }
        }
    }

    /**
     * Reads incoming plugin messages from the server.
     */
    @SubscribeEvent
    public void onPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
        if (!BadgeChannel.CHANNEL.equals(event.packet.channel())) {
            return;
        }

        ByteBuf buf = event.packet.payload();
        if (buf == null || !buf.isReadable()) {
            return;
        }

        byte type = buf.readByte();
        switch (type) {
            case PACKET_ADD:
                ClientBadgeManager.addUser(readUuid(buf));
                break;
            case PACKET_REMOVE:
                ClientBadgeManager.removeUser(readUuid(buf));
                break;
            case PACKET_LIST:
                ClientBadgeManager.setUsers(readUuidSet(buf));
                break;
            default:
                // Unknown packet type; ignore.
                break;
        }
    }

    /**
     * Announces this client as a YukiClient user to the current server.
     */
    private void sendAnnounce() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.thePlayer.sendQueue == null) {
            return;
        }

        UUID uuid = mc.thePlayer.getUniqueID();
        if (uuid == null) {
            return;
        }

        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(PACKET_ANNOUNCE);
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
        mc.thePlayer.sendQueue.addToSendQueue(new C17PacketCustomPayload(BadgeChannel.CHANNEL, new PacketBuffer(buf)));
    }

    private static UUID readUuid(ByteBuf buf) {
        return new UUID(buf.readLong(), buf.readLong());
    }

    private static Set<UUID> readUuidSet(ByteBuf buf) {
        int count = buf.readInt();
        Set<UUID> set = new HashSet<UUID>(Math.max(16, count));
        for (int i = 0; i < count; i++) {
            set.add(readUuid(buf));
        }
        return set;
    }
}
