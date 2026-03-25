package com.ogtenzohd.cmoncol.compat;

import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.api.utils.LevelUtils;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import static com.mojang.logging.LogUtils.getLogger;

public class RCTCompat {

    private static final Logger LOGGER = getLogger();

    public static int getPlayerLevelCap(ServerPlayer player) {
        try {
            return LevelUtils.levelCap(player);
        } catch (Exception e) {
            return 100;
        }
    }

    public static void advancePlayerProgress(ServerPlayer player, String gymLeaderId) {
        try {
            PlayerState state = PlayerState.get(player);

            if (state != null) {
                state.addProgressDefeat(gymLeaderId);

                state.addDefeat(gymLeaderId);
            }
        } catch (Exception e) {
            LOGGER.info("[CobblemonColonies] Failed to sync progress with RCTMod for leader: {}", gymLeaderId);
        }
    }
}