package com.yukiclient.badge;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Server-side presence relay for YukiClient.
 *
 * <p>Runs when the YukiClient mod is installed on a dedicated or integrated
 * server. It tracks which players are using YukiClient and tells all connected
 * YukiClient clients so each can display badges for the others.</p>
 */
public final class ClientBadgeServerHandler {

    private static final byte PACKET_ANNOUNCE = 0x01;
    private static final byte PACKET_ADD = 0x02;
    private static final byte PACKET_REMOVE = 0x03;
    private static final byte PACKET_LIST = 0x04;

    private final FMLEventChannel eventChannel;
    private final Set<UUID> yukiUsers = Collections.synchronizedSet(new HashSet<UUID>());

    public ClientBadgeServerHandler(FMLEventChannel eventChannel) {
        this.eventChannel = eventChannel;
        this.eventChannel.register(this);
    }

    /**
     * Registers logout listener on the Forge event bus.
     */
    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Handles incoming announce packets from clients.
     */
    @SubscribeEvent
    public void onPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
        if (!BadgeChannel.CHANNEL.equals(event.packet.channel())) {
            return;
        }

        if (!(event.handler instanceof net.minecraft.network.NetHandlerPlayServer)) {
            return;
        }

        EntityPlayerMP player = ((net.minecraft.network.NetHandlerPlayServer) event.handler).playerEntity;
        ByteBuf buf = event.packet.payload();
        if (buf == null || !buf.isReadable()) {
            return;
        }

        byte type = buf.readByte();
        if (type != PACKET_ANNOUNCE) {
            return;
        }

        UUID uuid;
        try {
            uuid = new UUID(buf.readLong(), buf.readLong());
        } catch (Exception e) {
            return;
        }

        if (!player.getUniqueID().equals(uuid)) {
            // Do not trust a UUID that does not match the sender.
            return;
        }

        if (yukiUsers.add(uuid)) {
            // Send the full list to the newcomer.
            sendToPlayer(player, buildListPacket());

            // Tell everyone else that a new YukiClient user joined.
            broadcastExcept(buildSinglePacket(PACKET_ADD, uuid), uuid);
        }
    }

    /**
     * Removes a player from the tracked set when they disconnect.
     */
    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.player.getUniqueID();
        if (yukiUsers.remove(uuid)) {
            broadcast(buildSinglePacket(PACKET_REMOVE, uuid));
        }
    }

    private void broadcast(byte[] message) {
        for (EntityPlayerMP online : net.minecraftforge.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerList()) {
            sendToPlayer(online, message);
        }
    }

    private void broadcastExcept(byte[] message, UUID except) {
        for (EntityPlayerMP online : net.minecraftforge.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerList()) {
            if (!online.getUniqueID().equals(except)) {
                sendToPlayer(online, message);
            }
        }
    }

    private void sendToPlayer(EntityPlayerMP player, byte[] message) {
        // FMLEventChannel cannot send from server directly using its bus, so we
        // send the vanilla custom payload packet directly to the player.
        ByteBuf buf = Unpooled.wrappedBuffer(message);
        player.playerNetServerHandler.sendPacket(new S3FPacketCustomPayload(BadgeChannel.CHANNEL, new PacketBuffer(buf)));
    }

    private byte[] buildSinglePacket(byte type, UUID uuid) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(17);
        DataOutputStream out = new DataOutputStream(baos);
        try {
            out.writeByte(type);
            out.writeLong(uuid.getMostSignificantBits());
            out.writeLong(uuid.getLeastSignificantBits());
        } catch (IOException e) {
            // ByteArrayOutputStream never throws.
        }
        return baos.toByteArray();
    }

    private byte[] buildListPacket() {
        UUID[] snapshot;
        synchronized (yukiUsers) {
            snapshot = yukiUsers.toArray(new UUID[yukiUsers.size()]);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream(5 + snapshot.length * 16);
        DataOutputStream out = new DataOutputStream(baos);
        try {
            out.writeByte(PACKET_LIST);
            out.writeInt(snapshot.length);
            for (UUID uuid : snapshot) {
                out.writeLong(uuid.getMostSignificantBits());
                out.writeLong(uuid.getLeastSignificantBits());
            }
        } catch (IOException e) {
            // ByteArrayOutputStream never throws.
        }
        return baos.toByteArray();
    }
}
