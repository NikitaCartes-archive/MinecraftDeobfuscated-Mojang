package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class SpectralArrow extends AbstractArrow {
	private int duration = 200;

	public SpectralArrow(EntityType<? extends SpectralArrow> entityType, Level level) {
		super(entityType, level);
	}

	public SpectralArrow(Level level, LivingEntity livingEntity) {
		super(EntityType.SPECTRAL_ARROW, livingEntity, level);
	}

	public SpectralArrow(Level level, double d, double e, double f) {
		super(EntityType.SPECTRAL_ARROW, d, e, f, level);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level.isClientSide && !this.inGround) {
			this.level.addParticle(ParticleTypes.INSTANT_EFFECT, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
		}
	}

	@Override
	protected ItemStack getPickupItem() {
		return new ItemStack(Items.SPECTRAL_ARROW);
	}

	@Override
	protected void doPostHurtEffects(LivingEntity livingEntity) {
		super.doPostHurtEffects(livingEntity);
		MobEffectInstance mobEffectInstance = new MobEffectInstance(MobEffects.GLOWING, this.duration, 0);
		livingEntity.addEffect(mobEffectInstance);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.contains("Duration")) {
			this.duration = compoundTag.getInt("Duration");
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("Duration", this.duration);
	}
}
