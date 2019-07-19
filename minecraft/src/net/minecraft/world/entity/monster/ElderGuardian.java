package net.minecraft.world.entity.monster;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
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

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3F);
		this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(8.0);
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(80.0);
	}

	@Override
	public int getAttackDuration() {
		return 60;
	}

	@Environment(EnvType.CLIENT)
	public void setGhost() {
		this.clientSideSpikesAnimation = 1.0F;
		this.clientSideSpikesAnimationO = this.clientSideSpikesAnimation;
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
					serverPlayer.connection.send(new ClientboundGameEventPacket(10, 0.0F));
					serverPlayer.addEffect(new MobEffectInstance(mobEffect, 6000, 2));
				}
			}
		}

		if (!this.hasRestriction()) {
			this.restrictTo(new BlockPos(this), 16);
		}
	}
}
