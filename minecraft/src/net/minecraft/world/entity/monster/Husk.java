package net.minecraft.world.entity.monster;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class Husk extends Zombie {
	public Husk(EntityType<? extends Husk> entityType, Level level) {
		super(entityType, level);
	}

	public static boolean checkHuskSpawnRules(
		EntityType<Husk> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random
	) {
		return checkMonsterSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, random)
			&& (mobSpawnType == MobSpawnType.SPAWNER || levelAccessor.canSeeSky(blockPos));
	}

	@Override
	protected boolean isSunSensitive() {
		return false;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.HUSK_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.HUSK_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.HUSK_DEATH;
	}

	@Override
	protected SoundEvent getStepSound() {
		return SoundEvents.HUSK_STEP;
	}

	@Override
	public boolean doHurtTarget(Entity entity) {
		boolean bl = super.doHurtTarget(entity);
		if (bl && this.getMainHandItem().isEmpty() && entity instanceof LivingEntity) {
			float f = this.level.getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();
			((LivingEntity)entity).addEffect(new MobEffectInstance(MobEffects.HUNGER, 140 * (int)f));
		}

		return bl;
	}

	@Override
	protected boolean convertsInWater() {
		return true;
	}

	@Override
	protected void doUnderWaterConversion() {
		this.convertToZombieType(EntityType.ZOMBIE);
		if (!this.isSilent()) {
			this.level.levelEvent(null, 1041, this.blockPosition(), 0);
		}
	}

	@Override
	protected ItemStack getSkull() {
		return ItemStack.EMPTY;
	}
}
