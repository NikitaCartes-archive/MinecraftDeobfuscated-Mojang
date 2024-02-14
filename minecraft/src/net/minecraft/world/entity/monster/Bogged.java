package net.minecraft.world.entity.monster;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Bogged extends AbstractSkeleton {
	private static final int HARD_ATTACK_INTERVAL = 50;
	private static final int NORMAL_ATTACK_INTERVAL = 70;

	public static AttributeSupplier.Builder createAttributes() {
		return AbstractSkeleton.createAttributes().add(Attributes.MAX_HEALTH, 16.0);
	}

	public Bogged(EntityType<? extends Bogged> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.BOGGED_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.BOGGED_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.BOGGED_DEATH;
	}

	@Override
	protected SoundEvent getStepSound() {
		return SoundEvents.BOGGED_STEP;
	}

	@Override
	protected AbstractArrow getArrow(ItemStack itemStack, float f) {
		AbstractArrow abstractArrow = super.getArrow(itemStack, f);
		if (abstractArrow instanceof Arrow arrow) {
			arrow.addEffect(new MobEffectInstance(MobEffects.POISON, 100));
		}

		return abstractArrow;
	}

	@Override
	protected int getHardAttackInterval() {
		return 50;
	}

	@Override
	protected int getAttackInterval() {
		return 70;
	}
}
