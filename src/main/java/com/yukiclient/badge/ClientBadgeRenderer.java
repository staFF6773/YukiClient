package com.yukiclient.badge;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.yukiclient.modules.ClientBadgeModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Renders the YukiClient logo on the local player's tab entry and nametag.
 */
public final class ClientBadgeRenderer {

    private static final ResourceLocation BADGE_TEXTURE =
            new ResourceLocation("yukiclient", "yuki-badge-16.png");

    private static final int TAB_ICON_SIZE = 8;
    private static final int NAMETAG_ICON_SIZE = 8;
    private static final int NAMETAG_ICON_GAP = 2;

    private static final Ordering<NetworkPlayerInfo> PLAYER_ORDER = Ordering.from(new PlayerComparator());

    private final ClientBadgeModule module;

    public ClientBadgeRenderer(ClientBadgeModule module) {
        this.module = module;
    }

    /**
     * Registers this renderer on the Forge event bus.
     */
    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Renders a YukiClient badge beside the player's nameplate.
     *
     * <p>We cancel the vanilla nameplate and redraw it ourselves so the badge is
     * visible at any distance in third person.</p>
     */
    @SubscribeEvent
    public void onRenderSpecialsPre(RenderLivingEvent.Specials.Pre event) {
        if (!module.isEnabled() || !(event.entity instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.entity;
        if (!ClientBadgeManager.isUser(player.getUniqueID())) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getRenderManager() == null || mc.fontRendererObj == null) {
            return;
        }

        event.setCanceled(true);
        renderNameplateWithBadge(player, event.x, event.y, event.z);
    }

    /**
     * Renders the YukiClient icon in the tab list.
     *
     * <p>The icon is drawn in the head slot, before the player's name, so it sits
     * inside the tab row without adding extra blank space.</p>
     */
    @SubscribeEvent
    public void onRenderPlayerListOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.PLAYER_LIST || !module.isEnabled()) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.thePlayer.sendQueue == null) {
            return;
        }

        List<NetworkPlayerInfo> players = PLAYER_ORDER.sortedCopy(mc.thePlayer.sendQueue.getPlayerInfoMap());
        if (players.isEmpty()) {
            return;
        }

        int scaledWidth = event.resolution.getScaledWidth();

        int maxNameWidth = 0;
        int maxScoreWidth = 0;
        for (NetworkPlayerInfo info : players) {
            maxNameWidth = Math.max(maxNameWidth, mc.fontRendererObj.getStringWidth(getPlayerName(info)));

            ScoreObjective objective = mc.thePlayer.getWorldScoreboard().getObjectiveInDisplaySlot(0);
            if (objective != null && objective.getRenderType() != net.minecraft.scoreboard.IScoreObjectiveCriteria.EnumRenderType.HEARTS) {
                int scoreWidth = mc.fontRendererObj.getStringWidth(" " + objective.getScoreboard().getValueFromObjective(info.getGameProfile().getName(), objective).getScorePoints());
                maxScoreWidth = Math.max(maxScoreWidth, scoreWidth);
            }
        }

        players = players.subList(0, Math.min(players.size(), 80));
        int playerCount = players.size();
        int rows = playerCount;
        int columns = 1;
        while (rows > 20) {
            columns++;
            rows = (playerCount + columns - 1) / columns;
        }

        boolean showHeads = mc.isIntegratedServerRunning() || mc.getNetHandler().getNetworkManager().getIsencrypted();
        int scoreWidth;
        ScoreObjective objective = mc.thePlayer.getWorldScoreboard().getObjectiveInDisplaySlot(0);
        if (objective != null) {
            scoreWidth = objective.getRenderType() == net.minecraft.scoreboard.IScoreObjectiveCriteria.EnumRenderType.HEARTS
                    ? 90 : maxScoreWidth;
        } else {
            scoreWidth = 0;
        }

        int columnWidth = Math.min(columns * ((showHeads ? 9 : 0) + maxNameWidth + scoreWidth + 13), scaledWidth - 50) / columns;
        int startX = scaledWidth / 2 - (columnWidth * columns + (columns - 1) * 5) / 2;
        int startY = 10 + getHeaderOffset(mc, scaledWidth);

        for (int i = 0; i < playerCount; i++) {
            NetworkPlayerInfo info = players.get(i);
            if (!ClientBadgeManager.isUser(info.getGameProfile().getId())) {
                continue;
            }

            int column = i / rows;
            int row = i % rows;
            int rowX = startX + column * columnWidth + column * 5;
            int rowY = startY + row * 9;

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            mc.getTextureManager().bindTexture(BADGE_TEXTURE);

            Gui.drawModalRectWithCustomSizedTexture(
                    rowX, rowY, 0.0F, 0.0F,
                    TAB_ICON_SIZE, TAB_ICON_SIZE,
                    TAB_ICON_SIZE, TAB_ICON_SIZE);

            GlStateManager.disableBlend();
        }
    }

    private static void renderNameplateWithBadge(EntityPlayer player, double x, double y, double z) {
        Minecraft mc = Minecraft.getMinecraft();
        String name = player.getDisplayName().getFormattedText();
        int nameWidth = mc.fontRendererObj.getStringWidth(name);

        double labelY = y + player.height + 0.5D
                - (player.isChild() ? player.height / 2.0D : 0.0D);

        int combinedWidth = nameWidth + NAMETAG_ICON_SIZE + NAMETAG_ICON_GAP;
        int halfCombined = combinedWidth / 2;
        int iconX = -halfCombined;
        int nameX = iconX + NAMETAG_ICON_SIZE + NAMETAG_ICON_GAP;

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.0F, (float) labelY, (float) z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        float scale = -0.026666668F;
        GlStateManager.scale(scale, scale, scale);

        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos((double) (-halfCombined - 1), -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        worldrenderer.pos((double) (-halfCombined - 1), 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        worldrenderer.pos((double) (halfCombined + 1), 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        worldrenderer.pos((double) (halfCombined + 1), -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);

        mc.getTextureManager().bindTexture(BADGE_TEXTURE);
        Gui.drawModalRectWithCustomSizedTexture(
                iconX, 0, 0.0F, 0.0F,
                NAMETAG_ICON_SIZE, NAMETAG_ICON_SIZE,
                NAMETAG_ICON_SIZE, NAMETAG_ICON_SIZE);

        mc.fontRendererObj.drawString(name, nameX, 0, 553648127);

        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    private static int getHeaderOffset(Minecraft mc, int scaledWidth) {
        try {
            GuiIngame ingameGUI = mc.ingameGUI;
            if (ingameGUI == null) {
                return 0;
            }

            Field overlayField = GuiIngame.class.getDeclaredField("overlayPlayerList");
            overlayField.setAccessible(true);
            GuiPlayerTabOverlay overlay = (GuiPlayerTabOverlay) overlayField.get(ingameGUI);

            Field headerField = GuiPlayerTabOverlay.class.getDeclaredField("header");
            headerField.setAccessible(true);
            IChatComponent header = (IChatComponent) headerField.get(overlay);

            if (header != null) {
                List<String> lines = mc.fontRendererObj.listFormattedStringToWidth(header.getFormattedText(), scaledWidth - 50);
                return lines.size() * mc.fontRendererObj.FONT_HEIGHT + 1;
            }
        } catch (Exception e) {
            // Ignore reflection failures; fallback to no header offset.
        }
        return 0;
    }

    private static String getPlayerName(NetworkPlayerInfo info) {
        String name = info.getDisplayName() != null
                ? info.getDisplayName().getFormattedText()
                : ScorePlayerTeam.formatPlayerName(info.getPlayerTeam(), info.getGameProfile().getName());

        if (info.getGameType() == WorldSettings.GameType.SPECTATOR) {
            name = EnumChatFormatting.ITALIC + name;
        }

        return name;
    }

    private static final class PlayerComparator implements java.util.Comparator<NetworkPlayerInfo> {
        @Override
        public int compare(NetworkPlayerInfo a, NetworkPlayerInfo b) {
            ScorePlayerTeam teamA = a.getPlayerTeam();
            ScorePlayerTeam teamB = b.getPlayerTeam();
            return ComparisonChain.start()
                    .compareTrueFirst(a.getGameType() != WorldSettings.GameType.SPECTATOR, b.getGameType() != WorldSettings.GameType.SPECTATOR)
                    .compareTrueFirst(teamA != null, teamB != null)
                    .compare(teamA != null ? teamA.getRegisteredName() : "", teamB != null ? teamB.getRegisteredName() : "")
                    .compare(a.getGameProfile().getName(), b.getGameProfile().getName())
                    .result();
        }
    }
}
