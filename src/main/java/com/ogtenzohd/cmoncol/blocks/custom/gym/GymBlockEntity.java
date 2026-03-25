package com.ogtenzohd.cmoncol.blocks.custom.gym;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.ogtenzohd.cmoncol.CobblemonColonies;
import com.ogtenzohd.cmoncol.config.CCConfig;
import com.ogtenzohd.cmoncol.registration.CmoncolReg;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GymBlockEntity extends TileEntityColonyBuilding {

    private static final int COOLDOWN_VICTORY = 24000;
    private static final int COOLDOWN_FLED = 6000;
    private static final int TIMEOUT_TICKS = 1200;

    private boolean isArenaActive = false;
    private int postBattleTicks = -1;
    private long cooldownEndTime = 0;
    private String lastDefeatedLeader = "";
    private String currentLeader = "";

    private boolean hasChallengerEntered = false;
    private int arenaTimeoutTicks = 0;
    private int syncedBuildingLevel = 1;

    public GymBlockEntity(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        super(CmoncolReg.GYM_BE.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level == null || this.level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) this.level;

        if (isArenaActive) {
            if (serverLevel.getGameTime() % 20 == 0) {
                BlockPos tagA = getTagPos("structure_a");
                BlockPos tagB = getTagPos("structure_b");

                if (tagA != null && tagB != null) {
                    AABB arenaBox = getArenaBox(tagA, tagB);

                    boolean npcExists = false;
                    boolean playerExists = false;

                    for (Entity e : serverLevel.getEntities(null, arenaBox)) {
                        if (BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString().equals("cobblemon:npc")) {
                            npcExists = true;
                        } else if (e instanceof Player) {
                            playerExists = true;
                        }
                    }

                    if (!hasChallengerEntered) {
                        if (playerExists) {
                            hasChallengerEntered = true;
                            if (CCConfig.INSTANCE.debug.isTrue()) {
                                CobblemonColonies.LOGGER.info("Challenger has entered the arena! Match officially begins.");
                            }
                        }else {
                            arenaTimeoutTicks -= 20;
                            if (arenaTimeoutTicks <= 0) {
                                    resetArenaState(serverLevel, "Challenger never entered the arena. Doors closed.", 0);
                            }
                        }
                        return;
                    }

                    if (!npcExists) {
                        isArenaActive = false;
                        postBattleTicks = 100;
                        lastDefeatedLeader = currentLeader;
                        cooldownEndTime = serverLevel.getGameTime() + COOLDOWN_VICTORY;
                        if (CCConfig.INSTANCE.debug.isTrue()) {
                        CobblemonColonies.LOGGER.info("Gym Leader Defeated! Teleporting in 5 seconds...");
                        }
                    } else if (!playerExists) {
                        resetArenaState(serverLevel, "Challenger lost or fled. Arena reset and locked.", COOLDOWN_FLED);
                    }
                }
            }
        }


        if (postBattleTicks > 0) {
            postBattleTicks--;
            if (postBattleTicks == 0) {
                teleportPlayersOut(serverLevel);
                setDoorState(serverLevel, false);
                currentLeader = "";
            }
        }
    }

    public int getSyncedBuildingLevel() {
        if (this.level != null && !this.level.isClientSide) {
            if (this.getBuilding() instanceof com.minecolonies.core.colony.buildings.AbstractBuilding abstractBuilding) {
                return abstractBuilding.getBuildingLevel();
            }
        }
        return this.syncedBuildingLevel;
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider provider) {
        net.minecraft.nbt.CompoundTag tag = super.getUpdateTag(provider);
        saveAdditional(tag, provider);
        return tag;
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    public String checkCanBattle(String requestedLeader) {
        if (this.level == null) return "error";

        if (isArenaActive || postBattleTicks > 0) {
            return "A battle is currently taking place. Please wait.";
        }

        if (this.level.getGameTime() < cooldownEndTime) {
            if (requestedLeader.equalsIgnoreCase(lastDefeatedLeader)) {
                return "The Gym Leader's pokemon need to recover. Come back later.";
            } else {
                return "The Gym Leader isn't available right now. Come back later.";
            }
        }
        return "yes";
    }

    public boolean safeReset() {
        if (this.isArenaActive || this.postBattleTicks > 0) {
            return false;
        }

        this.cooldownEndTime = 0;
        this.lastDefeatedLeader = "";
        this.setChanged();

        if (CCConfig.INSTANCE.debug.isTrue()) {
            CobblemonColonies.LOGGER.info("The PokeCenter successfully refreshed the Gym's daily cooldown!");
        }

        return true;
    }

    private void setDoorState(ServerLevel level, boolean open) {
        BlockPos tagA = getTagPos("door_a");
        BlockPos tagB = getTagPos("door_b");
        if (tagA == null || tagB == null) return;

        BlockState state = open ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()
                : net.minecraft.world.level.block.Blocks.OXIDIZED_COPPER.defaultBlockState();

        int minX = Math.min(tagA.getX(), tagB.getX());
        int minY = Math.min(tagA.getY(), tagB.getY());
        int minZ = Math.min(tagA.getZ(), tagB.getZ());
        int maxX = Math.max(tagA.getX(), tagB.getX());
        int maxY = Math.max(tagA.getY(), tagB.getY());
        int maxZ = Math.max(tagA.getZ(), tagB.getZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    level.setBlock(new BlockPos(x, y, z), state, 3);
                }
            }
        }
    }

    public void loadGymArena(ServerLevel serverLevel, String leaderName) {
        StructureTemplateManager structureManager = serverLevel.getServer().getStructureManager();
        ResourceLocation structureId = ResourceLocation.fromNamespaceAndPath(CobblemonColonies.MODID, "structures/" + leaderName);
        Optional<StructureTemplate> templateOptional = structureManager.get(structureId);

        if (templateOptional.isPresent()) {
            StructureTemplate template = templateOptional.get();

            BlockPos tagA = getTagPos("structure_a");
            BlockPos tagB = getTagPos("structure_b");

            if (tagA == null || tagB == null) return;

            int holeMinX = Math.min(tagA.getX(), tagB.getX());
            int holeMaxX = Math.max(tagA.getX(), tagB.getX());
            int holeMinZ = Math.min(tagA.getZ(), tagB.getZ());
            int holeMaxZ = Math.max(tagA.getZ(), tagB.getZ());
            int holeMinY = Math.min(tagA.getY(), tagB.getY());

            int holeWidth = (holeMaxX - holeMinX) + 1;
            int holeLength = (holeMaxZ - holeMinZ) + 1;

            Direction facing = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
            Rotation baseRot = switch (facing) {
                case NORTH -> Rotation.CLOCKWISE_180;
                case EAST -> Rotation.COUNTERCLOCKWISE_90;
                case WEST -> Rotation.CLOCKWISE_90;
                default -> Rotation.NONE;
            };

            Rotation extraRot = switch (leaderName.toLowerCase()) {
                case "boo", "booa" -> Rotation.CLOCKWISE_180;
                default -> Rotation.COUNTERCLOCKWISE_90;
            };

            Rotation rot = combineRotations(baseRot, extraRot);

            Vec3i nbtSize = template.getSize();
            int rawX = nbtSize.getX();
            int rawZ = nbtSize.getZ();

            int nbtW = (rot == Rotation.NONE || rot == Rotation.CLOCKWISE_180) ? rawX : rawZ;
            int nbtL = (rot == Rotation.NONE || rot == Rotation.CLOCKWISE_180) ? rawZ : rawX;

            int targetMinX = holeMinX + ((holeWidth - nbtW) / 2);
            int targetMinZ = holeMinZ + ((holeLength - nbtL) / 2);

            int yAdjust = switch (leaderName.toLowerCase()) {
                case "brock", "erika", "koga", "sabrina", "blaine" -> -2;
                case "misty", "giovanni" -> -2;
                case "surge" -> -2;
                default -> -2;
            };
            int pasteY = holeMinY + yAdjust;

            BlockPos pastePos = switch (rot) {
                case CLOCKWISE_90 -> new BlockPos(targetMinX + rawZ - 1, pasteY, targetMinZ);
                case CLOCKWISE_180 -> new BlockPos(targetMinX + rawX - 1, pasteY, targetMinZ + rawZ - 1);
                case COUNTERCLOCKWISE_90 -> new BlockPos(targetMinX, pasteY, targetMinZ + rawX - 1);
                default -> new BlockPos(targetMinX, pasteY, targetMinZ);
            };

            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setRotation(rot)
                    .setMirror(Mirror.NONE)
                    .setIgnoreEntities(true);

            template.placeInWorld(serverLevel, pastePos, pastePos, settings, serverLevel.getRandom(), Block.UPDATE_ALL);

            clearGymNPCs(serverLevel);

            double spawnX = holeMinX + (holeWidth / 2.0);
            double spawnZ = holeMinZ + (holeLength / 2.0);
            double spawnY = pasteY + 2.0;
            boolean observerFound = false;

            int searchMaxX = Math.max(tagA.getX(), tagB.getX());
            int searchMaxZ = Math.max(tagA.getZ(), tagB.getZ());
            int searchMaxY = pasteY + template.getSize().getY();

            for (int x = holeMinX; x <= searchMaxX; x++) {
                for (int y = pasteY; y <= searchMaxY; y++) {
                    for (int z = holeMinZ; z <= searchMaxZ; z++) {
                        BlockPos checkPos = new BlockPos(x, y, z);

                        if (serverLevel.getBlockState(checkPos).is(net.minecraft.world.level.block.Blocks.OBSERVER)) {
                            spawnX = checkPos.getX() + 0.5;
                            spawnY = checkPos.getY() + 1.0;
                            spawnZ = checkPos.getZ() + 0.5;
                            observerFound = true;
                            break;
                        }
                    }
                    if (observerFound) break;
                }
                if (observerFound) break;
            }

            if (!observerFound) {
                if (CCConfig.INSTANCE.debug.isTrue()) {
                    CobblemonColonies.LOGGER.warn("No Observer found in " + leaderName + "'s gym! Using fallback coordinates.");
                }
            }
            String[] npcsToSpawn;

            switch (leaderName.toLowerCase()) {
                case "tate_and_liza":
                    npcsToSpawn = new String[]{"tate", "liza"};
                    break;
                case "striaton":
                    String[] brothers = {"cilan", "chili", "cress"};
                    npcsToSpawn = new String[]{brothers[serverLevel.random.nextInt(brothers.length)]};
                    break;
                case "opelucid":
                    String[] dragons = {"drayden", "iris"};
                    npcsToSpawn = new String[]{dragons[serverLevel.random.nextInt(dragons.length)]};
                    break;
                case "stow_on_side":
                    String[] stow = {"bea", "allister"};
                    npcsToSpawn = new String[]{stow[serverLevel.random.nextInt(stow.length)]};
                    break;
                case "circhester":
                    String[] circhester = {"gordie", "melony"};
                    npcsToSpawn = new String[]{circhester[serverLevel.random.nextInt(circhester.length)]};
                    break;
                default:
                    npcsToSpawn = new String[]{leaderName.toLowerCase()};
                    break;
            }

            for (int i = 0; i < npcsToSpawn.length; i++) {
                String npcName = npcsToSpawn[i];
                double finalX = spawnX;
                double finalZ = spawnZ;

                if (npcsToSpawn.length > 1) {
                    finalX += (i == 0) ? -1.0 : 1.0;
                }

                if (npcName.equalsIgnoreCase("supporter")) {
                    spawnVIPSupporter(serverLevel, finalX, spawnY, finalZ);
                } else {
                    String spawnCmd = String.format(java.util.Locale.US, "spawnnpcat %f %f %f cmoncol:%s", finalX, spawnY, finalZ, npcName);
                    serverLevel.getServer().getCommands().performPrefixedCommand(
                            serverLevel.getServer().createCommandSourceStack().withLevel(serverLevel).withSuppressedOutput(), spawnCmd);
                }
            }

            faceNPCsTowardsBlock(serverLevel, tagA, tagB);
            setDoorState(serverLevel, true);
            isArenaActive = true;
            currentLeader = leaderName.toLowerCase();
            hasChallengerEntered = false;
            arenaTimeoutTicks = TIMEOUT_TICKS;
        }
    }

    private void spawnVIPSupporter(ServerLevel serverLevel, double spawnX, double spawnY, double spawnZ) {
        String vipUUIDString = com.ogtenzohd.cmoncol.util.CmoncolPerks.getRandomSupporterUUID();
        java.util.UUID vipUUID = java.util.UUID.fromString(vipUUIDString);
        String vipName = "VIP Supporter";

        try {
            com.mojang.authlib.yggdrasil.ProfileResult result = serverLevel.getServer().getSessionService().fetchProfile(vipUUID, false);
            if (result != null && result.profile().getName() != null) {
                vipName = result.profile().getName();
            }
        } catch (Exception ignored) {}

        final String finalVipName = vipName;

        net.minecraft.nbt.CompoundTag npcTags = new net.minecraft.nbt.CompoundTag();
        npcTags.putString("id", "cobblemon:npc");
        npcTags.putString("NPCClass", "cmoncol:supporter");

        npcTags.putString("ForcedResourceIdentifier", "cobblemon:standard");
        npcTags.putByte("BehavioursAreCustom", (byte) 1);

        net.minecraft.nbt.CompoundTag configTag = new net.minecraft.nbt.CompoundTag();
        configTag.putString("player_texture", finalVipName);
        npcTags.put("Config", configTag);

        net.minecraft.nbt.CompoundTag dataTag = new net.minecraft.nbt.CompoundTag();
        dataTag.putString("player_texture_username", finalVipName);
        npcTags.put("Data", dataTag);

        net.minecraft.nbt.ListTag behavioursList = new net.minecraft.nbt.ListTag();
        behavioursList.add(net.minecraft.nbt.StringTag.valueOf("cobblemon:core"));
        behavioursList.add(net.minecraft.nbt.StringTag.valueOf("cobblemon:player_textured"));
        npcTags.put("Behaviours", behavioursList);

        net.minecraft.nbt.ListTag posList = new net.minecraft.nbt.ListTag();
        posList.add(net.minecraft.nbt.DoubleTag.valueOf(spawnX));
        posList.add(net.minecraft.nbt.DoubleTag.valueOf(spawnY));
        posList.add(net.minecraft.nbt.DoubleTag.valueOf(spawnZ));
        npcTags.put("Pos", posList);

        net.minecraft.world.entity.Entity entity = net.minecraft.world.entity.EntityType.loadEntityRecursive(npcTags, serverLevel, (e) -> {
            e.setCustomName(net.minecraft.network.chat.Component.literal(finalVipName));
            e.setCustomNameVisible(true);
            return e;
        });

        if (entity instanceof com.cobblemon.mod.common.entity.npc.NPCEntity npc) {

            npc.loadTextureFromGameProfileName(finalVipName);
            com.cobblemon.mod.common.api.storage.party.NPCPartyStore newParty = new com.cobblemon.mod.common.api.storage.party.NPCPartyStore(npc);
            int numPokemon = 3 + serverLevel.random.nextInt(4);

            try {
                java.util.List<com.cobblemon.mod.common.pokemon.Species> implemented =
                        new java.util.ArrayList<>(PokemonSpecies.getImplemented());

                for (int i = 0; i < numPokemon; i++) {
                    com.cobblemon.mod.common.pokemon.Species randomSpecies = implemented.get(serverLevel.random.nextInt(implemented.size()));
                    com.cobblemon.mod.common.pokemon.Pokemon mon = randomSpecies.create(50);
                    newParty.add(mon);
                }
            } catch (Exception e) {
                if (CCConfig.INSTANCE.debug.isTrue()) {
                    CobblemonColonies.LOGGER.error("Failed to generate VIP Pokemon", e);
                }
            }

            newParty.initialize();
            npc.setParty(newParty);

            serverLevel.addFreshEntity(npc);
        }
    }

    private void resetArenaState(ServerLevel level, String logMessage, int cooldownPenalty) {
        this.isArenaActive = false;
        this.currentLeader = "";
        this.hasChallengerEntered = false;
        this.cooldownEndTime = level.getGameTime() + cooldownPenalty;

        clearGymNPCs(level);
        setDoorState(level, false);
        if (CCConfig.INSTANCE.debug.isTrue()) {
            CobblemonColonies.LOGGER.info(logMessage);
        }
    }

    private void clearGymNPCs(ServerLevel level) {
        BlockPos tagA = getTagPos("structure_a");
        BlockPos tagB = getTagPos("structure_b");

        if (tagA != null && tagB != null) {
            AABB arenaBox = getArenaBox(tagA, tagB);
            level.getEntities(null, arenaBox).stream()
                    .filter(e -> {
                        String type = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString();
                        return type.equals("cobblemon:npc") || type.equals("minecraft:armor_stand");
                    })
                    .forEach(Entity::discard);
        } else {
            AABB fallbackBox = new AABB(this.getBlockPos()).inflate(15);
            level.getEntities(null, fallbackBox).stream()
                    .filter(e -> {
                        String type = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString();
                        return type.equals("cobblemon:npc") || type.equals("minecraft:armor_stand");
                    })
                    .forEach(Entity::discard);
        }
    }

    private AABB getArenaBox(BlockPos tagA, BlockPos tagB) {
        return new AABB(tagA.getX(), tagA.getY(), tagA.getZ(), tagB.getX() + 1.0, tagB.getY() + 1.0, tagB.getZ() + 1.0).inflate(2);
    }

    private void teleportPlayersOut(ServerLevel level) {
        BlockPos tagA = getTagPos("structure_a");
        BlockPos tagB = getTagPos("structure_b");
        if (tagA == null || tagB == null) return;

        AABB arenaBox = getArenaBox(tagA, tagB);
        BlockPos spawnPos = this.getBlockPos().offset(0, 1, 0);

        for (Player player : level.players()) {
            if (arenaBox.contains(player.position())) {
                player.teleportTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
            }
        }
    }

    private BlockPos getTagPos(String tagName) {
        Map<String, Set<BlockPos>> tagMap = this.getWorldTagNamePosMap();
        if (tagMap != null && tagMap.containsKey(tagName)) {
            return tagMap.get(tagName).iterator().next();
        }
        return null;
    }

    private Rotation combineRotations(Rotation r1, Rotation r2) {
        int v1 = switch(r1) { case CLOCKWISE_90 -> 1; case CLOCKWISE_180 -> 2; case COUNTERCLOCKWISE_90 -> 3; default -> 0; };
        int v2 = switch(r2) { case CLOCKWISE_90 -> 1; case CLOCKWISE_180 -> 2; case COUNTERCLOCKWISE_90 -> 3; default -> 0; };
        return switch((v1 + v2) % 4) {
            case 1 -> Rotation.CLOCKWISE_90;
            case 2 -> Rotation.CLOCKWISE_180;
            case 3 -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    public void forceResetArena() {
        this.cooldownEndTime = 0;
        this.lastDefeatedLeader = "";
        this.isArenaActive = false;
        this.currentLeader = "";
        this.hasChallengerEntered = false;
        this.postBattleTicks = -1;
        this.arenaTimeoutTicks = 0;

        if (this.level instanceof ServerLevel serverLevel) {
            clearGymNPCs(serverLevel);
            setDoorState(serverLevel, false);
        }

        this.setChanged();
        CobblemonColonies.LOGGER.info("Gym Arena forcefully reset by an Operator.");
    }


    private void faceNPCsTowardsBlock(ServerLevel level, BlockPos tagA, BlockPos tagB) {
        if (tagA == null || tagB == null) return;

        AABB arenaBox = getArenaBox(tagA, tagB);

        double targetX = this.getBlockPos().getX() + 0.5;
        double targetZ = this.getBlockPos().getZ() + 0.5;

        for (Entity e : level.getEntities(null, arenaBox)) {
            if (BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString().equals("cobblemon:npc")) {

                double dX = targetX - e.getX();
                double dZ = targetZ - e.getZ();

                float yaw = (float) (Math.atan2(dZ, dX) * (180.0 / Math.PI)) - 90.0F;

                e.setYRot(yaw);
                e.setXRot(0.0F);
                e.setYHeadRot(yaw);

                if (e instanceof net.minecraft.world.entity.LivingEntity living) {
                    living.yBodyRot = yaw;
                }
            }
        }
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putLong("GymCooldown", cooldownEndTime);
        tag.putString("LastLeader", lastDefeatedLeader);
        tag.putBoolean("ArenaActive", isArenaActive);
        tag.putString("CurrentLeader", currentLeader);
        tag.putBoolean("ChallengerEntered", hasChallengerEntered);
        tag.putInt("ArenaTimeout", arenaTimeoutTicks);
        if (this.level != null && !this.level.isClientSide) {
            tag.putInt("ClientBuildingLevel", getSyncedBuildingLevel());
        }
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.loadAdditional(tag, provider);
        this.cooldownEndTime = tag.getLong("GymCooldown");
        this.lastDefeatedLeader = tag.getString("LastLeader");
        this.isArenaActive = tag.getBoolean("ArenaActive");
        this.currentLeader = tag.getString("CurrentLeader");
        this.hasChallengerEntered = tag.getBoolean("ChallengerEntered");
        this.arenaTimeoutTicks = tag.getInt("ArenaTimeout");
        if (tag.contains("ClientBuildingLevel")) {
            this.syncedBuildingLevel = tag.getInt("ClientBuildingLevel");
        }
    }
}