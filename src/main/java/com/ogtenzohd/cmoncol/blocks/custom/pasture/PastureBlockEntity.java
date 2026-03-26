package com.ogtenzohd.cmoncol.blocks.custom.pasture;

import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.ogtenzohd.cmoncol.registration.CmoncolReg;
import com.ogtenzohd.cmoncol.util.RancherRecipeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PastureBlockEntity extends TileEntityColonyBuilding {

    public static class PastureSlot {
        public UUID ownerUUID;
        public String ownerName;
        public CompoundTag pokemonNBT;
        public CompoundTag snapshotNBT;
        public UUID spawnedEntityUUID = null;
        public int selectedRecipe = 0;

        public PastureSlot(UUID ownerUUID, String ownerName, CompoundTag pokemonNBT) {
            this.ownerUUID = ownerUUID;
            this.ownerName = ownerName;
            this.pokemonNBT = pokemonNBT;
            this.snapshotNBT = pokemonNBT.copy();
        }
    }

    private final List<PastureSlot> storedPokemon = new ArrayList<>();
    private int startupDelay = 60;
    private int clientLevel = 1;

    public PastureBlockEntity(BlockPos pos, BlockState state) {
        super(CmoncolReg.PASTURE_BE.get(), pos, state);
    }

    private String getBuildingTag() {
        return "cmoncol_origin_" + this.worldPosition.getX() + "_" + this.worldPosition.getY() + "_" + this.worldPosition.getZ();
    }

    public void toggleRecipe(int slotIndex) {
        if (this.level == null) return;
        if (slotIndex >= 0 && slotIndex < storedPokemon.size()) {
            PastureSlot slot = storedPokemon.get(slotIndex);
            Pokemon p = new Pokemon();
            p.loadFromNBT(level.registryAccess(), slot.pokemonNBT);
            String species = p.getSpecies().getName().toLowerCase();

            List<RancherRecipeManager.RancherRecipe> recipes = RancherRecipeManager.getRecipesFor(species);
            if (recipes.size() > 1) {
                slot.selectedRecipe = (slot.selectedRecipe + 1) % recipes.size();
                this.setChanged();
                if (level != null) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            if (serverLevel.getGameTime() % 100 == 0) {
                serverLevel.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }

            if (startupDelay > 0) {
                startupDelay--;
                if (startupDelay == 0) {
                    BlockPos center = getGardenCenter();
                    AABB scanArea = new AABB(center).inflate(48);
                    String myTag = getBuildingTag();

                    List<PokemonEntity> ghosts = serverLevel.getEntitiesOfClass(PokemonEntity.class, scanArea,
                            e -> e.getTags().contains("cmoncol_dummy"));

                    for (PokemonEntity ghost : ghosts) {
                        boolean belongsToNeighbor = false;
                        for (String tag : ghost.getTags()) {
                            if (tag.startsWith("cmoncol_origin_") && !tag.equals(myTag)) {
                                belongsToNeighbor = true;
                                break;
                            }
                        }

                        if (!belongsToNeighbor) {
                            ghost.discard();
                        }
                    }

                    for (PastureSlot pastureSlot : storedPokemon) {
                        spawnPokemonEntity(pastureSlot);
                    }
                }
                return;
            }

            if (serverLevel.getGameTime() % 20 == 0) {
                BlockPos center = getGardenCenter();
                Map<String, Set<BlockPos>> tagMap = this.getWorldTagNamePosMap();

                for (PastureSlot slot : storedPokemon) {
                    if (slot.spawnedEntityUUID != null) {
                        Entity e = serverLevel.getEntity(slot.spawnedEntityUUID);
                        if (e != null) checkBounds(e, center, tagMap);
                    }
                }
            }
        }
    }

    @Override
    public void setRemoved() {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            for (PastureSlot slot : storedPokemon) {
                if (slot.spawnedEntityUUID != null) {
                    Entity e = serverLevel.getEntity(slot.spawnedEntityUUID);
                    if (e instanceof PokemonEntity pokeEntity) {
                        slot.pokemonNBT = pokeEntity.getPokemon().saveToNBT(serverLevel.registryAccess(), new CompoundTag());
                        slot.snapshotNBT = slot.pokemonNBT.copy();
                        pokeEntity.discard();
                    }
                    slot.spawnedEntityUUID = null;
                }
            }
        }
        super.setRemoved();
    }

    public int getMaxSlots() {
        int bLevel = 1;
        if (this.level != null && this.level.isClientSide) {
            bLevel = this.clientLevel;
        } else {
            IBuilding building = getBuilding();
            if (building instanceof AbstractBuilding abstractBuilding) {
                bLevel = abstractBuilding.getBuildingLevel();
            }
        }
        return Math.max(1, bLevel * 2);
    }

    public List<PastureSlot> getStoredPokemon() { return storedPokemon; }

    private void checkBounds(Entity entity, BlockPos center, Map<String, Set<BlockPos>> tagMap) {
        BlockPos entPos = entity.blockPosition();
        boolean outOfBounds = false;
        if (tagMap != null) {
            if (tagMap.containsKey("pasture_no_access") && tagMap.get("pasture_no_access").contains(entPos)) outOfBounds = true;
            if (tagMap.containsKey("pasture_point_a") && tagMap.containsKey("pasture_point_b")) {
                BlockPos a = tagMap.get("pasture_point_a").iterator().next();
                BlockPos b = tagMap.get("pasture_point_b").iterator().next();
                if (entPos.getX() < Math.min(a.getX(), b.getX()) || entPos.getX() > Math.max(a.getX(), b.getX()) ||
                        entPos.getZ() < Math.min(a.getZ(), b.getZ()) || entPos.getZ() > Math.max(a.getZ(), b.getZ())) outOfBounds = true;
            }
        } else if (entity.distanceToSqr(center.getX(), center.getY(), center.getZ()) > 200) outOfBounds = true;

        if (outOfBounds) {
            entity.teleportTo(center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5);
            entity.setDeltaMovement(0, 0, 0);
        }
    }

    private BlockPos getGardenCenter() {
        Map<String, Set<BlockPos>> tagMap = this.getWorldTagNamePosMap();
        if (tagMap != null && tagMap.containsKey("pasture_point_a") && tagMap.containsKey("pasture_point_b")) {
            BlockPos a = tagMap.get("pasture_point_a").iterator().next();
            BlockPos b = tagMap.get("pasture_point_b").iterator().next();
            return new BlockPos((a.getX() + b.getX()) / 2, Math.min(a.getY(), b.getY()), (a.getZ() + b.getZ()) / 2);
        }
        return this.getBlockPos();
    }

    private void spawnPokemonEntity(PastureSlot slot) {
        if (this.level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            CompoundTag dummyData = slot.pokemonNBT.copy();
            dummyData.remove("owner");
            Pokemon mon = new Pokemon();
            mon.loadFromNBT(serverLevel.registryAccess(), dummyData);
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("cobblemon:pokemon"));
            Entity entity = type.create(serverLevel);
            if (entity instanceof PokemonEntity pokeEntity) {
                pokeEntity.setPokemon(mon);
                BlockPos center = getGardenCenter();
                pokeEntity.setPos(center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5);
                pokeEntity.restrictTo(center, 12);

                pokeEntity.getPokemon().setUuid(UUID.randomUUID());
                pokeEntity.getTags().add("cmoncol_dummy");
                pokeEntity.getTags().add(getBuildingTag());
                pokeEntity.setInvulnerable(true);
                pokeEntity.setPersistenceRequired();

                pokeEntity.setCustomName(Component.literal(slot.ownerName + "'s " + pokeEntity.getPokemon().getDisplayName(true).getString()));
                pokeEntity.setCustomNameVisible(true);

                serverLevel.addFreshEntity(pokeEntity);
                slot.spawnedEntityUUID = pokeEntity.getUUID();
                this.setChanged();
            }
        }
    }

    public void addPokemon(UUID owner, String ownerName, CompoundTag nbt) {
        if (canAcceptMore(owner)) {
            PastureSlot newSlot = new PastureSlot(owner, ownerName, nbt);
            storedPokemon.add(newSlot);
            spawnPokemonEntity(newSlot);

            this.setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    }

    public void removePokemon(int index) {
        if (index >= 0 && index < storedPokemon.size()) {
            PastureSlot slot = storedPokemon.get(index);
            if (this.level instanceof net.minecraft.server.level.ServerLevel serverLevel && slot.spawnedEntityUUID != null) {
                Entity e = serverLevel.getEntity(slot.spawnedEntityUUID);
                if (e != null) e.discard();
            }
            storedPokemon.remove(index);
            this.setChanged();
            if (level != null && !level.isClientSide) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void emergencyRecoverAllPokemon() {
        if (this.level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            for (PastureSlot slot : this.storedPokemon) {
                net.minecraft.server.level.ServerPlayer sPlayer = serverLevel.getServer().getPlayerList().getPlayer(slot.ownerUUID);
                if (sPlayer != null) {
                    Pokemon mon = new Pokemon();
                    mon.loadFromNBT(serverLevel.registryAccess(), slot.snapshotNBT);
                    mon.setUuid(UUID.randomUUID());
                    com.cobblemon.mod.common.api.storage.party.PlayerPartyStore party = com.cobblemon.mod.common.Cobblemon.INSTANCE.getStorage().getParty(sPlayer);
                    com.cobblemon.mod.common.api.storage.pc.PCStore pc = com.cobblemon.mod.common.Cobblemon.INSTANCE.getStorage().getPC(sPlayer);
                    if (!party.add(mon)) {
                        pc.add(mon);
                    }
                }
            }
            this.storedPokemon.clear();
            this.setChanged();
        }
    }

    public boolean canAcceptMore(UUID playerUUID) {
        long playerPokemonCount = storedPokemon.stream()
                .filter(slot -> slot.ownerUUID.equals(playerUUID))
                .count();
        return playerPokemonCount < getMaxSlots();
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        if (tag.contains("PastureSlots")) {
            ListTag slotsTag = tag.getList("PastureSlots", Tag.TAG_COMPOUND);
            for (int i = 0; i < slotsTag.size(); i++) {
                CompoundTag slotData = slotsTag.getCompound(i);
                slotData.remove("SnapshotNBT");
            }
        }
        return tag;
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.saveAdditional(tag, provider);
        ListTag slotsTag = new ListTag();
        for (PastureSlot slot : storedPokemon) {
            CompoundTag slotData = new CompoundTag();
            slotData.putUUID("Owner", slot.ownerUUID);
            slotData.putString("OwnerName", slot.ownerName);
            slotData.put("PokemonNBT", slot.pokemonNBT);
            slotData.put("SnapshotNBT", slot.snapshotNBT);
            slotData.putInt("Recipe", slot.selectedRecipe);
            slotsTag.add(slotData);
        }
        tag.put("PastureSlots", slotsTag);

        if (this.level != null && !this.level.isClientSide) {
            IBuilding building = getBuilding();
            if (building instanceof AbstractBuilding abstractBuilding) {
                tag.putInt("ClientLevel", abstractBuilding.getBuildingLevel());
            }
        } else {
            tag.putInt("ClientLevel", this.clientLevel);
        }
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.loadAdditional(tag, provider);
        storedPokemon.clear();
        if (tag.contains("PastureSlots")) {
            ListTag slotsTag = tag.getList("PastureSlots", Tag.TAG_COMPOUND);
            for (int i = 0; i < slotsTag.size(); i++) {
                CompoundTag slotData = slotsTag.getCompound(i);
                PastureSlot slot = new PastureSlot(slotData.getUUID("Owner"), slotData.getString("OwnerName"), slotData.getCompound("PokemonNBT"));
                if (slotData.contains("SnapshotNBT")) slot.snapshotNBT = slotData.getCompound("SnapshotNBT");
                else slot.snapshotNBT = slot.pokemonNBT.copy();
                if (slotData.contains("Recipe")) slot.selectedRecipe = slotData.getInt("Recipe");
                storedPokemon.add(slot);
            }
        }

        if (tag.contains("ClientLevel")) {
            this.clientLevel = tag.getInt("ClientLevel");
        }
    }
}