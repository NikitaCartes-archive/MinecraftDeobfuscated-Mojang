package net.minecraft.world.entity.monster;

import java.util.List;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class ElderGuardian extends Guardian {
	public static final float ELDER_SIZE_SCALE = EntityType.ELDER_GUARDIAN.getWidth() / EntityType.GUARDIAN.getWidth();

	public ElderGuardian(EntityType<? extends ElderGuardian> entityType, Level level) {
		super(entityType, level);
		this.setPersistenceRequired();
		if (this.randomStrollGoal != null) {
			this.randomStrollGoal.setInterval(400);
		}
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Guardian.createAttributes().add(Attributes.MOVEMENT_SPEED, 0.3F).add(Attributes.ATTACK_DAMAGE, 8.0).add(Attributes.MAX_HEALTH, 80.0);
	}

	@Override
	public int getAttackDuration() {
		return 60;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return this.isInWaterOrBubble() ? SoundEvents.ELDER_GUARDIAN_AMBIENT : SoundEvents.ELDER_GUARDIAN_AMBIENT_LAND;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return this.isInWaterOrBubble() ? SoundEvents.ELDER_GUARDIAN_HURT : SoundEvents.ELDER_GUARDIAN_HURT_LAND;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return this.isInWaterOrBubble() ? SoundEvents.ELDER_GUARDIAN_DEATH : SoundEvents.ELDER_GUARDIAN_DEATH_LAND;
	}

	@Override
	protected SoundEvent getFlopSound() {
		return SoundEvents.ELDER_GUARDIAN_FLOP;
	}

	@Override
	protected void customServerAiStep() {
		super.customServerAiStep();
		int i = 1200;
		if ((this.tickCount + this.getId()) % 1200 == 0) {
			MobEffect mobEffect = MobEffects.DIG_SLOWDOWN;
			List<ServerPlayer> list = ((ServerLevel)this.level)
				.getPlayers(serverPlayerx -> this.distanceToSqr(serverPlayerx) < 2500.0 && serverPlayerx.gameMode.isSurvival());
			int j = 2;
			int k = 6000;
			int l = 1200;

			for (ServerPlayer serverPlayer : list) {
				if (!serverPlayer.hasEffect(mobEffect) || serverPlayer.getEffect(mobEffect).getAmplifier() < 2 || serverPlayer.getEffect(mobEffect).getDuration() < 1200) {
					serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT, this.isSilent() ? 0.0F : 1.0F));
					serverPlayer.addEffect(new MobEffectInstance(mobEffect, 6000, 2));
				}
			}
		}

		if (!this.hasRestriction()) {
			this.restrictTo(this.blockPosition(), 16);
		}
	}
}
