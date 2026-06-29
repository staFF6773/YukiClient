package com.yukiclient.badge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public final class ClientBadgeRenderer {

    private static final ResourceLocation BADGE_TEXTURE =
            new ResourceLocation("yukiclient", "yuki-badge-16.png");

    private static final int NAMETAG_ICON_SIZE = 8;
    private static final int NAMETAG_ICON_GAP = 2;

    public ClientBadgeRenderer() {
    }

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRenderSpecialsPre(RenderLivingEvent.Specials.Pre event) {
        if (!(event.entity instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.entity;
        Minecraft mc = Minecraft.getMinecraft();
        if (player != mc.thePlayer) {
            return;
        }

        if (mc.getRenderManager() == null || mc.fontRendererObj == null) {
            return;
        }

        event.setCanceled(true);
        renderNameplateWithBadge(player, event.x, event.y, event.z);
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

}
