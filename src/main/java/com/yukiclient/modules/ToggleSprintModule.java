package com.yukiclient.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Toggle Sprint Module.
 *
 * While enabled, the player automatically sprints whenever moving forward, so
 * sprint no longer needs to be held. Sprint is suppressed while sneaking, when
 * walking into a wall, or when hunger is too low to sprint in vanilla.
 */
public class ToggleSprintModule extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();

    public ToggleSprintModule() {
        super("ToggleSprint", "Automatically sprints while you move forward.");
        // Off by default so it never conflicts with the hold-to-sprint module.
        this.enabled = false;
        this.x = 10;
        this.y = 134;
        this.width = 60;
        this.height = 12;
    }

    @Override
    public void render() {
        // ToggleSprint has no HUD visuals.
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!isEnabled() || mc.thePlayer == null || mc.currentScreen != null) return;

        EntityPlayerSP player = mc.thePlayer;
        boolean movingForward = player.movementInput != null && player.movementInput.moveForward > 0.0F;
        boolean hasStamina = player.getFoodStats().getFoodLevel() > 6;

        if (movingForward && hasStamina && !player.isSneaking() && !player.isCollidedHorizontally) {
            player.setSprinting(true);
        }
    }
}
