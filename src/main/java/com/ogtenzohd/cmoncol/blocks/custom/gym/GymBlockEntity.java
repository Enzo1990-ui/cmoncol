package com.ogtenzohd.cmoncol.blocks.custom.gym;

import com.ogtenzohd.cmoncol.CobblemonColonies;
import com.ogtenzohd.cmoncol.registration.CmoncolReg;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GymBlockEntity extends TileEntityColonyBuilding {

    private boolean isArenaActive = false;
    private int postBattleTicks = -1;
    private long cooldownEndTime = 0;
    private String lastDefeatedLeader = "";
    private String currentLeader = "";
    
    private boolean hasChallengerEntered = false;
    private int arenaTimeoutTicks = 0;

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
                    AABB arenaBox = new AABB(tagA.getX(), tagA.getY(), tagA.getZ(), tagB.getX() + 1.0, tagB.getY() + 1.0, tagB.getZ() + 1.0).inflate(2);
                    
                    boolean npcExists = false;
                    boolean playerExists = false;
                    
                    for (Entity e : serverLevel.getEntities(null, arenaBox)) {
                        if (BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString().equals("cobblemon:npc")) {
                            npcExists = true;
                        } else if (e instanceof Player) {
                            playerExists = true;
                        }
                    }

                    // it wouldnt let me through the door!!
                    if (!hasChallengerEntered) {
                        if (playerExists) {
                            hasChallengerEntered = true;
                            CobblemonColonies.LOGGER.info("Challenger has entered the arena! Match officially begins.");
                        } else {
                            arenaTimeoutTicks -= 20;
                            if (arenaTimeoutTicks <= 0) {
                                isArenaActive = false;
                                currentLeader = "";
                                String killCmd = String.format("kill @e[type=cobblemon:npc,x=%d,y=%d,z=%d,distance=..30]", this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ());
                                serverLevel.getServer().getCommands().performPrefixedCommand(serverLevel.getServer().createCommandSourceStack().withSuppressedOutput(), killCmd);
                                setDoorState(serverLevel, false);
                                CobblemonColonies.LOGGER.info("Challenger never entered the arena. Doors closed.");
                            }
                        }
                        return;
                    }

                    if (!npcExists) {
                        isArenaActive = false;
                        postBattleTicks = 100;
                        lastDefeatedLeader = currentLeader;
                        cooldownEndTime = serverLevel.getGameTime() + 24000;
                        CobblemonColonies.LOGGER.info("Gym Leader Defeated! Teleporting in 5 seconds...");
                    }
                    else if (!playerExists) {
                        isArenaActive = false;
                        currentLeader = "";
                        String killCmd = String.format("kill @e[type=cobblemon:npc,x=%d,y=%d,z=%d,distance=..30]", this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ());
                        serverLevel.getServer().getCommands().performPrefixedCommand(serverLevel.getServer().createCommandSourceStack().withSuppressedOutput(), killCmd);
                        setDoorState(serverLevel, false);
                        cooldownEndTime = serverLevel.getGameTime() + 6000;
                        CobblemonColonies.LOGGER.info("Challenger lost or fled. Arena reset and locked.");
                    }
                }
            }
        }
        if (postBattleTicks > 0) {
            postBattleTicks--;
            if (postBattleTicks == 0) {
                BlockPos tagA = getTagPos("structure_a");
                BlockPos tagB = getTagPos("structure_b");
                
                if (tagA != null && tagB != null) {
                    AABB arenaBox = new AABB(tagA.getX(), tagA.getY(), tagA.getZ(), tagB.getX() + 1.0, tagB.getY() + 1.0, tagB.getZ() + 1.0).inflate(2);
                    
                    for (Player player : serverLevel.players()) {
                        if (arenaBox.contains(player.position())) {
                            player.teleportTo(this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 1.0, this.getBlockPos().getZ() + 0.5);
                        }
                    }
                }
                
                setDoorState(serverLevel, false);
                currentLeader = "";
            }
        }
    }

    public String checkCanBattle(String requestedLeader) {
        if (this.level == null) return "error";
        
        if (isArenaActive || postBattleTicks > 0) {
            return "A battle is currently taking place. Please wait.";
        }

        if (this.level.getGameTime() < cooldownEndTime) {
            if (requestedLeader.equalsIgnoreCase(lastDefeatedLeader)) {
                return "Gym Leader's pokemon need to recover come back later";
            } else {
                return "Gym Leader isn't available right now come back later";
            }
        }
        return "yes"; // not true.. YES because why not
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

    public void loadGymArena(ServerLevel serverLevel, BlockPos gymBlockPos, String leaderName) {
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
            int holeMaxY = Math.max(tagA.getY(), tagB.getY());

            int holeWidth = (holeMaxX - holeMinX) + 1;
            int holeLength = (holeMaxZ - holeMinZ) + 1;

            Direction facing = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
            Rotation baseRot = switch (facing) {
                case NORTH -> Rotation.CLOCKWISE_180;
                case EAST -> Rotation.COUNTERCLOCKWISE_90;
                case WEST -> Rotation.CLOCKWISE_90;
                default -> Rotation.NONE;
            };

			//i built the structures the wrong way round and im not rebuilding them
            Rotation extraRot = switch (leaderName.toLowerCase()) {
                case "erika" -> Rotation.CLOCKWISE_180;
                case "sabrina" -> Rotation.CLOCKWISE_180;
                case "koga" -> Rotation.CLOCKWISE_180;
				case "blaine" -> Rotation.CLOCKWISE_180;
                default -> Rotation.NONE; 
            };
            
            Rotation rot = combineRotations(baseRot, extraRot);
            
            CobblemonColonies.LOGGER.info("6. Building Facing: {}, Extra NBT Fix: {}, Final Rotation: {}", facing, extraRot, rot);
			
            Vec3i nbtSize = template.getSize();
            int rawX = nbtSize.getX();
            int rawZ = nbtSize.getZ();
            
            int nbtW = (rot == Rotation.NONE || rot == Rotation.CLOCKWISE_180) ? rawX : rawZ;
            int nbtL = (rot == Rotation.NONE || rot == Rotation.CLOCKWISE_180) ? rawZ : rawX;

            int targetMinX = holeMinX + ((holeWidth - nbtW) / 2);
            int targetMinZ = holeMinZ + ((holeLength - nbtL) / 2);

            int yAdjust = switch (leaderName.toLowerCase()) {
                case "brock" -> +2;
                case "misty" -> -1;
                case "surge" -> +0;
                case "erika" -> +2;
                case "koga" -> +2;
                case "sabrina" -> +2;
                case "blaine" -> +2;
                case "giovanni" -> -2;
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

            String killCmd = String.format("kill @e[type=cobblemon:npc,x=%d,y=%d,z=%d,distance=..30]", gymBlockPos.getX(), gymBlockPos.getY(), gymBlockPos.getZ());
            serverLevel.getServer().getCommands().performPrefixedCommand(serverLevel.getServer().createCommandSourceStack().withSuppressedOutput(), killCmd);

            double spawnX = holeMinX + (holeWidth / 2.0);
            double spawnZ = holeMinZ + (holeLength / 2.0);
            double spawnY = pasteY + 4.0; 

            String spawnCmd = String.format(java.util.Locale.US, "spawnnpcat %f %f %f cmoncol:%s", spawnX, spawnY, spawnZ, leaderName.toLowerCase());
            serverLevel.getServer().getCommands().performPrefixedCommand(
                serverLevel.getServer().createCommandSourceStack().withLevel(serverLevel).withSuppressedOutput(), spawnCmd);
                
            setDoorState(serverLevel, true);
            isArenaActive = true;
            currentLeader = leaderName.toLowerCase();
            hasChallengerEntered = false;
            arenaTimeoutTicks = 1200;
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
            String killCmd = String.format("kill @e[type=cobblemon:npc,x=%d,y=%d,z=%d,distance=..30]", this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ());
            serverLevel.getServer().getCommands().performPrefixedCommand(serverLevel.getServer().createCommandSourceStack().withSuppressedOutput(), killCmd);
            setDoorState(serverLevel, false);
        }
        
        this.setChanged();
        CobblemonColonies.LOGGER.info("Gym Arena forcefully reset by an Operator.");
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putLong("GymCooldown", cooldownEndTime);
        tag.putString("LastLeader", lastDefeatedLeader);
        tag.putBoolean("ArenaActive", isArenaActive);
        tag.putString("CurrentLeader", currentLeader);
        tag.putBoolean("ChallengerEntered", hasChallengerEntered);
        tag.putInt("ArenaTimeout", arenaTimeoutTicks);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.cooldownEndTime = tag.getLong("GymCooldown");
        this.lastDefeatedLeader = tag.getString("LastLeader");
        this.isArenaActive = tag.getBoolean("ArenaActive");
        this.currentLeader = tag.getString("CurrentLeader");
        this.hasChallengerEntered = tag.getBoolean("ChallengerEntered");
        this.arenaTimeoutTicks = tag.getInt("ArenaTimeout");
    }
}