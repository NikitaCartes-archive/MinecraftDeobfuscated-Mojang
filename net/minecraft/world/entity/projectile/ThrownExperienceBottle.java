/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class ThrownExperienceBottle
extends ThrowableItemProjectile {
    public ThrownExperienceBottle(EntityType<? extends ThrownExperienceBottle> entityType, Level level) {
        super((EntityType<? extends ThrowableItemProjectile>)entityType, level);
    }

    public ThrownExperienceBottle(Level level, LivingEntity livingEntity) {
        super((EntityType<? extends ThrowableItemProjectile>)EntityType.EXPERIENCE_BOTTLE, livingEntity, level);
    }

    public ThrownExperienceBottle(Level level, double d, double e, double f) {
        super((EntityType<? extends ThrowableItemProjectile>)EntityType.EXPERIENCE_BOTTLE, d, e, f, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.EXPERIENCE_BOTTLE;
    }

    @Override
    protected float getGravity() {
        return 0.07f;
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (!this.level.isClientSide) {
            int j;
            this.level.levelEvent(2002, new BlockPos(this), PotionUtils.getColor(Potions.WATER));
            for (int i = 3 + this.level.random.nextInt(5) + this.level.random.nextInt(5); i > 0; i -= j) {
                j = ExperienceOrb.getExperienceValue(i);
                this.level.addFreshEntity(new ExperienceOrb(this.level, this.x, this.y, this.z, j));
            }
            this.remove();
        }
    }
}

