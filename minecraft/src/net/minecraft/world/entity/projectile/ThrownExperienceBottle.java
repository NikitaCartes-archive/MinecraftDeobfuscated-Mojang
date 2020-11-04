package net.minecraft.world.entity.projectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class ThrownExperienceBottle extends ThrowableItemProjectile {
	public ThrownExperienceBottle(EntityType<? extends ThrownExperienceBottle> entityType, Level level) {
		super(entityType, level);
	}

	public ThrownExperienceBottle(Level level, LivingEntity livingEntity) {
		super(EntityType.EXPERIENCE_BOTTLE, livingEntity, level);
	}

	public ThrownExperienceBottle(Level level, double d, double e, double f) {
		super(EntityType.EXPERIENCE_BOTTLE, d, e, f, level);
	}

	@Override
	protected Item getDefaultItem() {
		return Items.EXPERIENCE_BOTTLE;
	}

	@Override
	protected float getGravity() {
		return 0.07F;
	}

	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);
		if (this.level instanceof ServerLevel) {
			this.level.levelEvent(2002, this.blockPosition(), PotionUtils.getColor(Potions.WATER));
			int i = 3 + this.level.random.nextInt(5) + this.level.random.nextInt(5);
			ExperienceOrb.award((ServerLevel)this.level, this.position(), i);
			this.discard();
		}
	}
}
