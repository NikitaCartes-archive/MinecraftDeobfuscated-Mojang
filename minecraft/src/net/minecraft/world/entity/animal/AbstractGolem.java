package net.minecraft.world.entity.animal;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

public abstract class AbstractGolem extends PathfinderMob {
	protected AbstractGolem(EntityType<? extends AbstractGolem> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	public boolean causeFallDamage(float f, float g) {
		return false;
	}

	@Nullable
	@Override
	protected SoundEvent getAmbientSound() {
		return null;
	}

	@Nullable
	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return null;
	}

	@Nullable
	@Override
	protected SoundEvent getDeathSound() {
		return null;
	}

	@Override
	public int getAmbientSoundInterval() {
		return 120;
	}

	@Override
	public boolean removeWhenFarAway(double d) {
		return false;
	}
}
