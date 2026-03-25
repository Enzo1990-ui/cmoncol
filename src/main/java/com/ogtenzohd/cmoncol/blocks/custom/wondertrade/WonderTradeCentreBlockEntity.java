package com.ogtenzohd.cmoncol.blocks.custom.wondertrade;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.ogtenzohd.cmoncol.config.CCConfig;
import com.ogtenzohd.cmoncol.registration.CmoncolReg;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class WonderTradeCentreBlockEntity extends TileEntityColonyBuilding {

    private CompoundTag depositedPokemonNBT = null;
    private CompoundTag readyPokemonNBT = null;
    private int tradeTimer = 0;
    private static final int TRADE_TIME_TICKS = 1200;
    private java.util.UUID depositorUUID = null;
    private boolean isBoosted = false;

    public WonderTradeCentreBlockEntity(BlockPos pos, BlockState state) {
        super(CmoncolReg.WONDER_TRADE_CENTRE_BE.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level != null) {
            if (tradeTimer > 0) {
                tradeTimer--;
                
                if (tradeTimer <= 0 && !this.level.isClientSide) {
                    processTrade();
                }
            }
        }
    }

    public void setBoosted(boolean boosted) { 
        this.isBoosted = boosted; 
        this.setChanged(); 
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public boolean isBoosted() { return this.isBoosted; }

    public void startTrade(CompoundTag pokemonNbt, UUID playerUUID, boolean boosted) {
        this.depositedPokemonNBT = pokemonNbt;
        this.depositorUUID = playerUUID;
        this.isBoosted = boosted;
        this.readyPokemonNBT = null;
        this.tradeTimer = boosted ? TRADE_TIME_TICKS / 2 : TRADE_TIME_TICKS;
        this.setChanged();
        
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    private void processTrade() {
        if (this.level == null || this.level.isClientSide) return;

        double luckFactor = this.isBoosted ? 3.0 : 1.0;
        if (this.depositorUUID != null && com.ogtenzohd.cmoncol.util.CmoncolPerks.hasVIPPerks(this.depositorUUID)) {
            luckFactor += 0.5;
        }

        double legWeight = CCConfig.INSTANCE.legendaryWeight.get() * luckFactor;
        double shinyWeight = CCConfig.INSTANCE.shinyWeight.get() * luckFactor;
        double rareWeight = CCConfig.INSTANCE.rareWeight.get() * luckFactor;

        double totalSpecialWeight = legWeight + shinyWeight + rareWeight;

        if (totalSpecialWeight > 1.0) {
            legWeight = legWeight / totalSpecialWeight;
            shinyWeight = shinyWeight / totalSpecialWeight;
            rareWeight = rareWeight / totalSpecialWeight;
        }

        double roll = this.level.random.nextDouble();

        String speciesName = "random";
        String extraProperties = "";
        int minLvl = 5, maxLvl = 25;

        java.util.List<com.cobblemon.mod.common.pokemon.Species> allImplemented =
                new java.util.ArrayList<>(PokemonSpecies.getImplemented());

        if (roll < legWeight) {
            java.util.List<com.cobblemon.mod.common.pokemon.Species> legendaries = new java.util.ArrayList<>();
            for (com.cobblemon.mod.common.pokemon.Species s : allImplemented) {
                if (s.getLabels().contains("legendary") || s.getLabels().contains("mythical")) {
                    legendaries.add(s);
                }
            }
            if (!legendaries.isEmpty()) {
                speciesName = legendaries.get(this.level.random.nextInt(legendaries.size())).getName();
            }

            extraProperties = " aspects=colour:yellow";
            minLvl = 70; maxLvl = 100;
        }
        else if (roll < (legWeight + shinyWeight)) {
            extraProperties = " shiny=yes aspects=colour:purple";
            minLvl = 30; maxLvl = 50;
        }
        else if (roll < (legWeight + shinyWeight + rareWeight)) {
            java.util.List<com.cobblemon.mod.common.pokemon.Species> ultraBeasts = new java.util.ArrayList<>();
            for (com.cobblemon.mod.common.pokemon.Species s : allImplemented) {
                if (s.getLabels().contains("ultra_beast")) {
                    ultraBeasts.add(s);
                }
            }
            if (!ultraBeasts.isEmpty()) {
                speciesName = ultraBeasts.get(this.level.random.nextInt(ultraBeasts.size())).getName();
            }

            extraProperties = " aspects=colour:blue";
            minLvl = 40; maxLvl = 60;
        }

        String pokemonSpec = "species=" + speciesName + extraProperties;

        if (net.neoforged.fml.ModList.get().isLoaded("rctmod") && this.depositorUUID != null) {
            net.minecraft.world.entity.player.Player player = this.level.getPlayerByUUID(this.depositorUUID);
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                int playerCap = com.ogtenzohd.cmoncol.compat.RCTCompat.getPlayerLevelCap(serverPlayer);
                if (maxLvl > playerCap) maxLvl = playerCap;
                if (minLvl > playerCap) minLvl = playerCap;
            }
        }

        try {
            Pokemon randomMon = PokemonProperties.Companion.parse(pokemonSpec).create();
            int finalLevel = minLvl + this.level.random.nextInt(maxLvl - minLvl + 1);
            randomMon.setLevel(finalLevel);
            this.readyPokemonNBT = randomMon.saveToNBT(this.level.registryAccess(), new CompoundTag());
        } catch (Exception e) {
            Pokemon fallback = PokemonProperties.Companion.parse("species=magikarp level=5").create();
            this.readyPokemonNBT = fallback.saveToNBT(this.level.registryAccess(), new CompoundTag());
            com.ogtenzohd.cmoncol.CobblemonColonies.LOGGER.error("WonderTrade failed to parse string: {}", pokemonSpec, e);
        }

        this.depositedPokemonNBT = null;
        this.tradeTimer = 0;
        this.setChanged();
        this.level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    public CompoundTag getReadyPokemon() { return this.readyPokemonNBT; }
    
    public java.util.UUID getDepositorUUID() { return this.depositorUUID; }

    public void claimPokemon() {
        this.readyPokemonNBT = null;
        this.isBoosted = false;
        this.setChanged();
        
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }
    
    public int getTradeTimer() { return this.tradeTimer; }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.saveAdditional(tag, provider);
        if (depositedPokemonNBT != null) tag.put("DepositedPokemon", depositedPokemonNBT);
        if (readyPokemonNBT != null) tag.put("ReadyPokemon", readyPokemonNBT);
        if (depositorUUID != null) tag.putUUID("DepositorUUID", depositorUUID);
        tag.putInt("TradeTimer", tradeTimer);
        tag.putBoolean("IsBoosted", isBoosted);
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("DepositedPokemon")) {
            depositedPokemonNBT = tag.getCompound("DepositedPokemon");
        } else {
            depositedPokemonNBT = null; 
        }
        
        if (tag.contains("DepositorUUID")) depositorUUID = tag.getUUID("DepositorUUID");

        if (tag.contains("ReadyPokemon")) {
            readyPokemonNBT = tag.getCompound("ReadyPokemon");
        } else {
            readyPokemonNBT = null;
        }
        
        tradeTimer = tag.getInt("TradeTimer");
        isBoosted = tag.getBoolean("IsBoosted");
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.@NotNull Provider provider) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, provider);
        return tag;
    }
}