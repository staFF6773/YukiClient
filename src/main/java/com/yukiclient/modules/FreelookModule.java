package com.yukiclient.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MouseHelper;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * Freelook Module - LunarClient Style.
 *
 * Hold Left Alt to freely rotate the camera 360 degrees around the player.
 * The real player orientation stays locked while freelook is active, so
 * movement direction and server packets do not change. The camera uses a
 * separate freelook orientation, and the perspective is forced to third-person.
 * Releasing Alt snaps the camera back.
 */
public class FreelookModule extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();

    private boolean wasAltDown = false;
    private boolean freelookActive = false;

    private float originalYaw;
    private float originalPitch;
    private float originalPrevYaw;
    private float originalPrevPitch;

    private float freelookYaw;
    private float freelookPitch;

    private int originalPerspective;

    private MouseHelper originalMouseHelper;
    private final FreelookMouseHelper freelookMouseHelper = new FreelookMouseHelper();

    // Stored model orientation while overriding player model rendering
    private float storedRenderYawOffset;
    private float storedPrevRenderYawOffset;
    private float storedRotationYawHead;
    private float storedPrevRotationYawHead;
    private float storedRotationPitch;
    private float storedPrevRotationPitch;

    public FreelookModule() {
        super("Freelook", "360-degree camera view while holding Left Alt.");
        // Default size is only relevant for the HUD editor selection box
        this.x = 10;
        this.y = 70;
        this.width = 60;
        this.height = 12;
    }

    @Override
    public void render() {
        // Freelook has no permanent HUD visuals.
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        // If the module is disabled or we are in a GUI/incomplete world, abort.
        if (!isEnabled() || mc.thePlayer == null || mc.currentScreen != null || !mc.inGameHasFocus) {
            if (freelookActive) {
                stopFreelook();
            }
            wasAltDown = false;
            return;
        }

        boolean altDown = Keyboard.isKeyDown(Keyboard.KEY_LMENU);

        if (altDown && !wasAltDown) {
            startFreelook();
        } else if (!altDown && wasAltDown) {
            stopFreelook();
        }

        wasAltDown = altDown;

        if (freelookActive) {
            lockPlayerOrientation();
            forceThirdPerson();
        }
    }

    /**
     * Keeps the actual player orientation locked to the original direction so
     * movement packets do not reveal a changed viewing angle.
     */
    private void lockPlayerOrientation() {
        EntityPlayer player = mc.thePlayer;
        player.rotationYaw = originalYaw;
        player.rotationPitch = originalPitch;
        player.prevRotationYaw = originalYaw;
        player.prevRotationPitch = originalPitch;
    }

    private void forceThirdPerson() {
        mc.gameSettings.thirdPersonView = 1;
    }

    /**
     * Applies the freelook orientation to the rendering camera while leaving
     * the logical player orientation untouched. Updates are performed every
     * render frame for smooth camera motion.
     */
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (!freelookActive || mc.thePlayer == null) return;

        EntityPlayer player = mc.thePlayer;
        if (event.phase == TickEvent.Phase.START) {
            updateFreelookRotation();

            player.rotationYaw = freelookYaw;
            player.rotationPitch = freelookPitch;
            player.prevRotationYaw = freelookYaw;
            player.prevRotationPitch = freelookPitch;
        } else {
            player.rotationYaw = originalYaw;
            player.rotationPitch = originalPitch;
            player.prevRotationYaw = originalYaw;
            player.prevRotationPitch = originalPitch;
        }
    }

    /**
     * Reads raw mouse movement and updates the separate freelook orientation.
     * The real player rotation is never changed here.
     */
    private void updateFreelookRotation() {
        int dx = Mouse.getDX();
        int dy = Mouse.getDY();
        if (dx == 0 && dy == 0) return;

        float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
        float multiplier = f * f * f * 8.0F * 0.15F;

        freelookYaw += dx * multiplier;
        freelookPitch -= dy * multiplier;
        freelookPitch = MathHelper.clamp_float(freelookPitch, -90.0F, 90.0F);
    }

    /**
     * Locks the player model to the original facing direction right before
     * the model is rendered so it does not visibly spin with the camera.
     */
    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (!shouldOverrideModel(event)) return;

        EntityPlayer player = event.entityPlayer;

        storedRenderYawOffset = player.renderYawOffset;
        storedPrevRenderYawOffset = player.prevRenderYawOffset;
        storedRotationYawHead = player.rotationYawHead;
        storedPrevRotationYawHead = player.prevRotationYawHead;
        storedRotationPitch = player.rotationPitch;
        storedPrevRotationPitch = player.prevRotationPitch;

        player.renderYawOffset = originalYaw;
        player.prevRenderYawOffset = originalYaw;
        player.rotationYawHead = originalYaw;
        player.prevRotationYawHead = originalYaw;
        player.rotationPitch = originalPitch;
        player.prevRotationPitch = originalPitch;
    }

    /**
     * Restores the model orientation after rendering.
     */
    @SubscribeEvent
    public void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        if (!shouldOverrideModel(event)) return;

        EntityPlayer player = event.entityPlayer;

        player.renderYawOffset = storedRenderYawOffset;
        player.prevRenderYawOffset = storedPrevRenderYawOffset;
        player.rotationYawHead = storedRotationYawHead;
        player.prevRotationYawHead = storedPrevRotationYawHead;
        player.rotationPitch = storedRotationPitch;
        player.prevRotationPitch = storedPrevRotationPitch;
    }

    private boolean shouldOverrideModel(RenderPlayerEvent event) {
        return isEnabled() && freelookActive && event.entityPlayer == mc.thePlayer;
    }

    /**
     * Enters freelook state and stores the player's current orientation.
     */
    private void startFreelook() {
        freelookActive = true;
        EntityPlayer player = mc.thePlayer;

        originalYaw = player.rotationYaw;
        originalPitch = player.rotationPitch;
        originalPrevYaw = player.prevRotationYaw;
        originalPrevPitch = player.prevRotationPitch;

        freelookYaw = originalYaw;
        freelookPitch = originalPitch;

        originalPerspective = mc.gameSettings.thirdPersonView;
        mc.gameSettings.thirdPersonView = 1;

        originalMouseHelper = mc.mouseHelper;
        mc.mouseHelper = freelookMouseHelper;
    }

    /**
     * Exits freelook state and restores the player's original orientation,
     * perspective and mouse helper.
     */
    private void stopFreelook() {
        freelookActive = false;
        EntityPlayer player = mc.thePlayer;
        if (player != null) {
            player.rotationYaw = originalYaw;
            player.rotationPitch = originalPitch;
            player.prevRotationYaw = originalPrevYaw;
            player.prevRotationPitch = originalPrevPitch;
            player.rotationYawHead = originalYaw;
            player.prevRotationYawHead = originalYaw;
        }

        if (originalMouseHelper != null) {
            mc.mouseHelper = originalMouseHelper;
            originalMouseHelper = null;
        }
        Mouse.getDX();
        Mouse.getDY();

        mc.gameSettings.thirdPersonView = originalPerspective;
    }

    /**
     * Custom MouseHelper that does not apply mouse deltas to the player.
     * The module reads the raw deltas itself, preventing movement from being
     * affected by the freelook camera rotation.
     */
    private static class FreelookMouseHelper extends MouseHelper {
        @Override
        public void mouseXYChange() {
            // No-op by design.
        }
    }
}
