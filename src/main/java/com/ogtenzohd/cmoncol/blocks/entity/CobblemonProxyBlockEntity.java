package com.ogtenzohd.cmoncol.blocks.entity;

import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.List;

public abstract class CobblemonProxyBlockEntity extends TileEntityColonyBuilding {

    public static final String STORED_POKEMON_TAG = "StoredPokemon";
    public static final String SNAPSHOT_POKEMON_TAG = "SnapshotPokemon";
    public static final String OWNER_UUID_TAG = "OwnerUUID";
    public static final String OWNER_NAME_TAG = "OwnerName";

    protected CompoundTag storedPokemonData = null;
    protected CompoundTag snapshotPokemonData = null; 
    protected UUID ownerUUID = null;
    protected String ownerName = "Unknown";
    
    protected UUID spawnedEntityUUID = null;
    private boolean firstTick = true; 

    public CobblemonProxyBlockEntity(BlockEntityType<? extends AbstractTileEntityColonyBuilding> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public boolean hasPokemon() { return storedPokemonData != null; }
    public UUID getOwnerUUID() { return ownerUUID; }
    public String getOwnerName() { return ownerName; }
    public CompoundTag getStoredPokemonData() { return storedPokemonData; }
    public UUID getSpawnedEntityUUID() { return spawnedEntityUUID; }

    @Override
    public void tick() {
        super.tick();
        if (this.level instanceof net.minecraft.server.level.ServerLevel serverLevel && hasPokemon()) {
            if (firstTick) {
                firstTick = false;
                AABB scanArea = new AABB(this.worldPosition).inflate(15);
                List<PokemonEntity> existingDummies = serverLevel.getEntitiesOfClass(PokemonEntity.class, scanArea, 
                    e -> e.getTags().contains("cmoncol_dummy") && e.getCustomName() != null && e.getCustomName().getString().startsWith(this.ownerName));

                if (!existingDummies.isEmpty()) {
                    this.spawnedEntityUUID = existingDummies.get(0).getUUID();
                } else {
                    spawnPokemonEntity();
                }
            }

            if (this.spawnedEntityUUID != null) {
                Entity entity = serverLevel.getEntity(this.spawnedEntityUUID);
                if (entity != null) {
                    checkBounds(entity, serverLevel);
                }
            }
        }
    }

    private void checkBounds(Entity entity, net.minecraft.server.level.ServerLevel serverLevel) {
        BlockPos entPos = entity.blockPosition();
        boolean outOfBounds = false;
        Map<String, Set<BlockPos>> tagMap = this.getWorldTagNamePosMap();
        String prefix = getSchematicPrefix();
        BlockPos center = getGardenCenter();

        if (tagMap != null) {
            if (tagMap.containsKey(prefix + "_no_access") && tagMap.get(prefix + "_no_access").contains(entPos)) outOfBounds = true;
            if (tagMap.containsKey(prefix + "_point_a") && tagMap.containsKey(prefix + "_point_b")) {
                BlockPos pointA = tagMap.get(prefix + "_point_a").iterator().next();
                BlockPos pointB = tagMap.get(prefix + "_point_b").iterator().next();
                if (entPos.getX() < Math.min(pointA.getX(), pointB.getX()) || entPos.getX() > Math.max(pointA.getX(), pointB.getX()) ||
                    entPos.getZ() < Math.min(pointA.getZ(), pointB.getZ()) || entPos.getZ() > Math.max(pointA.getZ(), pointB.getZ())) {
                    outOfBounds = true;
                }
            }
        } else if (entity.distanceToSqr(center.getX(), center.getY(), center.getZ()) > 200) {
            outOfBounds = true;
        }

        if (outOfBounds) {
            entity.teleportTo(center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5);
            entity.setDeltaMovement(0, 0, 0);
        }
    }

    private String getSchematicPrefix() {
        Map<String, Set<BlockPos>> tagMap = this.getWorldTagNamePosMap();
        if (tagMap != null) {
            if (tagMap.containsKey("pasture_point_a")) return "pasture";
            if (tagMap.containsKey("trainer_acadamy_point_a")) return "colony_traineracadamy";
        }
        return "pasture";
    }

    private BlockPos getGardenCenter() {
        Map<String, Set<BlockPos>> tagMap = this.getWorldTagNamePosMap();
        String prefix = getSchematicPrefix();
        if (tagMap != null && tagMap.containsKey(prefix + "_point_a") && tagMap.containsKey(prefix + "_point_b")) {
            BlockPos a = tagMap.get(prefix + "_point_a").iterator().next();
            BlockPos b = tagMap.get(prefix + "_point_b").iterator().next();
            return new BlockPos((a.getX() + b.getX()) / 2, Math.min(a.getY(), b.getY()), (a.getZ() + b.getZ()) / 2);
        }
        return this.getBlockPos();
    }

    private void spawnPokemonEntity() {
        if (this.level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            CompoundTag dummyData = this.storedPokemonData.copy();
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
                    pokeEntity.setInvulnerable(true); 
                    pokeEntity.setPersistenceRequired();
                    pokeEntity.setCustomName(Component.literal(this.ownerName + "'s " + mon.getDisplayName(true).getString()));
                    pokeEntity.setCustomNameVisible(true);
                    serverLevel.addFreshEntity(pokeEntity);
                    this.spawnedEntityUUID = pokeEntity.getUUID();
                    this.setChanged();
                }
            }
        }
    }

    private void removePokemonEntity() {
        if (this.level instanceof net.minecraft.server.level.ServerLevel serverLevel && this.spawnedEntityUUID != null) {
            Entity entity = serverLevel.getEntity(this.spawnedEntityUUID);
            if (entity != null) entity.discard();
            this.spawnedEntityUUID = null;
            this.setChanged();
        }
    }

    public void setProxyData(UUID playerUUID, String playerName, CompoundTag pokemonNBT) {
        removePokemonEntity(); 
        this.ownerUUID = playerUUID;
        this.ownerName = playerName;
        this.storedPokemonData = pokemonNBT;
        this.snapshotPokemonData = pokemonNBT.copy(); 
        spawnPokemonEntity();
        this.setChanged();
        if (level != null && !level.isClientSide) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    public void clearProxyData() {
        removePokemonEntity();
        this.ownerUUID = null;
        this.ownerName = "Unknown";
        this.storedPokemonData = null;
        this.snapshotPokemonData = null; 
        this.setChanged();
        if (level != null && !level.isClientSide) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    public Pokemon extractLivePokemon(net.minecraft.server.level.ServerLevel serverLevel) {
        if (!hasPokemon()) return null;
        
        if (this.spawnedEntityUUID != null) {
            Entity e = serverLevel.getEntity(this.spawnedEntityUUID);
            if (e instanceof PokemonEntity pokeEntity) {
                pokeEntity.discard();
            }
        }
        
        Pokemon mon = new Pokemon();
        mon.loadFromNBT(serverLevel.registryAccess(), this.storedPokemonData);
        mon.setUuid(UUID.randomUUID()); 
        
        return mon;
    }

    public void emergencyRecoverPokemon() {
        if (this.level instanceof net.minecraft.server.level.ServerLevel serverLevel && this.snapshotPokemonData != null) {
            net.minecraft.server.level.ServerPlayer sPlayer = serverLevel.getServer().getPlayerList().getPlayer(this.ownerUUID);
            if (sPlayer != null) {
                Pokemon mon = new Pokemon();
                mon.loadFromNBT(serverLevel.registryAccess(), this.snapshotPokemonData);
                mon.setUuid(UUID.randomUUID()); 
                com.cobblemon.mod.common.api.storage.party.PlayerPartyStore party = com.cobblemon.mod.common.Cobblemon.INSTANCE.getStorage().getParty(sPlayer);
                com.cobblemon.mod.common.api.storage.pc.PCStore pc = com.cobblemon.mod.common.Cobblemon.INSTANCE.getStorage().getPC(sPlayer);
                if (party != null && !party.add(mon)) if (pc != null) pc.add(mon);
            }
            this.clearProxyData(); 
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (storedPokemonData != null) tag.put(STORED_POKEMON_TAG, storedPokemonData);
        if (snapshotPokemonData != null) tag.put(SNAPSHOT_POKEMON_TAG, snapshotPokemonData);
        if (ownerUUID != null) tag.putUUID(OWNER_UUID_TAG, ownerUUID);
        tag.putString(OWNER_NAME_TAG, ownerName);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.storedPokemonData = tag.contains(STORED_POKEMON_TAG) ? tag.getCompound(STORED_POKEMON_TAG) : null;
        this.ownerUUID = tag.hasUUID(OWNER_UUID_TAG) ? tag.getUUID(OWNER_UUID_TAG) : null;
        this.ownerName = tag.getString(OWNER_NAME_TAG);
        if (tag.contains(SNAPSHOT_POKEMON_TAG)) this.snapshotPokemonData = tag.getCompound(SNAPSHOT_POKEMON_TAG);
        else if (this.storedPokemonData != null) this.snapshotPokemonData = this.storedPokemonData.copy();
    }
}