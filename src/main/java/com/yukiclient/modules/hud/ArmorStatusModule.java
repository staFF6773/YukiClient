package com.yukiclient.modules.hud;

import com.yukiclient.modules.Module;
import com.yukiclient.theme.YukiTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

/**
 * Armor Status Module - LunarClient Style.
 *
 * Displays equipped armor pieces and their remaining durability inside a
 * dark bordered panel. Durability text turns red when it drops below 25%.
 *
 * <p>Measurements, durability text, and colors are cached and only rebuilt
 * when the armor inventory or damage values change.</p>
 */
public class ArmorStatusModule extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();

    private final int iconSize = 16;
    private final int rowGap = 2;
    private final int pad = 6;

    // Cached render state. Rebuilt only when armor changes.
    private final String[] cachedText = new String[4];
    private final int[] cachedColor = new int[4];
    private final boolean[] cachedVisible = new boolean[4];
    private int cachedPanelWidth;
    private int cachedPanelHeight;

    // Snapshot data used to detect armor/durability changes. -1 stackSize means empty.
    private final int[] lastDamage = new int[4];
    private final int[] lastStackSize = new int[4];

    public ArmorStatusModule() {
        super("ArmorStatus", "Displays equipped armor durability.", Module.Category.HUD);
        // Default position: bottom area of the screen
        this.x = 10;
        this.y = 200;
        this.width = 90;
        this.height = 84;

        // Initialize snapshot to force a cache rebuild on first render.
        for (int i = 0; i < 4; i++) {
            lastStackSize[i] = -2;
        }
    }

    @Override
    public void render() {
        // Safety check to prevent crashes when player is not in a world
        if (mc.thePlayer == null) return;

        ItemStack[] armorInventory = mc.thePlayer.inventory.armorInventory;

        if (armorSnapshotChanged(armorInventory)) {
            rebuildCache(armorInventory);
            updateSnapshot(armorInventory);
        }

        // --- Draw panel background + border ---
        YukiTheme.drawLunarBox(this.x, this.y, cachedPanelWidth, cachedPanelHeight);

        // --- Draw armor items ---
        int iconX = this.x + pad;
        int textX = iconX + iconSize + 4;
        int currentY = this.y + pad;

        // 3 = Helmet, 2 = Chestplate, 1 = Leggings, 0 = Boots
        for (int i = 3; i >= 0; i--) {
            if (!cachedVisible[i]) continue;

            // Render the item icon using Minecraft's standard item renderer
            GlStateManager.pushMatrix();
            mc.getRenderItem().renderItemAndEffectIntoGUI(armorInventory[i], iconX, currentY);
            mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, armorInventory[i], iconX, currentY);
            GlStateManager.popMatrix();

            // Durability text
            mc.fontRendererObj.drawString(cachedText[i], textX, currentY + 4, cachedColor[i], false);

            currentY += iconSize + rowGap;
        }

        // Update module dimensions based on rendered content for the editor
        this.width = cachedPanelWidth;
        this.height = cachedPanelHeight;
    }

    /**
     * Returns true if any armor slot reference or damage value has changed.
     */
    private boolean armorSnapshotChanged(ItemStack[] armorInventory) {
        for (int i = 0; i < 4; i++) {
            ItemStack stack = armorInventory[i];
            if (stack == null) {
                if (lastStackSize[i] != -1) {
                    return true;
                }
            } else {
                if (lastStackSize[i] == -1
                        || stack.getItemDamage() != lastDamage[i]
                        || stack.stackSize != lastStackSize[i]) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Stores a snapshot of the armor inventory for future change detection.
     */
    private void updateSnapshot(ItemStack[] armorInventory) {
        for (int i = 0; i < 4; i++) {
            ItemStack stack = armorInventory[i];
            if (stack == null) {
                lastStackSize[i] = -1;
            } else {
                lastStackSize[i] = stack.stackSize;
                lastDamage[i] = stack.getItemDamage();
            }
        }
    }

    /**
     * Recomputes panel size, durability text, and colors from the current armor.
     */
    private void rebuildCache(ItemStack[] armorInventory) {
        int itemCount = 0;
        int maxTextWidth = 0;

        for (int i = 3; i >= 0; i--) {
            ItemStack stack = armorInventory[i];
            if (stack == null) {
                cachedVisible[i] = false;
                cachedText[i] = "";
                continue;
            }

            cachedVisible[i] = true;
            itemCount++;
            cachedText[i] = getDurabilityText(stack);
            cachedColor[i] = getDurabilityColor(stack);

            int tw = mc.fontRendererObj.getStringWidth(cachedText[i]);
            if (tw > maxTextWidth) {
                maxTextWidth = tw;
            }
        }

        int innerWidth = iconSize + 4 + maxTextWidth;
        cachedPanelWidth = innerWidth + pad * 2;
        int contentHeight = Math.max(iconSize, itemCount * (iconSize + rowGap) - rowGap);
        cachedPanelHeight = contentHeight + pad * 2;
    }

    /**
     * Returns the durability color: white normally, warning red below 25%.
     */
    private int getDurabilityColor(ItemStack stack) {
        if (!stack.isItemStackDamageable()) {
            return YukiTheme.SNOW_WHITE;
        }
        int maxDmg = stack.getMaxDamage();
        int currentDmg = maxDmg - stack.getItemDamage();
        return currentDmg < maxDmg / 4 ? 0xFF5555 : YukiTheme.SNOW_WHITE;
    }

    private String getDurabilityText(ItemStack stack) {
        if (!stack.isItemStackDamageable()) {
            return "";
        }
        int maxDmg = stack.getMaxDamage();
        int currentDmg = maxDmg - stack.getItemDamage();
        return currentDmg + "/" + maxDmg;
    }
}
