package com.ogtenzohd.cmoncol.colony.ai;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.minecolonies.api.entity.ai.combat.threat.IThreatTableEntity;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.core.entity.ai.workers.guard.AbstractEntityAIGuard;
import com.minecolonies.core.entity.ai.workers.guard.KnightCombatAI;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.ogtenzohd.cmoncol.colony.buildings.PokemonGuardBuilding;
import com.ogtenzohd.cmoncol.colony.job.PokemonGuardJob;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.level.block.Blocks;

import java.util.EnumSet;
import java.util.UUID;

public class PokemonGuardAI extends AbstractEntityAIGuard<PokemonGuardJob, PokemonGuardBuilding> {

    private UUID partnerUUID = null;
    private int spawnTimer = 60;
    private int stuckTimer = 0;
    private boolean isPhasing = false;
    private net.minecraft.world.phys.Vec3 lastPokePos = net.minecraft.world.phys.Vec3.ZERO;

    public PokemonGuardAI(PokemonGuardJob job) {
        super(job);
        toolsNeeded.add(ModEquipmentTypes.sword.get());
        new KnightCombatAI((EntityCitizen) worker, getStateAI(), this);
    }

    @Override
    public Class<PokemonGuardBuilding> getExpectedBuildingClass() {
        return PokemonGuardBuilding.class;
    }

    private int getCustomAttackDelay(String species) {
        switch (species.toLowerCase()) {
            case "scyther":
            case "scizor":
                return 5;
            case "pikachu":
            case "raichu":
            case "croagunk":
            case "toxicroak":
                return 10;
            case "growlithe":
            case "arcanine":
            case "corphish":
            case "crawdaunt":
                return 15;
            case "weepinbell":
            case "victreebel":
            case "sealeo":
            case "walrein":
            case "stoutland":
                return 20;
            case "magneton":
            case "magnezone":
            case "gigalith":
                return 15;
            default:
                return 25;
        }
    }

    @Override
    public void tick() {
        if (job.getWorkBuilding() == null || job.getColony().getWorld() == null) {
            return;
        }

        super.tick();

        job.getCitizen().getEntity().ifPresent(guard -> {
            if (guard.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {

                int myCitizenId = job.getCitizen().getId();
                String expectedSpecies = "growlithe";
                int spawnLevel = 10;

                if (guard.tickCount % 100 == 0) {
                    String targetTag = "guard_partner_" + guard.getUUID();
                    java.util.List<com.cobblemon.mod.common.entity.pokemon.PokemonEntity> clones = serverLevel.getEntitiesOfClass(
                            com.cobblemon.mod.common.entity.pokemon.PokemonEntity.class,
                            guard.getBoundingBox().inflate(128.0D),
                            entity -> entity.getTags().contains(targetTag)
                    );

                    for (com.cobblemon.mod.common.entity.pokemon.PokemonEntity clone : clones) {
                        if (partnerUUID != null && !clone.getUUID().equals(partnerUUID)) {
                            clone.discard();
                        }
                    }
                }

                if (job.getWorkBuilding() != null) {
                    BlockPos bldPos = job.getWorkBuilding().getPosition();
                    net.minecraft.world.level.block.entity.BlockEntity be = serverLevel.getBlockEntity(bldPos);

                    if (be instanceof com.ogtenzohd.cmoncol.blocks.custom.pokemonguard.PokemonGuardBuildingBlockEntity guardBE) {
                        String assignment = guardBE.getAssignedPartner(myCitizenId);
                        if (assignment == null || assignment.equals("growlithe")) {
                            for (int fallbackSlot = 0; fallbackSlot < 3; fallbackSlot++) {
                                String fallback = guardBE.getAssignedPartner(fallbackSlot);
                                if (!fallback.equals("growlithe")) {
                                    assignment = fallback;
                                    break;
                                }
                            }
                        }
                        if (assignment != null && !assignment.isEmpty()) {
                            expectedSpecies = assignment.toLowerCase();
                        }
                    }
                    spawnLevel = job.getWorkBuilding().getBuildingLevel() * 10;
                }

                if (partnerUUID != null) {
                    net.minecraft.world.entity.Entity currentPartner = serverLevel.getEntity(partnerUUID);
                    if (currentPartner instanceof com.cobblemon.mod.common.entity.pokemon.PokemonEntity poke) {
                        String actualSpecies = poke.getPokemon().getSpecies().getName().toLowerCase();

                        if (!actualSpecies.equals(expectedSpecies)) {
                            poke.discard();
                            partnerUUID = null;
                            isPhasing = false;
                        } else {
                            double distToGuard = poke.distanceToSqr(guard);

                            if (isPhasing) {
                                poke.noPhysics = true;
                                poke.getNavigation().stop();
                                net.minecraft.world.phys.Vec3 targetPos = guard.position().add(0, 3.0, 0);
                                net.minecraft.world.phys.Vec3 dir = targetPos.subtract(poke.position()).normalize();
                                poke.setDeltaMovement(dir.scale(0.35));
                                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,
                                        poke.getX(), poke.getY() + (poke.getBbHeight() / 2.0), poke.getZ(), 5, 0.5, 0.5, 0.5, 0.0);

                                if (distToGuard < 6.25) {
                                    isPhasing = false;
                                    poke.noPhysics = false;
                                    stuckTimer = 0;
                                }
                            } else {
                                if (distToGuard > 64.0) {
                                    if (poke.position().distanceToSqr(lastPokePos) < 0.01) {
                                        stuckTimer++;
                                    } else {
                                        stuckTimer = 0;
                                    }
                                    if (stuckTimer > 60) {
                                        isPhasing = true;
                                    }
                                } else {
                                    stuckTimer = 0;
                                }
                                lastPokePos = poke.position();
                            }
                        }
                    }
                }

                if (partnerUUID == null || serverLevel.getEntity(partnerUUID) == null) {
                    spawnTimer--;
                    if (spawnTimer <= 0) {
                        String targetTag = "guard_partner_" + guard.getUUID();
                        java.util.List<com.cobblemon.mod.common.entity.pokemon.PokemonEntity> lostPokemon = serverLevel.getEntitiesOfClass(
                                com.cobblemon.mod.common.entity.pokemon.PokemonEntity.class,
                                guard.getBoundingBox().inflate(128.0D),
                                entity -> entity.getTags().contains(targetTag)
                        );

                        if (!lostPokemon.isEmpty()) {
                            com.cobblemon.mod.common.entity.pokemon.PokemonEntity recoveredPoke = lostPokemon.get(0);
                            this.partnerUUID = recoveredPoke.getUUID();
                            injectGuardGoals(recoveredPoke, guard);
                            for (int i = 1; i < lostPokemon.size(); i++) {
                                lostPokemon.get(i).discard();
                            }
                        } else {
                            spawnPartner(guard, serverLevel, expectedSpecies, spawnLevel);
                        }

                        spawnTimer = 20;
                    }
                }
            }
        });
    }

    private void spawnPartner(net.minecraft.world.entity.LivingEntity guard, net.minecraft.server.level.ServerLevel level, String chosenSpecies, int spawnLevel) {
        String propsString = "species=" + chosenSpecies + " level=" + spawnLevel + " uncatchable=true";
        com.cobblemon.mod.common.pokemon.Pokemon mon = com.cobblemon.mod.common.api.pokemon.PokemonProperties.Companion.parse(propsString).create();
        net.minecraft.world.entity.EntityType<?> type = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(net.minecraft.resources.ResourceLocation.parse("cobblemon:pokemon"));

        net.minecraft.world.entity.Entity newEntity = type.create(level);
        if (newEntity instanceof com.cobblemon.mod.common.entity.pokemon.PokemonEntity pokeEntity) {
            pokeEntity.setPokemon(mon);
            pokeEntity.setPos(guard.getX(), guard.getY(), guard.getZ());
            pokeEntity.setInvulnerable(false);
            pokeEntity.setPersistenceRequired();
            pokeEntity.getTags().add("guard_partner_" + guard.getUUID());
            pokeEntity.getPersistentData().putBoolean("cmoncol:is_guard", true);

            injectGuardGoals(pokeEntity, guard);

            level.addFreshEntity(pokeEntity);
            this.partnerUUID = pokeEntity.getUUID();
        }
    }

    private boolean isFriendly(LivingEntity target, LivingEntity guard) {
        if (target == null) return false;
        if (target == guard) return true;

        if (target instanceof com.minecolonies.api.entity.citizen.AbstractEntityCitizen targetCitizen &&
                guard instanceof com.minecolonies.api.entity.citizen.AbstractEntityCitizen guardCitizen) {

            if (targetCitizen.getCitizenData() != null && guardCitizen.getCitizenData() != null) {
                if (targetCitizen.getCitizenData().getColony() != null && guardCitizen.getCitizenData().getColony() != null) {
                    return targetCitizen.getCitizenData().getColony().getID() == guardCitizen.getCitizenData().getColony().getID();
                }
            }
            return true;
        }

        if (target instanceof net.minecraft.world.entity.player.Player) return true;
        if (target.getTags().contains("cmoncol_dummy")) return true;
        if (target.getTags().stream().anyMatch(tag -> tag.startsWith("guard_partner_"))) return true;

        return false;
    }

    private void injectGuardGoals(PokemonEntity pokemon, LivingEntity guard) {
        pokemon.getBrain().removeAllBehaviors();
        pokemon.stopSleeping();

        pokemon.goalSelector.addGoal(0, new Goal() {
            @Override
            public boolean canUse() {
                if (!guard.isAlive()) return true;
                if (guard instanceof EntityCitizen citizen) {
                    if (citizen.getCitizenData() == null || !(citizen.getCitizenData().getJob() instanceof PokemonGuardJob)) {
                        return true;
                    }
                }
                return false;
            }
            @Override
            public void start() { pokemon.discard(); }
        });

        pokemon.goalSelector.addGoal(2, new Goal() {
            private int stuckTicks = 0;

            { this.setFlags(EnumSet.of(Goal.Flag.MOVE)); }

            @Override
            public boolean canUse() {
                return guard.isAlive() && pokemon.distanceToSqr(guard) > 25;
            }

            @Override
            public void tick() {
                pokemon.getNavigation().moveTo(guard, 1.2D);
                if (pokemon.distanceToSqr(guard) > 144) {
                    stuckTicks++;
                    if (stuckTicks > 60) {
                        BlockPos targetPos = guard.blockPosition();
                        for (int i = 0; i < 10; i++) {
                            int dx = pokemon.getRandom().nextInt(5) - 2;
                            int dz = pokemon.getRandom().nextInt(5) - 2;
                            BlockPos pos = targetPos.offset(dx, 0, dz);

                            if (pokemon.level().isEmptyBlock(pos) && pokemon.level().isEmptyBlock(pos.above())) {
                                pokemon.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                                pokemon.getNavigation().stop();
                                stuckTicks = 0;
                                return;
                            }
                        }
                    }
                } else {
                    stuckTicks = 0;
                }
            }
        });

        pokemon.targetSelector.addGoal(1, new Goal() {
            @Override public boolean canUse() {
                LivingEntity target = guard.getLastHurtMob();
                return target != null && target.isAlive() && !isFriendly(target, guard);
            }
            @Override public void start() { pokemon.setTarget(guard.getLastHurtMob()); }
        });

        pokemon.targetSelector.addGoal(2, new Goal() {
            @Override public boolean canUse() {
                LivingEntity attacker = guard.getLastHurtByMob();
                return attacker != null && attacker.isAlive() && !isFriendly(attacker, guard);
            }
            @Override public void start() { pokemon.setTarget(guard.getLastHurtByMob()); }
        });

        pokemon.targetSelector.addGoal(3, new Goal() {
            @Override public boolean canUse() {
                if (guard instanceof IThreatTableEntity threatEntity) {
                    LivingEntity target = threatEntity.getThreatTable().getTargetMob();
                    return target != null && target.isAlive() && !isFriendly(target, guard);
                }
                return false;
            }
            @Override public void start() {
                if (guard instanceof IThreatTableEntity threatEntity) {
                    pokemon.setTarget(threatEntity.getThreatTable().getTargetMob());
                }
            }
        });
        String speciesName = pokemon.getPokemon().getSpecies().getName().toLowerCase();
        int attackCooldown = getCustomAttackDelay(speciesName);
        pokemon.goalSelector.addGoal(1, new GuardPokemonAttackGoal(pokemon, 1.2D, true, attackCooldown));
    }
}

class GuardPokemonAttackGoal extends net.minecraft.world.entity.ai.goal.MeleeAttackGoal {
    private final int customCooldown;

    public GuardPokemonAttackGoal(net.minecraft.world.entity.PathfinderMob mob, double speedModifier, boolean followTargetEvenIfNotSeen, int customCooldown) {
        super(mob, speedModifier, followTargetEvenIfNotSeen);
        this.customCooldown = customCooldown;
    }

    @Override
    protected int getAttackInterval() {
        return this.adjustedTickDelay(this.customCooldown);
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity target) {
        if (this.canPerformAttack(target)) {
            this.resetAttackCooldown();
            this.mob.swing(net.minecraft.world.InteractionHand.MAIN_HAND);

            if (this.mob.level() instanceof ServerLevel sl && this.mob instanceof PokemonEntity pokemon) {
                String species = pokemon.getPokemon().getSpecies().getName().toLowerCase();

                switch (species) {
                    case "growlithe":
                    case "arcanine":
                        target.hurt(pokemon.damageSources().mobAttack(pokemon), species.equals("arcanine") ? 15.0F : 10.0F);
                        target.igniteForSeconds(5.0F);
                        sl.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1, target.getZ(), species.equals("arcanine") ? 30 : 15, 0.5D, 0.5D, 0.5D, 0.1D);
                        break;

                    case "pikachu":
                    case "raichu":
                    case "magneton":
                    case "magnezone":
                        target.hurt(pokemon.damageSources().magic(), (species.equals("raichu") || species.equals("magnezone")) ? 14.0F : 8.0F);
                        for (int y = 0; y < 5; y++) {
                            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + y, target.getZ(), 10, 0.2, 0.5, 0.2, 0.1);
                        }
                        break;

                    case "corphish":
                    case "crawdaunt":
                        target.hurt(pokemon.damageSources().mobAttack(pokemon), species.equals("crawdaunt") ? 14.0F : 8.0F);
                        sl.sendParticles(ParticleTypes.BUBBLE_POP, target.getX(), target.getY() + 1, target.getZ(), 20, 0.5D, 0.5D, 0.5D, 0.1D);
                        sl.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY() + 1, target.getZ(), 20, 0.5D, 0.5D, 0.5D, 0.1D);
                        break;

                    case "weepinbell":
                    case "victreebel":
                        target.hurt(pokemon.damageSources().mobAttack(pokemon), species.equals("victreebel") ? 14.0F : 9.0F);
                        target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));

                        net.minecraft.world.phys.Vec3 start = pokemon.position().add(0, 1.0, 0);
                        net.minecraft.world.phys.Vec3 end = target.position().add(0, 1.0, 0);
                        net.minecraft.world.phys.Vec3 dir = end.subtract(start);
                        for (int i = 0; i < 15; i++) {
                            double progress = (double) i / 15.0;
                            double pX = start.x + dir.x * progress;
                            double pY = start.y + dir.y * progress;
                            double pZ = start.z + dir.z * progress;
                            sl.sendParticles(ParticleTypes.ITEM_SLIME, pX, pY, pZ, 2, 0.1, 0.1, 0.1, 0.0);
                        }
                        sl.sendParticles(ParticleTypes.ITEM_SLIME, target.getX(), target.getY() + 1, target.getZ(), 15, 0.5, 0.5, 0.5, 0.1);
                        break;

                    case "scyther":
                    case "scizor":
                        target.hurt(pokemon.damageSources().mobAttack(pokemon), species.equals("scizor") ? 16.0F : 12.0F);
                        sl.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1, target.getZ(), 3, 0.5D, 0.5D, 0.5D, 0.0D);
                        break;

                    case "croagunk":
                    case "toxicroak":
                        target.hurt(pokemon.damageSources().mobAttack(pokemon), species.equals("toxicroak") ? 14.0F : 9.0F);
                        target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON, 100, species.equals("toxicroak") ? 1 : 0));
                        sl.sendParticles(ParticleTypes.WITCH, target.getX(), target.getY() + 1, target.getZ(), 25, 0.5D, 0.5D, 0.5D, 0.1D);
                        break;

                    case "sealeo":
                    case "walrein":
                        target.hurt(pokemon.damageSources().freeze(), species.equals("walrein") ? 15.0F : 10.0F);
                        target.setTicksFrozen(100);
                        target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 100, species.equals("walrein") ? 2 : 1));
                        sl.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + 1, target.getZ(), 30, 0.5D, 0.5D, 0.5D, 0.05D);
                        break;

                    case "stoutland":
                        EvokerFangs fangs = new EvokerFangs(sl, target.getX(), target.getY(), target.getZ(), target.getYRot(), 0, pokemon);
                        sl.addFreshEntity(fangs);
                        break;

                    case "gigalith":
                        net.minecraft.world.phys.AABB aoeBox = target.getBoundingBox().inflate(1.5D);
                        sl.getEntitiesOfClass(LivingEntity.class, aoeBox).forEach(victim -> {
                            if (victim != pokemon && victim != this.mob) {
                                victim.hurt(victim.damageSources().fallingBlock(pokemon), 18.0F);
                                sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DRIPSTONE_BLOCK.defaultBlockState()),
                                        victim.getX(), victim.getY() + 2.0, victim.getZ(), 20, 0.5, 0.5, 0.5, 0.1);
                            }
                        });
                        break;

                    default:
                        target.hurt(pokemon.damageSources().mobAttack(pokemon), 10.0F);
                        sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1, target.getZ(), 10, 0.5D, 0.5D, 0.5D, 0.1D);
                        break;
                }
            }
        }
    }
}