package com.yukiclient.modules;

/**
 * Toggle for the local YukiClient badge.
 *
 * <p>When enabled, the local player sees a small YukiClient logo on their own
 * nametag and in the player-list tab menu. This is purely local visual flair
 * and does not require a server-side plugin.</p>
 */
public class ClientBadgeModule extends Module {

    public ClientBadgeModule() {
        super("Client Badges", "Shows the YukiClient logo on your own nametag and tab entry.", Category.HUD);
        this.width = 120;
        this.height = 12;
    }

    @Override
    public void render() {
        // Rendering is handled by ClientBadgeRenderer on the Forge event bus.
    }
}
