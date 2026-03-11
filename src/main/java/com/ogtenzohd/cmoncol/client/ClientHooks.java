package com.ogtenzohd.cmoncol.client;

import com.ogtenzohd.cmoncol.colony.buildings.gui.GymWindow;
import net.minecraft.core.BlockPos;

/**
 * Client-side helper methods that can be called from common code via
 * dist-safe guards. Keeping these references in a separate class prevents
 * the server classloader from ever resolving the client-only types.
 */
public final class ClientHooks {

    private ClientHooks() {}

    public static void openGymWindow(BlockPos gymPos) {
        net.minecraft.client.Minecraft.getInstance().setScreen(
            new com.ldtteam.blockui.BOScreen(new GymWindow(gymPos))
        );
    }
}
