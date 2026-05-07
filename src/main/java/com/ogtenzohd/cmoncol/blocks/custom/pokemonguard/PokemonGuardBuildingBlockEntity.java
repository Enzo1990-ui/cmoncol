package com.ogtenzohd.cmoncol.blocks.custom.pokemonguard;

import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class PokemonGuardBuildingBlockEntity extends TileEntityColonyBuilding {

    private final java.util.Map<Integer, String> guardAssignments = new java.util.HashMap<>();

    public final java.util.List<Integer> syncedGuardIds = new java.util.ArrayList<>();
    public final java.util.Map<Integer, String> syncedGuardNames = new java.util.HashMap<>();

    public PokemonGuardBuildingBlockEntity(BlockPos pos, BlockState state) {
        super(com.ogtenzohd.cmoncol.registration.CmoncolReg.POKEMON_GUARD_BE.get(), pos, state);
    }

    public String getAssignedPartner(int citizenId) {
        return this.guardAssignments.getOrDefault(citizenId, "growlithe"); // Default
    }

    public void setAssignedPartner(int citizenId, String partner) {
        this.guardAssignments.put(citizenId, partner);
        this.setChanged();
    }

    @Override
    public void saveAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        net.minecraft.nbt.CompoundTag assignmentsTag = new net.minecraft.nbt.CompoundTag();
        for (java.util.Map.Entry<Integer, String> entry : this.guardAssignments.entrySet()) {
            assignmentsTag.putString(String.valueOf(entry.getKey()), entry.getValue());
        }
        tag.put("GuardAssignments", assignmentsTag);

        if (this.level != null && !this.level.isClientSide() && this.getBuilding() != null) {
            java.util.List<com.minecolonies.api.colony.ICitizenData> guards = new java.util.ArrayList<>(this.getBuilding().getAllAssignedCitizen());
            guards.sort(java.util.Comparator.comparingInt(com.minecolonies.api.colony.ICitizenData::getId));

            int[] idsArray = new int[guards.size()];
            net.minecraft.nbt.CompoundTag namesTag = new net.minecraft.nbt.CompoundTag();

            for (int i = 0; i < guards.size(); i++) {
                com.minecolonies.api.colony.ICitizenData cit = guards.get(i);
                int id = cit.getId();
                idsArray[i] = id;
                namesTag.putString(String.valueOf(id), cit.getName());
            }
            tag.putIntArray("SyncedGuardIds", idsArray);
            tag.put("SyncedGuardNames", namesTag);
        }
    }

    @Override
    public void loadAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.guardAssignments.clear();
        if (tag.contains("GuardAssignments")) {
            net.minecraft.nbt.CompoundTag assignmentsTag = tag.getCompound("GuardAssignments");
            for (String key : assignmentsTag.getAllKeys()) {
                try {
                    this.guardAssignments.put(Integer.parseInt(key), assignmentsTag.getString(key));
                } catch (NumberFormatException ignored) {}
            }
        }

        if (tag.contains("SyncedGuardIds")) {
            this.syncedGuardIds.clear();
            for (int id : tag.getIntArray("SyncedGuardIds")) {
                this.syncedGuardIds.add(id);
            }
        }
        if (tag.contains("SyncedGuardNames")) {
            this.syncedGuardNames.clear();
            net.minecraft.nbt.CompoundTag namesTag = tag.getCompound("SyncedGuardNames");
            for (String key : namesTag.getAllKeys()) {
                try {
                    this.syncedGuardNames.put(Integer.parseInt(key), namesTag.getString(key));
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider provider) {
        net.minecraft.nbt.CompoundTag tag = super.getUpdateTag(provider);
        saveAdditional(tag, provider);
        return tag;
    }
}