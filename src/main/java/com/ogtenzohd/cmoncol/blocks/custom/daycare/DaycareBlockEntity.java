package com.ogtenzohd.cmoncol.blocks.custom.daycare;

import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.ogtenzohd.cmoncol.registration.CmoncolReg;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DaycareBlockEntity extends TileEntityColonyBuilding {

    public static class DaycareSlot {
        public UUID ownerUUID;
        public String ownerName;
        public CompoundTag pokemonNBT;
        public CompoundTag snapshotNBT; 
        public int timeInDaycare; 
        public UUID spawnedEntityUUID = null; 
        
        public DaycareSlot(UUID ownerUUID, String ownerName, CompoundTag pokemonNBT) {
            this.ownerUUID = ownerUUID;
            this.ownerName = ownerName;
            this.pokemonNBT = pokemonNBT;
            this.snapshotNBT = pokemonNBT.copy(); 
            this.timeInDaycare = 0;
        }
    }

    private final List<DaycareSlot> storedPokemon = new ArrayList<>();
    private int startupDelay = 60; 

    public DaycareBlockEntity(BlockPos pos, BlockState state) {
        super(CmoncolReg.DAYCARE_BE.get(), pos, state); 
    }

    public int getMaxSlotsPerPlayer() { return 2; }
    public List<DaycareSlot> getStoredPokemon() { return storedPokemon; }

    private String getBuildingTag() {
        return "cmoncol_origin_" + this.worldPosition.getX() + "_" + this.worldPosition.getY() + "_" + this.worldPosition.getZ();
    }

    @Override
    public void setRemoved() {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            for (DaycareSlot slot : storedPokemon) {
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

    @Override
    public void tick() {
        super.tick();
        if (this.level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            if (startupDelay > 0) {
                startupDelay--;
                if (startupDelay == 0) {
                    BlockPos center = getGardenCenter();
                    AABB scanArea = new AABB(center).inflate(32); 
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
                        if (!belongsToNeighbor) ghost.discard();
                    }

                    for (int i = 0; i < storedPokemon.size(); i++) {
                        spawnPokemonEntity(storedPokemon.get(i), i);
                    }
                }
                return;
            }

            if (serverLevel.getGameTime() % 20 == 0) {
                for (DaycareSlot slot : storedPokemon) {
                    slot.timeInDaycare++;
                }
            }

            BlockPos center = getGardenCenter();
            Map<String, Set<BlockPos>> tagMap = this.getWorldTagNamePosMap();
            for (int i = 0; i < storedPokemon.size(); i++) {
                DaycareSlot slot = storedPokemon.get(i);
                if (slot.spawnedEntityUUID != null) {
                    Entity e = serverLevel.getEntity(slot.spawnedEntityUUID);
                    if (e != null) checkBounds(e, center, tagMap);
                }
            }
        }
    }

    private void checkBounds(Entity entity, BlockPos center, Map<String, Set<BlockPos>> tagMap) {
        BlockPos entPos = entity.blockPosition();
        boolean outOfBounds = false;
        if (tagMap != null) {
            if (tagMap.containsKey("daycare_no_access") && tagMap.get("daycare_no_access").contains(entPos)) outOfBounds = true;
            if (tagMap.containsKey("daycare_point_a") && tagMap.containsKey("daycare_point_b")) {
                BlockPos a = tagMap.get("daycare_point_a").iterator().next();
                BlockPos b = tagMap.get("daycare_point_b").iterator().next();
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
        if (tagMap != null && tagMap.containsKey("daycare_point_a") && tagMap.containsKey("daycare_point_b")) {
            BlockPos a = tagMap.get("daycare_point_a").iterator().next();
            BlockPos b = tagMap.get("daycare_point_b").iterator().next();
            return new BlockPos((a.getX() + b.getX()) / 2, Math.min(a.getY(), b.getY()), (a.getZ() + b.getZ()) / 2);
        }
        return this.getBlockPos(); 
    }

    private void spawnPokemonEntity(DaycareSlot slot, int index) {
        if (this.level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            CompoundTag dummyData = slot.pokemonNBT.copy();
            dummyData.remove("owner"); 
            Pokemon mon = new Pokemon();
            mon.loadFromNBT(this.level.registryAccess(), dummyData);
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("cobblemon:pokemon"));
            if (type != null) {
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
                    
                    String pokeName = mon.getDisplayName(true).getString();
                    pokeEntity.setCustomName(Component.literal(slot.ownerName + "'s " + pokeName));
                    pokeEntity.setCustomNameVisible(true);
                    
                    serverLevel.addFreshEntity(pokeEntity);
                    slot.spawnedEntityUUID = pokeEntity.getUUID();
                    this.setChanged();
                }
            }
        }
    }

    public void emergencyRecoverAllPokemon() {
    if (this.level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
        for (DaycareSlot slot : this.storedPokemon) {
            
            if (slot.spawnedEntityUUID != null) {
                Entity e = serverLevel.getEntity(slot.spawnedEntityUUID);
                if (e != null) e.discard();
            }

            net.minecraft.server.level.ServerPlayer sPlayer = serverLevel.getServer().getPlayerList().getPlayer(slot.ownerUUID);
            if (sPlayer != null) {
                Pokemon mon = new Pokemon();
                mon.loadFromNBT(serverLevel.registryAccess(), slot.snapshotNBT);
                mon.setUuid(UUID.randomUUID()); 
                com.cobblemon.mod.common.api.storage.party.PlayerPartyStore party = com.cobblemon.mod.common.Cobblemon.INSTANCE.getStorage().getParty(sPlayer);
                com.cobblemon.mod.common.api.storage.pc.PCStore pc = com.cobblemon.mod.common.Cobblemon.INSTANCE.getStorage().getPC(sPlayer);
                if (party != null && !party.add(mon)) if (pc != null) pc.add(mon);
            }
        }
        this.storedPokemon.clear();
        this.setChanged();
    }
}
	
	// need to remove the 20 duplicated entities
    private void removePokemonEntity(UUID entityUUID) {
        if (this.level instanceof net.minecraft.server.level.ServerLevel serverLevel && entityUUID != null) {
            Entity entity = serverLevel.getEntity(entityUUID);
            if (entity != null) entity.discard();
        }
    }

    public boolean canAcceptMore(UUID playerUUID) {
        long count = storedPokemon.stream().filter(s -> s.ownerUUID.equals(playerUUID)).count();
        return count < getMaxSlotsPerPlayer();
    }

    public void addPokemon(UUID owner, String ownerName, CompoundTag nbt) {
        if (canAcceptMore(owner)) {
            DaycareSlot newSlot = new DaycareSlot(owner, ownerName, nbt);
            storedPokemon.add(newSlot);
            spawnPokemonEntity(newSlot, storedPokemon.size() - 1);
            this.setChanged();
            if (level != null && !level.isClientSide) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void removePokemon(int index) {
        if (index >= 0 && index < storedPokemon.size()) {
            removePokemonEntity(storedPokemon.get(index).spawnedEntityUUID);
            storedPokemon.remove(index);
            this.setChanged();
            if (level != null && !level.isClientSide) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        ListTag slotsTag = new ListTag();
        for (DaycareSlot slot : storedPokemon) {
            CompoundTag slotData = new CompoundTag();
            slotData.putUUID("Owner", slot.ownerUUID);
            slotData.putString("OwnerName", slot.ownerName);
            slotData.put("PokemonNBT", slot.pokemonNBT);
            slotData.put("SnapshotNBT", slot.snapshotNBT); 
            slotData.putInt("TimeInDaycare", slot.timeInDaycare);
            slotsTag.add(slotData);
        }
        tag.put("DaycareSlots", slotsTag);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        storedPokemon.clear();
        if (tag.contains("DaycareSlots")) {
            ListTag slotsTag = tag.getList("DaycareSlots", Tag.TAG_COMPOUND);
            for (int i = 0; i < slotsTag.size(); i++) {
                CompoundTag slotData = slotsTag.getCompound(i);
                DaycareSlot slot = new DaycareSlot(slotData.getUUID("Owner"), slotData.getString("OwnerName"), slotData.getCompound("PokemonNBT"));
                if (slotData.contains("SnapshotNBT")) slot.snapshotNBT = slotData.getCompound("SnapshotNBT");
                else slot.snapshotNBT = slot.pokemonNBT.copy();
                slot.timeInDaycare = slotData.getInt("TimeInDaycare");
                storedPokemon.add(slot);
            }
        }
    }
}