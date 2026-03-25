package com.ogtenzohd.cmoncol.economy.providers;

import com.ogtenzohd.cmoncol.economy.IEconomyProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Method;
import java.math.BigInteger;

public class CobbleDollarsProvider implements IEconomyProvider {

    private Method getCobbleDollarsMethod;
    private Method setCobbleDollarsMethod;

    @SuppressWarnings("JavaReflectionMemberAccess")
    public CobbleDollarsProvider() {
        try {

            this.getCobbleDollarsMethod = Player.class.getMethod("cobbleDollars$getCobbleDollars");
            this.setCobbleDollarsMethod = Player.class.getMethod("cobbleDollars$setCobbleDollars", BigInteger.class);

            com.ogtenzohd.cmoncol.CobblemonColonies.LOGGER.info("[CmoncolEconomy] Successfully hooked into the CobbleDollars Player Mixin! (Native Economy Active)");
        } catch (Throwable e) {
            com.ogtenzohd.cmoncol.CobblemonColonies.LOGGER.error("[CmoncolEconomy] Could not find the CobbleDollars Mixin methods. Balance will be 0.");
        }
    }

    @Override
    public double getBalance(ServerPlayer player) {
        if (this.getCobbleDollarsMethod == null) return 0.0;
        try {
            BigInteger balance = (BigInteger) this.getCobbleDollarsMethod.invoke(player);
            return balance != null ? balance.doubleValue() : 0.0;
        } catch (Throwable e) {
            return 0.0;
        }
    }

    @Override
    public boolean withdraw(ServerPlayer player, double amount) {
        if (this.getCobbleDollarsMethod == null || this.setCobbleDollarsMethod == null) return false;
        try {
            BigInteger currentBalance = (BigInteger) this.getCobbleDollarsMethod.invoke(player);
            if (currentBalance == null) currentBalance = BigInteger.ZERO;

            BigInteger cost = BigInteger.valueOf((long) amount);

            if (currentBalance.compareTo(cost) >= 0) {
                BigInteger newBalance = currentBalance.subtract(cost);
                this.setCobbleDollarsMethod.invoke(player, newBalance);
                return true;
            }
            return false;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public void deposit(ServerPlayer player, double amount) {
        if (this.getCobbleDollarsMethod == null || this.setCobbleDollarsMethod == null) return;
        try {
            BigInteger currentBalance = (BigInteger) this.getCobbleDollarsMethod.invoke(player);
            if (currentBalance == null) currentBalance = BigInteger.ZERO;

            BigInteger gain = BigInteger.valueOf((long) amount);
            BigInteger newBalance = currentBalance.add(gain);

            this.setCobbleDollarsMethod.invoke(player, newBalance);
        } catch (Throwable e) {
            com.ogtenzohd.cmoncol.CobblemonColonies.LOGGER.error("[CmoncolEconomy] Failed to deposit native CobbleDollars", e);
        }
    }

    @Override
    public Component formatCurrency(double amount) {
        return Component.literal((long)amount + " C");
    }
}