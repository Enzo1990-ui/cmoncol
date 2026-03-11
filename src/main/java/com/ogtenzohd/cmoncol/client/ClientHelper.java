package com.ogtenzohd.cmoncol.client;

import net.minecraft.core.BlockPos;
import net.minecraft.client.Minecraft;
import com.ldtteam.blockui.BOScreen;
import com.ogtenzohd.cmoncol.colony.buildings.gui.GymWindow;

public class ClientHelper {
    public static void openGymScreen(BlockPos gymPos) {
        Minecraft.getInstance().setScreen(new BOScreen(new GymWindow(gymPos)));
    }
}