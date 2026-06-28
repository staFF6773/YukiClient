package com.yukiclient.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

/**
 * Sprint Module.
 *
 * Hold the left Control key to sprint. Sprinting automatically stops when the
 * key is released. The module respects sneaking and forward movement so it
 * does not fight with vanilla sprint logic.
 */
public class SprintModule extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();

    private boolean wasSprintKeyDown = false;

    public SprintModule() {
        super("Sprint", "Hold Left Control to sprint.");
        this.x = 10;
        this.y = 86;
        this.width = 60;
        this.height = 12;
    }

    @Override
    public void render() {
        // Sprint has no HUD visuals.
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        // Reset state when the module is disabled, the player is not in a world,
        // or a GUI is open.
        if (!isEnabled() || mc.thePlayer == null || mc.currentScreen != null) {
            if (wasSprintKeyDown && mc.thePlayer != null) {
                mc.thePlayer.setSprinting(false);
            }
            wasSprintKeyDown = false;
            return;
        }

        boolean sprintKeyDown = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
        EntityPlayerSP player = mc.thePlayer;

        if (sprintKeyDown) {
            // Only force sprint while moving forward and not sneaking to avoid
            // fighting vanilla sprint checks.
            if (!player.isSneaking() && player.movementInput.moveForward >= 0.8F) {
                player.setSprinting(true);
            }
        } else if (wasSprintKeyDown) {
            // Key was released this tick: stop the sprint we started.
            player.setSprinting(false);
        }

        wasSprintKeyDown = sprintKeyDown;
    }
}
