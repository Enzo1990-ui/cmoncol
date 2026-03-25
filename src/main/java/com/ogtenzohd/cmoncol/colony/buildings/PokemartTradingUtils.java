package com.ogtenzohd.cmoncol.colony.buildings;

import com.ogtenzohd.cmoncol.config.CCConfig;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class PokemartTradingUtils {

    public static MerchantOffers getTradesForLevel(int buildingLevel) {
        MerchantOffers offers = new MerchantOffers();

        for (String tradeString : CCConfig.INSTANCE.pokemartTrades.get()) {
            try {
                String[] parts = tradeString.split(",");

                if (parts.length != 5) continue;

                int requiredLevel = Integer.parseInt(parts[0]);

                if (buildingLevel < requiredLevel) continue;
                String action = parts[1].toLowerCase();
                String itemId = parts[2];
                int currencyAmount = Integer.parseInt(parts[3]);
                int itemAmount = Integer.parseInt(parts[4]);
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId));

                if (action.equals("buy")) {
                    buyTrade(offers, item, currencyAmount, itemAmount);
                } else if (action.equals("sell")) {
                    sellTrade(offers, item, itemAmount, currencyAmount);
                }

            } catch (Exception e) {
                com.ogtenzohd.cmoncol.CobblemonColonies.LOGGER.error("Failed to parse Pokemart Trade: {}", tradeString);
            }
        }

        return offers;
    }

    private static Item getCurrencyItem() {
        String itemId = CCConfig.INSTANCE.daycareCurrencyItem.get();
        Item currency = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId));
        if (currency == Items.AIR) {
            return Items.EMERALD;
        }
        return currency;
    }

    @SuppressWarnings("SameParameterValue")
    private static void buyTrade(MerchantOffers offers, Item item, int currencyCost, int count) {
        if (item != Items.AIR) {
            offers.add(new MerchantOffer(
                    new ItemCost(getCurrencyItem(), currencyCost),
                    new ItemStack(item, count),
                    999,
                    2,
                    0.0f
            ));
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void sellTrade(MerchantOffers offers, Item item, int countToSell, int currencyReward) {
        if (item != Items.AIR) {
            offers.add(new MerchantOffer(
                    new ItemCost(item, countToSell),
                    new ItemStack(getCurrencyItem(), currencyReward),
                    999,
                    2,
                    0.0f
            ));
        }
    }
}