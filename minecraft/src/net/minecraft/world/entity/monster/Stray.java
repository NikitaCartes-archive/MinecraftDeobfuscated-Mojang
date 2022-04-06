package net.minecraft.world.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;

public class Stray extends AbstractSkeleton {
	public Stray(EntityType<? extends Stray> entityType, Level level) {
		super(entityType, level);
	}

	public static boolean checkStraySpawnRules(
		EntityType<Stray> entityType, ServerLevelAccessor serverLevelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource
	) {
		BlockPos blockPos2 = blockPos;

		do {
			blockPos2 = blockPos2.above();
		} while (serverLevelAccessor.getBlockState(blockPos2).is(Blocks.POWDER_SNOW));

		return checkMonsterSpawnRules(entityType, serverLevelAccessor, mobSpawnType, blockPos, randomSource)
			&& (mobSpawnType == MobSpawnType.SPAWNER || serverLevelAccessor.canSeeSky(blockPos2.below()));
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.STRAY_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.STRAY_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.STRAY_DEATH;
	}

	@Override
	SoundEvent getStepSound() {
		return SoundEvents.STRAY_STEP;
	}

	@Override
	protected AbstractArrow getArrow(ItemStack itemStack, float f) {
		AbstractArrow abstractArrow = super.getArrow(itemStack, f);
		if (abstractArrow instanceof Arrow) {
			((Arrow)abstractArrow).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 600));
		}

		return abstractArrow;
	}
}
