/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

public class Giant
extends Monster {
    public Giant(EntityType<? extends Giant> entityType, Level level) {
        super((EntityType<? extends Monster>)entityType, level);
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return 10.440001f;
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(100.0);
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5);
        this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(50.0);
    }

    @Override
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        return levelReader.getBrightness(blockPos) - 0.5f;
    }
}

