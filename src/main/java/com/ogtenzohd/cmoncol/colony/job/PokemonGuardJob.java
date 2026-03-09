package com.ogtenzohd.cmoncol.colony.job;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.core.colony.jobs.AbstractJobGuard;
import com.minecolonies.core.util.AttributeModifierUtils;
import com.ogtenzohd.cmoncol.colony.ai.PokemonGuardAI;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import static com.minecolonies.api.util.constant.CitizenConstants.GUARD_HEALTH_MOD_LEVEL_NAME;
import static com.minecolonies.api.util.constant.GuardConstants.KNIGHT_HP_BONUS;

public class PokemonGuardJob extends AbstractJobGuard<PokemonGuardJob> {

    public PokemonGuardJob(final ICitizenData entity) {
        super(entity);
    }

    @Override
    public PokemonGuardAI generateGuardAI() {
        return new PokemonGuardAI(this);
    }

    @Override
    public void onLevelUp() {
        if (getCitizen().getEntity().isPresent()) {
            final AbstractEntityCitizen citizen = getCitizen().getEntity().get();

            final AttributeModifier healthModLevel =
              new AttributeModifier(GUARD_HEALTH_MOD_LEVEL_NAME,
                getCitizen().getCitizenSkillHandler().getLevel(Skill.Stamina) + KNIGHT_HP_BONUS,
                AttributeModifier.Operation.ADD_VALUE);
                
            AttributeModifierUtils.addHealthModifier(citizen, healthModLevel);
        }
    }
}