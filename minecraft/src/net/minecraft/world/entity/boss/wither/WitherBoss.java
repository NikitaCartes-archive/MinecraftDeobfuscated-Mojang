package net.minecraft.world.entity.boss.wither;

import com.google.common.collect.ImmutableList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class WitherBoss extends Monster implements RangedAttackMob {
	private static final EntityDataAccessor<Integer> DATA_TARGET_A = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_TARGET_B = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_TARGET_C = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
	private static final List<EntityDataAccessor<Integer>> DATA_TARGETS = ImmutableList.of(DATA_TARGET_A, DATA_TARGET_B, DATA_TARGET_C);
	private static final EntityDataAccessor<Integer> DATA_ID_INV = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
	private static final int INVULNERABLE_TICKS = 220;
	private final float[] xRotHeads = new float[2];
	private final float[] yRotHeads = new float[2];
	private final float[] xRotOHeads = new float[2];
	private final float[] yRotOHeads = new float[2];
	private final int[] nextHeadUpdate = new int[2];
	private final int[] idleHeadUpdates = new int[2];
	private int destroyBlocksTick;
	private final ServerBossEvent bossEvent = (ServerBossEvent)new ServerBossEvent(
			this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS
		)
		.setDarkenScreen(true);
	private static final Predicate<LivingEntity> LIVING_ENTITY_SELECTOR = livingEntity -> !livingEntity.getType().is(EntityTypeTags.WITHER_FRIENDS)
			&& livingEntity.attackable();
	private static final TargetingConditions TARGETING_CONDITIONS = TargetingConditions.forCombat().range(20.0).selector(LIVING_ENTITY_SELECTOR);

	public WitherBoss(EntityType<? extends WitherBoss> entityType, Level level) {
		super(entityType, level);
		this.moveControl = new FlyingMoveControl(this, 10, false);
		this.setHealth(this.getMaxHealth());
		this.xpReward = 50;
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		FlyingPathNavigation flyingPathNavigation = new FlyingPathNavigation(this, level);
		flyingPathNavigation.setCanOpenDoors(false);
		flyingPathNavigation.setCanFloat(true);
		flyingPathNavigation.setCanPassDoors(true);
		return flyingPathNavigation;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new WitherBoss.WitherDoNothingGoal());
		this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 40, 20.0F));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, LivingEntity.class, 0, false, false, LIVING_ENTITY_SELECTOR));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_TARGET_A, 0);
		builder.define(DATA_TARGET_B, 0);
		builder.define(DATA_TARGET_C, 0);
		builder.define(DATA_ID_INV, 0);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("Invul", this.getInvulnerableTicks());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setInvulnerableTicks(compoundTag.getInt("Invul"));
		if (this.hasCustomName()) {
			this.bossEvent.setName(this.getDisplayName());
		}
	}

	@Override
	public void setCustomName(@Nullable Component component) {
		super.setCustomName(component);
		this.bossEvent.setName(this.getDisplayName());
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.WITHER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.WITHER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.WITHER_DEATH;
	}

	@Override
	public void aiStep() {
		Vec3 vec3 = this.getDeltaMovement().multiply(1.0, 0.6, 1.0);
		if (!this.level().isClientSide && this.getAlternativeTarget(0) > 0) {
			Entity entity = this.level().getEntity(this.getAlternativeTarget(0));
			if (entity != null) {
				double d = vec3.y;
				if (this.getY() < entity.getY() || !this.isPowered() && this.getY() < entity.getY() + 5.0) {
					d = Math.max(0.0, d);
					d += 0.3 - d * 0.6F;
				}

				vec3 = new Vec3(vec3.x, d, vec3.z);
				Vec3 vec32 = new Vec3(entity.getX() - this.getX(), 0.0, entity.getZ() - this.getZ());
				if (vec32.horizontalDistanceSqr() > 9.0) {
					Vec3 vec33 = vec32.normalize();
					vec3 = vec3.add(vec33.x * 0.3 - vec3.x * 0.6, 0.0, vec33.z * 0.3 - vec3.z * 0.6);
				}
			}
		}

		this.setDeltaMovement(vec3);
		if (vec3.horizontalDistanceSqr() > 0.05) {
			this.setYRot((float)Mth.atan2(vec3.z, vec3.x) * (180.0F / (float)Math.PI) - 90.0F);
		}

		super.aiStep();

		for (int i = 0; i < 2; i++) {
			this.yRotOHeads[i] = this.yRotHeads[i];
			this.xRotOHeads[i] = this.xRotHeads[i];
		}

		for (int i = 0; i < 2; i++) {
			int j = this.getAlternativeTarget(i + 1);
			Entity entity2 = null;
			if (j > 0) {
				entity2 = this.level().getEntity(j);
			}

			if (entity2 != null) {
				double e = this.getHeadX(i + 1);
				double f = this.getHeadY(i + 1);
				double g = this.getHeadZ(i + 1);
				double h = entity2.getX() - e;
				double k = entity2.getEyeY() - f;
				double l = entity2.getZ() - g;
				double m = Math.sqrt(h * h + l * l);
				float n = (float)(Mth.atan2(l, h) * 180.0F / (float)Math.PI) - 90.0F;
				float o = (float)(-(Mth.atan2(k, m) * 180.0F / (float)Math.PI));
				this.xRotHeads[i] = this.rotlerp(this.xRotHeads[i], o, 40.0F);
				this.yRotHeads[i] = this.rotlerp(this.yRotHeads[i], n, 10.0F);
			} else {
				this.yRotHeads[i] = this.rotlerp(this.yRotHeads[i], this.yBodyRot, 10.0F);
			}
		}

		boolean bl = this.isPowered();

		for (int jx = 0; jx < 3; jx++) {
			double p = this.getHeadX(jx);
			double q = this.getHeadY(jx);
			double r = this.getHeadZ(jx);
			float s = 0.3F * this.getScale();
			this.level()
				.addParticle(
					ParticleTypes.SMOKE,
					p + this.random.nextGaussian() * (double)s,
					q + this.random.nextGaussian() * (double)s,
					r + this.random.nextGaussian() * (double)s,
					0.0,
					0.0,
					0.0
				);
			if (bl && this.level().random.nextInt(4) == 0) {
				this.level()
					.addParticle(
						ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.7F, 0.7F, 0.5F),
						p + this.random.nextGaussian() * (double)s,
						q + this.random.nextGaussian() * (double)s,
						r + this.random.nextGaussian() * (double)s,
						0.0,
						0.0,
						0.0
					);
			}
		}

		if (this.getInvulnerableTicks() > 0) {
			float t = 3.3F * this.getScale();

			for (int u = 0; u < 3; u++) {
				this.level()
					.addParticle(
						ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.7F, 0.7F, 0.9F),
						this.getX() + this.random.nextGaussian(),
						this.getY() + (double)(this.random.nextFloat() * t),
						this.getZ() + this.random.nextGaussian(),
						0.0,
						0.0,
						0.0
					);
			}
		}
	}

	@Override
	protected void customServerAiStep() {
		if (this.getInvulnerableTicks() > 0) {
			int i = this.getInvulnerableTicks() - 1;
			this.bossEvent.setProgress(1.0F - (float)i / 220.0F);
			if (i <= 0) {
				this.level().explode(this, this.getX(), this.getEyeY(), this.getZ(), 7.0F, false, Level.ExplosionInteraction.MOB);
				if (!this.isSilent()) {
					this.level().globalLevelEvent(1023, this.blockPosition(), 0);
				}
			}

			this.setInvulnerableTicks(i);
			if (this.tickCount % 10 == 0) {
				this.heal(10.0F);
			}
		} else {
			super.customServerAiStep();

			for (int ix = 1; ix < 3; ix++) {
				if (this.tickCount >= this.nextHeadUpdate[ix - 1]) {
					this.nextHeadUpdate[ix - 1] = this.tickCount + 10 + this.random.nextInt(10);
					if ((this.level().getDifficulty() == Difficulty.NORMAL || this.level().getDifficulty() == Difficulty.HARD) && this.idleHeadUpdates[ix - 1]++ > 15) {
						float f = 10.0F;
						float g = 5.0F;
						double d = Mth.nextDouble(this.random, this.getX() - 10.0, this.getX() + 10.0);
						double e = Mth.nextDouble(this.random, this.getY() - 5.0, this.getY() + 5.0);
						double h = Mth.nextDouble(this.random, this.getZ() - 10.0, this.getZ() + 10.0);
						this.performRangedAttack(ix + 1, d, e, h, true);
						this.idleHeadUpdates[ix - 1] = 0;
					}

					int j = this.getAlternativeTarget(ix);
					if (j > 0) {
						LivingEntity livingEntity = (LivingEntity)this.level().getEntity(j);
						if (livingEntity != null && this.canAttack(livingEntity) && !(this.distanceToSqr(livingEntity) > 900.0) && this.hasLineOfSight(livingEntity)) {
							this.performRangedAttack(ix + 1, livingEntity);
							this.nextHeadUpdate[ix - 1] = this.tickCount + 40 + this.random.nextInt(20);
							this.idleHeadUpdates[ix - 1] = 0;
						} else {
							this.setAlternativeTarget(ix, 0);
						}
					} else {
						List<LivingEntity> list = this.level().getNearbyEntities(LivingEntity.class, TARGETING_CONDITIONS, this, this.getBoundingBox().inflate(20.0, 8.0, 20.0));
						if (!list.isEmpty()) {
							LivingEntity livingEntity2 = (LivingEntity)list.get(this.random.nextInt(list.size()));
							this.setAlternativeTarget(ix, livingEntity2.getId());
						}
					}
				}
			}

			if (this.getTarget() != null) {
				this.setAlternativeTarget(0, this.getTarget().getId());
			} else {
				this.setAlternativeTarget(0, 0);
			}

			if (this.destroyBlocksTick > 0) {
				this.destroyBlocksTick--;
				if (this.destroyBlocksTick == 0 && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
					boolean bl = false;
					int j = Mth.floor(this.getBbWidth() / 2.0F + 1.0F);
					int k = Mth.floor(this.getBbHeight());

					for (BlockPos blockPos : BlockPos.betweenClosed(
						this.getBlockX() - j, this.getBlockY(), this.getBlockZ() - j, this.getBlockX() + j, this.getBlockY() + k, this.getBlockZ() + j
					)) {
						BlockState blockState = this.level().getBlockState(blockPos);
						if (canDestroy(blockState)) {
							bl = this.level().destroyBlock(blockPos, true, this) || bl;
						}
					}

					if (bl) {
						this.level().levelEvent(null, 1022, this.blockPosition(), 0);
					}
				}
			}

			if (this.tickCount % 20 == 0) {
				this.heal(1.0F);
			}

			this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
		}
	}

	public static boolean canDestroy(BlockState blockState) {
		return !blockState.isAir() && !blockState.is(BlockTags.WITHER_IMMUNE);
	}

	public void makeInvulnerable() {
		this.setInvulnerableTicks(220);
		this.bossEvent.setProgress(0.0F);
		this.setHealth(this.getMaxHealth() / 3.0F);
	}

	@Override
	public void makeStuckInBlock(BlockState blockState, Vec3 vec3) {
	}

	@Override
	public void startSeenByPlayer(ServerPlayer serverPlayer) {
		super.startSeenByPlayer(serverPlayer);
		this.bossEvent.addPlayer(serverPlayer);
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer serverPlayer) {
		super.stopSeenByPlayer(serverPlayer);
		this.bossEvent.removePlayer(serverPlayer);
	}

	private double getHeadX(int i) {
		if (i <= 0) {
			return this.getX();
		} else {
			float f = (this.yBodyRot + (float)(180 * (i - 1))) * (float) (Math.PI / 180.0);
			float g = Mth.cos(f);
			return this.getX() + (double)g * 1.3 * (double)this.getScale();
		}
	}

	private double getHeadY(int i) {
		float f = i <= 0 ? 3.0F : 2.2F;
		return this.getY() + (double)(f * this.getScale());
	}

	private double getHeadZ(int i) {
		if (i <= 0) {
			return this.getZ();
		} else {
			float f = (this.yBodyRot + (float)(180 * (i - 1))) * (float) (Math.PI / 180.0);
			float g = Mth.sin(f);
			return this.getZ() + (double)g * 1.3 * (double)this.getScale();
		}
	}

	private float rotlerp(float f, float g, float h) {
		float i = Mth.wrapDegrees(g - f);
		if (i > h) {
			i = h;
		}

		if (i < -h) {
			i = -h;
		}

		return f + i;
	}

	private void performRangedAttack(int i, LivingEntity livingEntity) {
		this.performRangedAttack(
			i, livingEntity.getX(), livingEntity.getY() + (double)livingEntity.getEyeHeight() * 0.5, livingEntity.getZ(), i == 0 && this.random.nextFloat() < 0.001F
		);
	}

	private void performRangedAttack(int i, double d, double e, double f, boolean bl) {
		if (!this.isSilent()) {
			this.level().levelEvent(null, 1024, this.blockPosition(), 0);
		}

		double g = this.getHeadX(i);
		double h = this.getHeadY(i);
		double j = this.getHeadZ(i);
		double k = d - g;
		double l = e - h;
		double m = f - j;
		Vec3 vec3 = new Vec3(k, l, m);
		WitherSkull witherSkull = new WitherSkull(this.level(), this, vec3.normalize());
		witherSkull.setOwner(this);
		if (bl) {
			witherSkull.setDangerous(true);
		}

		witherSkull.setPos(g, h, j);
		this.level().addFreshEntity(witherSkull);
	}

	@Override
	public void performRangedAttack(LivingEntity livingEntity, float f) {
		this.performRangedAttack(0, livingEntity);
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (this.isInvulnerableTo(damageSource)) {
			return false;
		} else if (damageSource.is(DamageTypeTags.WITHER_IMMUNE_TO) || damageSource.getEntity() instanceof WitherBoss) {
			return false;
		} else if (this.getInvulnerableTicks() > 0 && !damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			return false;
		} else {
			if (this.isPowered()) {
				Entity entity = damageSource.getDirectEntity();
				if (entity instanceof AbstractArrow || entity instanceof WindCharge) {
					return false;
				}
			}

			Entity entity = damageSource.getEntity();
			if (entity != null && entity.getType().is(EntityTypeTags.WITHER_FRIENDS)) {
				return false;
			} else {
				if (this.destroyBlocksTick <= 0) {
					this.destroyBlocksTick = 20;
				}

				for (int i = 0; i < this.idleHeadUpdates.length; i++) {
					this.idleHeadUpdates[i] = this.idleHeadUpdates[i] + 3;
				}

				return super.hurt(damageSource, f);
			}
		}
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource damageSource, boolean bl) {
		super.dropCustomDeathLoot(serverLevel, damageSource, bl);
		ItemEntity itemEntity = this.spawnAtLocation(Items.NETHER_STAR);
		if (itemEntity != null) {
			itemEntity.setExtendedLifetime();
		}
	}

	@Override
	public void checkDespawn() {
		if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
			this.discard();
		} else {
			this.noActionTime = 0;
		}
	}

	@Override
	public boolean addEffect(MobEffectInstance mobEffectInstance, @Nullable Entity entity) {
		return false;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MAX_HEALTH, 300.0)
			.add(Attributes.MOVEMENT_SPEED, 0.6F)
			.add(Attributes.FLYING_SPEED, 0.6F)
			.add(Attributes.FOLLOW_RANGE, 40.0)
			.add(Attributes.ARMOR, 4.0);
	}

	public float[] getHeadYRots() {
		return this.yRotHeads;
	}

	public float[] getHeadXRots() {
		return this.xRotHeads;
	}

	public int getInvulnerableTicks() {
		return this.entityData.get(DATA_ID_INV);
	}

	public void setInvulnerableTicks(int i) {
		this.entityData.set(DATA_ID_INV, i);
	}

	public int getAlternativeTarget(int i) {
		return this.entityData.<Integer>get((EntityDataAccessor<Integer>)DATA_TARGETS.get(i));
	}

	public void setAlternativeTarget(int i, int j) {
		this.entityData.set((EntityDataAccessor<Integer>)DATA_TARGETS.get(i), j);
	}

	public boolean isPowered() {
		return this.getHealth() <= this.getMaxHealth() / 2.0F;
	}

	@Override
	protected boolean canRide(Entity entity) {
		return false;
	}

	@Override
	public boolean canUsePortal(boolean bl) {
		return false;
	}

	@Override
	public boolean canBeAffected(MobEffectInstance mobEffectInstance) {
		return mobEffectInstance.is(MobEffects.WITHER) ? false : super.canBeAffected(mobEffectInstance);
	}

	class WitherDoNothingGoal extends Goal {
		public WitherDoNothingGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			return WitherBoss.this.getInvulnerableTicks() > 0;
		}
	}
}
