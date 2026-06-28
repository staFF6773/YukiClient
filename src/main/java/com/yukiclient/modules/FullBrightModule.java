package com.yukiclient.modules;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * FullBright Module.
 *
 * Maximizes the client's gamma setting while enabled, brightening the world
 * as if the player has night vision. The original brightness is restored when
 * the module is disabled.
 */
public class FullBrightModule extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();

    private float originalGamma = -1.0F;
    private static final float FULL_BRIGHT_GAMMA = 1000.0F;

    public FullBrightModule() {
        super("FullBright", "Brightens the world like night vision.");
        this.x = 10;
        this.y = 118;
        this.width = 60;
        this.height = 12;
    }

    @Override
    public void render() {
        // FullBright has no HUD visuals.
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        if (isEnabled()) {
            // Capture the user's original gamma once, then force it to maximum.
            if (originalGamma < 0.0F) {
                originalGamma = mc.gameSettings.gammaSetting;
            }
            if (mc.gameSettings.gammaSetting != FULL_BRIGHT_GAMMA) {
                mc.gameSettings.gammaSetting = FULL_BRIGHT_GAMMA;
            }
        } else if (originalGamma >= 0.0F) {
            // Restore the original gamma when the module is turned off.
            mc.gameSettings.gammaSetting = originalGamma;
            originalGamma = -1.0F;
        }
    }
}
