package net.minecraft.world.entity.boss.wither;

import com.google.common.collect.ImmutableList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.fabricmc.api.EnvironmentInterfaces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@EnvironmentInterfaces({@EnvironmentInterface(
		value = EnvType.CLIENT,
		itf = PowerableMob.class
	)})
public class WitherBoss extends Monster implements PowerableMob, RangedAttackMob {
	private static final EntityDataAccessor<Integer> DATA_TARGET_A = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_TARGET_B = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_TARGET_C = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
	private static final List<EntityDataAccessor<Integer>> DATA_TARGETS = ImmutableList.of(DATA_TARGET_A, DATA_TARGET_B, DATA_TARGET_C);
	private static final EntityDataAccessor<Integer> DATA_ID_INV = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
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
	private static final Predicate<LivingEntity> LIVING_ENTITY_SELECTOR = livingEntity -> livingEntity.getMobType() != MobType.UNDEAD && livingEntity.attackable();
	private static final TargetingConditions TARGETING_CONDITIONS = new TargetingConditions().range(20.0).selector(LIVING_ENTITY_SELECTOR);

	public WitherBoss(EntityType<? extends WitherBoss> entityType, Level level) {
		super(entityType, level);
		this.setHealth(this.getMaxHealth());
		this.getNavigation().setCanFloat(true);
		this.xpReward = 50;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new WitherBoss.WitherDoNothingGoal());
		this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 40, 20.0F));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Mob.class, 0, false, false, LIVING_ENTITY_SELECTOR));
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_TARGET_A, 0);
		this.entityData.define(DATA_TARGET_B, 0);
		this.entityData.define(DATA_TARGET_C, 0);
		this.entityData.define(DATA_ID_INV, 0);
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
		if (!this.level.isClientSide && this.getAlternativeTarget(0) > 0) {
			Entity entity = this.level.getEntity(this.getAlternativeTarget(0));
			if (entity != null) {
				double d = vec3.y;
				if (this.getY() < entity.getY() || !this.isPowered() && this.getY() < entity.getY() + 5.0) {
					d = Math.max(0.0, d);
					d += 0.3 - d * 0.6F;
				}

				vec3 = new Vec3(vec3.x, d, vec3.z);
				Vec3 vec32 = new Vec3(entity.getX() - this.getX(), 0.0, entity.getZ() - this.getZ());
				if (getHorizontalDistanceSqr(vec32) > 9.0) {
					Vec3 vec33 = vec32.normalize();
					vec3 = vec3.add(vec33.x * 0.3 - vec3.x * 0.6, 0.0, vec33.z * 0.3 - vec3.z * 0.6);
				}
			}
		}

		this.setDeltaMovement(vec3);
		if (getHorizontalDistanceSqr(vec3) > 0.05) {
			this.yRot = (float)Mth.atan2(vec3.z, vec3.x) * (180.0F / (float)Math.PI) - 90.0F;
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
				entity2 = this.level.getEntity(j);
			}

			if (entity2 != null) {
				double e = this.getHeadX(i + 1);
				double f = this.getHeadY(i + 1);
				double g = this.getHeadZ(i + 1);
				double h = entity2.getX() - e;
				double k = entity2.getEyeY() - f;
				double l = entity2.getZ() - g;
				double m = (double)Mth.sqrt(h * h + l * l);
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
			this.level
				.addParticle(
					ParticleTypes.SMOKE, p + this.random.nextGaussian() * 0.3F, q + this.random.nextGaussian() * 0.3F, r + this.random.nextGaussian() * 0.3F, 0.0, 0.0, 0.0
				);
			if (bl && this.level.random.nextInt(4) == 0) {
				this.level
					.addParticle(
						ParticleTypes.ENTITY_EFFECT,
						p + this.random.nextGaussian() * 0.3F,
						q + this.random.nextGaussian() * 0.3F,
						r + this.random.nextGaussian() * 0.3F,
						0.7F,
						0.7F,
						0.5
					);
			}
		}

		if (this.getInvulnerableTicks() > 0) {
			for (int jxx = 0; jxx < 3; jxx++) {
				this.level
					.addParticle(
						ParticleTypes.ENTITY_EFFECT,
						this.getX() + this.random.nextGaussian(),
						this.getY() + (double)(this.random.nextFloat() * 3.3F),
						this.getZ() + this.random.nextGaussian(),
						0.7F,
						0.7F,
						0.9F
					);
			}
		}
	}

	@Override
	protected void customServerAiStep() {
		if (this.getInvulnerableTicks() > 0) {
			int i = this.getInvulnerableTicks() - 1;
			if (i <= 0) {
				Explosion.BlockInteraction blockInteraction = this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
					? Explosion.BlockInteraction.DESTROY
					: Explosion.BlockInteraction.NONE;
				this.level.explode(this, this.getX(), this.getEyeY(), this.getZ(), 7.0F, false, blockInteraction);
				if (!this.isSilent()) {
					this.level.globalLevelEvent(1023, this.blockPosition(), 0);
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
					if ((this.level.getDifficulty() == Difficulty.NORMAL || this.level.getDifficulty() == Difficulty.HARD) && this.idleHeadUpdates[ix - 1]++ > 15) {
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
						Entity entity = this.level.getEntity(j);
						if (entity == null || !entity.isAlive() || this.distanceToSqr(entity) > 900.0 || !this.canSee(entity)) {
							this.setAlternativeTarget(ix, 0);
						} else if (entity instanceof Player && ((Player)entity).abilities.invulnerable) {
							this.setAlternativeTarget(ix, 0);
						} else {
							this.performRangedAttack(ix + 1, (LivingEntity)entity);
							this.nextHeadUpdate[ix - 1] = this.tickCount + 40 + this.random.nextInt(20);
							this.idleHeadUpdates[ix - 1] = 0;
						}
					} else {
						List<LivingEntity> list = this.level.getNearbyEntities(LivingEntity.class, TARGETING_CONDITIONS, this, this.getBoundingBox().inflate(20.0, 8.0, 20.0));

						for (int k = 0; k < 10 && !list.isEmpty(); k++) {
							LivingEntity livingEntity = (LivingEntity)list.get(this.random.nextInt(list.size()));
							if (livingEntity != this && livingEntity.isAlive() && this.canSee(livingEntity)) {
								if (livingEntity instanceof Player) {
									if (!((Player)livingEntity).abilities.invulnerable) {
										this.setAlternativeTarget(ix, livingEntity.getId());
									}
								} else {
									this.setAlternativeTarget(ix, livingEntity.getId());
								}
								break;
							}

							list.remove(livingEntity);
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
				if (this.destroyBlocksTick == 0 && this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
					int ixx = Mth.floor(this.getY());
					int j = Mth.floor(this.getX());
					int l = Mth.floor(this.getZ());
					boolean bl = false;

					for (int m = -1; m <= 1; m++) {
						for (int n = -1; n <= 1; n++) {
							for (int o = 0; o <= 3; o++) {
								int p = j + m;
								int q = ixx + o;
								int r = l + n;
								BlockPos blockPos = new BlockPos(p, q, r);
								BlockState blockState = this.level.getBlockState(blockPos);
								if (canDestroy(blockState)) {
									bl = this.level.destroyBlock(blockPos, true, this) || bl;
								}
							}
						}
					}

					if (bl) {
						this.level.levelEvent(null, 1022, this.blockPosition(), 0);
					}
				}
			}

			if (this.tickCount % 20 == 0) {
				this.heal(1.0F);
			}

			this.bossEvent.setPercent(this.getHealth() / this.getMaxHealth());
		}
	}

	public static boolean canDestroy(BlockState blockState) {
		return !blockState.isAir() && !BlockTags.WITHER_IMMUNE.contains(blockState.getBlock());
	}

	public void makeInvulnerable() {
		this.setInvulnerableTicks(220);
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
			return this.getX() + (double)g * 1.3;
		}
	}

	private double getHeadY(int i) {
		return i <= 0 ? this.getY() + 3.0 : this.getY() + 2.2;
	}

	private double getHeadZ(int i) {
		if (i <= 0) {
			return this.getZ();
		} else {
			float f = (this.yBodyRot + (float)(180 * (i - 1))) * (float) (Math.PI / 180.0);
			float g = Mth.sin(f);
			return this.getZ() + (double)g * 1.3;
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
			this.level.levelEvent(null, 1024, this.blockPosition(), 0);
		}

		double g = this.getHeadX(i);
		double h = this.getHeadY(i);
		double j = this.getHeadZ(i);
		double k = d - g;
		double l = e - h;
		double m = f - j;
		WitherSkull witherSkull = new WitherSkull(this.level, this, k, l, m);
		if (bl) {
			witherSkull.setDangerous(true);
		}

		witherSkull.setPosRaw(g, h, j);
		this.level.addFreshEntity(witherSkull);
	}

	@Override
	public void performRangedAttack(LivingEntity livingEntity, float f) {
		this.performRangedAttack(0, livingEntity);
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (this.isInvulnerableTo(damageSource)) {
			return false;
		} else if (damageSource == DamageSource.DROWN || damageSource.getEntity() instanceof WitherBoss) {
			return false;
		} else if (this.getInvulnerableTicks() > 0 && damageSource != DamageSource.OUT_OF_WORLD) {
			return false;
		} else {
			if (this.isPowered()) {
				Entity entity = damageSource.getDirectEntity();
				if (entity instanceof AbstractArrow) {
					return false;
				}
			}

			Entity entity = damageSource.getEntity();
			if (entity != null && !(entity instanceof Player) && entity instanceof LivingEntity && ((LivingEntity)entity).getMobType() == this.getMobType()) {
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
	protected void dropCustomDeathLoot(DamageSource damageSource, int i, boolean bl) {
		super.dropCustomDeathLoot(damageSource, i, bl);
		ItemEntity itemEntity = this.spawnAtLocation(Items.NETHER_STAR);
		if (itemEntity != null) {
			itemEntity.setExtendedLifetime();
		}
	}

	@Override
	public void checkDespawn() {
		if (this.level.getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
			this.remove();
		} else {
			this.noActionTime = 0;
		}
	}

	@Override
	public boolean causeFallDamage(float f, float g) {
		return false;
	}

	@Override
	public boolean addEffect(MobEffectInstance mobEffectInstance) {
		return false;
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(300.0);
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.6F);
		this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0);
		this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(4.0);
	}

	@Environment(EnvType.CLIENT)
	public float getHeadYRot(int i) {
		return this.yRotHeads[i];
	}

	@Environment(EnvType.CLIENT)
	public float getHeadXRot(int i) {
		return this.xRotHeads[i];
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

	@Override
	public boolean isPowered() {
		return this.getHealth() <= this.getMaxHealth() / 2.0F;
	}

	@Override
	public MobType getMobType() {
		return MobType.UNDEAD;
	}

	@Override
	protected boolean canRide(Entity entity) {
		return false;
	}

	@Override
	public boolean canChangeDimensions() {
		return false;
	}

	@Override
	public boolean canBeAffected(MobEffectInstance mobEffectInstance) {
		return mobEffectInstance.getEffect() == MobEffects.WITHER ? false : super.canBeAffected(mobEffectInstance);
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
