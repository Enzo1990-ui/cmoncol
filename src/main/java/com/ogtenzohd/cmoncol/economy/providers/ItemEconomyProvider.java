package com.ogtenzohd.cmoncol.economy.providers;

import com.ogtenzohd.cmoncol.config.CCConfig;
import com.ogtenzohd.cmoncol.economy.IEconomyProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemEconomyProvider implements IEconomyProvider {

    private Item getCurrencyItem() {
        String itemId = CCConfig.INSTANCE.daycareCurrencyItem.get();
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId));
    }

    @Override
    public double getBalance(ServerPlayer player) {
        Item currency = getCurrencyItem();
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(currency)) count += stack.getCount();
        }
        return count;
    }

    @Override
    public boolean withdraw(ServerPlayer player, double amount) {
        int cost = (int) Math.ceil(amount);
        if (getBalance(player) < cost) return false;

        Item currency = getCurrencyItem();
        int remainingToTake = cost;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(currency)) {
                int take = Math.min(stack.getCount(), remainingToTake);
                stack.shrink(take);
                remainingToTake -= take;
                if (remainingToTake <= 0) break;
            }
        }
        return true;
    }

    @Override
    public void deposit(ServerPlayer player, double amount) {
        int toGive = (int) Math.floor(amount);
        Item currency = getCurrencyItem();
        ItemStack reward = new ItemStack(currency, toGive);

        if (!player.getInventory().add(reward)) {
            player.drop(reward, false);
        }
    }

    @Override
    public Component formatCurrency(double amount) {
        String itemName = getCurrencyItem().getDescription().getString();
        return Component.literal((int)amount + " " + itemName);
    }
}