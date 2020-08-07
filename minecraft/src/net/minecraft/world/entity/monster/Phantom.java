package net.minecraft.world.entity.monster;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class Phantom extends FlyingMob implements Enemy {
	private static final EntityDataAccessor<Integer> ID_SIZE = SynchedEntityData.defineId(Phantom.class, EntityDataSerializers.INT);
	private Vec3 moveTargetPoint = Vec3.ZERO;
	private BlockPos anchorPoint = BlockPos.ZERO;
	private Phantom.AttackPhase attackPhase = Phantom.AttackPhase.CIRCLE;

	public Phantom(EntityType<? extends Phantom> entityType, Level level) {
		super(entityType, level);
		this.xpReward = 5;
		this.moveControl = new Phantom.PhantomMoveControl(this);
		this.lookControl = new Phantom.PhantomLookControl(this);
	}

	@Override
	protected BodyRotationControl createBodyControl() {
		return new Phantom.PhantomBodyRotationControl(this);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new Phantom.PhantomAttackStrategyGoal());
		this.goalSelector.addGoal(2, new Phantom.PhantomSweepAttackGoal());
		this.goalSelector.addGoal(3, new Phantom.PhantomCircleAroundAnchorGoal());
		this.targetSelector.addGoal(1, new Phantom.PhantomAttackPlayerTargetGoal());
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(ID_SIZE, 0);
	}

	public void setPhantomSize(int i) {
		this.entityData.set(ID_SIZE, Mth.clamp(i, 0, 64));
	}

	private void updatePhantomSizeInfo() {
		this.refreshDimensions();
		this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double)(6 + this.getPhantomSize()));
	}

	public int getPhantomSize() {
		return this.entityData.get(ID_SIZE);
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return entityDimensions.height * 0.35F;
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		if (ID_SIZE.equals(entityDataAccessor)) {
			this.updatePhantomSizeInfo();
		}

		super.onSyncedDataUpdated(entityDataAccessor);
	}

	@Override
	protected boolean shouldDespawnInPeaceful() {
		return true;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level.isClientSide) {
			float f = Mth.cos((float)(this.getId() * 3 + this.tickCount) * 0.13F + (float) Math.PI);
			float g = Mth.cos((float)(this.getId() * 3 + this.tickCount + 1) * 0.13F + (float) Math.PI);
			if (f > 0.0F && g <= 0.0F) {
				this.level
					.playLocalSound(
						this.getX(),
						this.getY(),
						this.getZ(),
						SoundEvents.PHANTOM_FLAP,
						this.getSoundSource(),
						0.95F + this.random.nextFloat() * 0.05F,
						0.95F + this.random.nextFloat() * 0.05F,
						false
					);
			}

			int i = this.getPhantomSize();
			float h = Mth.cos(this.yRot * (float) (Math.PI / 180.0)) * (1.3F + 0.21F * (float)i);
			float j = Mth.sin(this.yRot * (float) (Math.PI / 180.0)) * (1.3F + 0.21F * (float)i);
			float k = (0.3F + f * 0.45F) * ((float)i * 0.2F + 1.0F);
			this.level.addParticle(ParticleTypes.MYCELIUM, this.getX() + (double)h, this.getY() + (double)k, this.getZ() + (double)j, 0.0, 0.0, 0.0);
			this.level.addParticle(ParticleTypes.MYCELIUM, this.getX() - (double)h, this.getY() + (double)k, this.getZ() - (double)j, 0.0, 0.0, 0.0);
		}
	}

	@Override
	public void aiStep() {
		if (this.isAlive() && this.isSunBurnTick()) {
			this.setSecondsOnFire(8);
		}

		super.aiStep();
	}

	@Override
	protected void customServerAiStep() {
		super.customServerAiStep();
	}

	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor,
		DifficultyInstance difficultyInstance,
		MobSpawnType mobSpawnType,
		@Nullable SpawnGroupData spawnGroupData,
		@Nullable CompoundTag compoundTag
	) {
		this.anchorPoint = this.blockPosition().above(5);
		this.setPhantomSize(0);
		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.contains("AX")) {
			this.anchorPoint = new BlockPos(compoundTag.getInt("AX"), compoundTag.getInt("AY"), compoundTag.getInt("AZ"));
		}

		this.setPhantomSize(compoundTag.getInt("Size"));
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("AX", this.anchorPoint.getX());
		compoundTag.putInt("AY", this.anchorPoint.getY());
		compoundTag.putInt("AZ", this.anchorPoint.getZ());
		compoundTag.putInt("Size", this.getPhantomSize());
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean shouldRenderAtSqrDistance(double d) {
		return true;
	}

	@Override
	public SoundSource getSoundSource() {
		return SoundSource.HOSTILE;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.PHANTOM_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.PHANTOM_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.PHANTOM_DEATH;
	}

	@Override
	public MobType getMobType() {
		return MobType.UNDEAD;
	}

	@Override
	protected float getSoundVolume() {
		return 1.0F;
	}

	@Override
	public boolean canAttackType(EntityType<?> entityType) {
		return true;
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		int i = this.getPhantomSize();
		EntityDimensions entityDimensions = super.getDimensions(pose);
		float f = (entityDimensions.width + 0.2F * (float)i) / entityDimensions.width;
		return entityDimensions.scale(f);
	}

	static enum AttackPhase {
		CIRCLE,
		SWOOP;
	}

	class PhantomAttackPlayerTargetGoal extends Goal {
		private final TargetingConditions attackTargeting = new TargetingConditions().range(64.0);
		private int nextScanTick = 20;

		private PhantomAttackPlayerTargetGoal() {
		}

		@Override
		public boolean canUse() {
			if (this.nextScanTick > 0) {
				this.nextScanTick--;
				return false;
			} else {
				this.nextScanTick = 60;
				List<Player> list = Phantom.this.level.getNearbyPlayers(this.attackTargeting, Phantom.this, Phantom.this.getBoundingBox().inflate(16.0, 64.0, 16.0));
				if (!list.isEmpty()) {
					list.sort(Comparator.comparing(Entity::getY).reversed());

					for (Player player : list) {
						if (Phantom.this.canAttack(player, TargetingConditions.DEFAULT)) {
							Phantom.this.setTarget(player);
							return true;
						}
					}
				}

				return false;
			}
		}

		@Override
		public boolean canContinueToUse() {
			LivingEntity livingEntity = Phantom.this.getTarget();
			return livingEntity != null ? Phantom.this.canAttack(livingEntity, TargetingConditions.DEFAULT) : false;
		}
	}

	class PhantomAttackStrategyGoal extends Goal {
		private int nextSweepTick;

		private PhantomAttackStrategyGoal() {
		}

		@Override
		public boolean canUse() {
			LivingEntity livingEntity = Phantom.this.getTarget();
			return livingEntity != null ? Phantom.this.canAttack(Phantom.this.getTarget(), TargetingConditions.DEFAULT) : false;
		}

		@Override
		public void start() {
			this.nextSweepTick = 10;
			Phantom.this.attackPhase = Phantom.AttackPhase.CIRCLE;
			this.setAnchorAboveTarget();
		}

		@Override
		public void stop() {
			Phantom.this.anchorPoint = Phantom.this.level
				.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, Phantom.this.anchorPoint)
				.above(10 + Phantom.this.random.nextInt(20));
		}

		@Override
		public void tick() {
			if (Phantom.this.attackPhase == Phantom.AttackPhase.CIRCLE) {
				this.nextSweepTick--;
				if (this.nextSweepTick <= 0) {
					Phantom.this.attackPhase = Phantom.AttackPhase.SWOOP;
					this.setAnchorAboveTarget();
					this.nextSweepTick = (8 + Phantom.this.random.nextInt(4)) * 20;
					Phantom.this.playSound(SoundEvents.PHANTOM_SWOOP, 10.0F, 0.95F + Phantom.this.random.nextFloat() * 0.1F);
				}
			}
		}

		private void setAnchorAboveTarget() {
			Phantom.this.anchorPoint = Phantom.this.getTarget().blockPosition().above(20 + Phantom.this.random.nextInt(20));
			if (Phantom.this.anchorPoint.getY() < Phantom.this.level.getSeaLevel()) {
				Phantom.this.anchorPoint = new BlockPos(Phantom.this.anchorPoint.getX(), Phantom.this.level.getSeaLevel() + 1, Phantom.this.anchorPoint.getZ());
			}
		}
	}

	class PhantomBodyRotationControl extends BodyRotationControl {
		public PhantomBodyRotationControl(Mob mob) {
			super(mob);
		}

		@Override
		public void clientTick() {
			Phantom.this.yHeadRot = Phantom.this.yBodyRot;
			Phantom.this.yBodyRot = Phantom.this.yRot;
		}
	}

	class PhantomCircleAroundAnchorGoal extends Phantom.PhantomMoveTargetGoal {
		private float angle;
		private float distance;
		private float height;
		private float clockwise;

		private PhantomCircleAroundAnchorGoal() {
		}

		@Override
		public boolean canUse() {
			return Phantom.this.getTarget() == null || Phantom.this.attackPhase == Phantom.AttackPhase.CIRCLE;
		}

		@Override
		public void start() {
			this.distance = 5.0F + Phantom.this.random.nextFloat() * 10.0F;
			this.height = -4.0F + Phantom.this.random.nextFloat() * 9.0F;
			this.clockwise = Phantom.this.random.nextBoolean() ? 1.0F : -1.0F;
			this.selectNext();
		}

		@Override
		public void tick() {
			if (Phantom.this.random.nextInt(350) == 0) {
				this.height = -4.0F + Phantom.this.random.nextFloat() * 9.0F;
			}

			if (Phantom.this.random.nextInt(250) == 0) {
				this.distance++;
				if (this.distance > 15.0F) {
					this.distance = 5.0F;
					this.clockwise = -this.clockwise;
				}
			}

			if (Phantom.this.random.nextInt(450) == 0) {
				this.angle = Phantom.this.random.nextFloat() * 2.0F * (float) Math.PI;
				this.selectNext();
			}

			if (this.touchingTarget()) {
				this.selectNext();
			}

			if (Phantom.this.moveTargetPoint.y < Phantom.this.getY() && !Phantom.this.level.isEmptyBlock(Phantom.this.blockPosition().below(1))) {
				this.height = Math.max(1.0F, this.height);
				this.selectNext();
			}

			if (Phantom.this.moveTargetPoint.y > Phantom.this.getY() && !Phantom.this.level.isEmptyBlock(Phantom.this.blockPosition().above(1))) {
				this.height = Math.min(-1.0F, this.height);
				this.selectNext();
			}
		}

		private void selectNext() {
			if (BlockPos.ZERO.equals(Phantom.this.anchorPoint)) {
				Phantom.this.anchorPoint = Phantom.this.blockPosition();
			}

			this.angle = this.angle + this.clockwise * 15.0F * (float) (Math.PI / 180.0);
			Phantom.this.moveTargetPoint = Vec3.atLowerCornerOf(Phantom.this.anchorPoint)
				.add((double)(this.distance * Mth.cos(this.angle)), (double)(-4.0F + this.height), (double)(this.distance * Mth.sin(this.angle)));
		}
	}

	class PhantomLookControl extends LookControl {
		public PhantomLookControl(Mob mob) {
			super(mob);
		}

		@Override
		public void tick() {
		}
	}

	class PhantomMoveControl extends MoveControl {
		private float speed = 0.1F;

		public PhantomMoveControl(Mob mob) {
			super(mob);
		}

		@Override
		public void tick() {
			if (Phantom.this.horizontalCollision) {
				Phantom.this.yRot += 180.0F;
				this.speed = 0.1F;
			}

			float f = (float)(Phantom.this.moveTargetPoint.x - Phantom.this.getX());
			float g = (float)(Phantom.this.moveTargetPoint.y - Phantom.this.getY());
			float h = (float)(Phantom.this.moveTargetPoint.z - Phantom.this.getZ());
			double d = (double)Mth.sqrt(f * f + h * h);
			double e = 1.0 - (double)Mth.abs(g * 0.7F) / d;
			f = (float)((double)f * e);
			h = (float)((double)h * e);
			d = (double)Mth.sqrt(f * f + h * h);
			double i = (double)Mth.sqrt(f * f + h * h + g * g);
			float j = Phantom.this.yRot;
			float k = (float)Mth.atan2((double)h, (double)f);
			float l = Mth.wrapDegrees(Phantom.this.yRot + 90.0F);
			float m = Mth.wrapDegrees(k * (180.0F / (float)Math.PI));
			Phantom.this.yRot = Mth.approachDegrees(l, m, 4.0F) - 90.0F;
			Phantom.this.yBodyRot = Phantom.this.yRot;
			if (Mth.degreesDifferenceAbs(j, Phantom.this.yRot) < 3.0F) {
				this.speed = Mth.approach(this.speed, 1.8F, 0.005F * (1.8F / this.speed));
			} else {
				this.speed = Mth.approach(this.speed, 0.2F, 0.025F);
			}

			float n = (float)(-(Mth.atan2((double)(-g), d) * 180.0F / (float)Math.PI));
			Phantom.this.xRot = n;
			float o = Phantom.this.yRot + 90.0F;
			double p = (double)(this.speed * Mth.cos(o * (float) (Math.PI / 180.0))) * Math.abs((double)f / i);
			double q = (double)(this.speed * Mth.sin(o * (float) (Math.PI / 180.0))) * Math.abs((double)h / i);
			double r = (double)(this.speed * Mth.sin(n * (float) (Math.PI / 180.0))) * Math.abs((double)g / i);
			Vec3 vec3 = Phantom.this.getDeltaMovement();
			Phantom.this.setDeltaMovement(vec3.add(new Vec3(p, r, q).subtract(vec3).scale(0.2)));
		}
	}

	abstract class PhantomMoveTargetGoal extends Goal {
		public PhantomMoveTargetGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		protected boolean touchingTarget() {
			return Phantom.this.moveTargetPoint.distanceToSqr(Phantom.this.getX(), Phantom.this.getY(), Phantom.this.getZ()) < 4.0;
		}
	}

	class PhantomSweepAttackGoal extends Phantom.PhantomMoveTargetGoal {
		private PhantomSweepAttackGoal() {
		}

		@Override
		public boolean canUse() {
			return Phantom.this.getTarget() != null && Phantom.this.attackPhase == Phantom.AttackPhase.SWOOP;
		}

		@Override
		public boolean canContinueToUse() {
			LivingEntity livingEntity = Phantom.this.getTarget();
			if (livingEntity == null) {
				return false;
			} else if (!livingEntity.isAlive()) {
				return false;
			} else if (!(livingEntity instanceof Player) || !((Player)livingEntity).isSpectator() && !((Player)livingEntity).isCreative()) {
				if (!this.canUse()) {
					return false;
				} else {
					if (Phantom.this.tickCount % 20 == 0) {
						List<Cat> list = Phantom.this.level.getEntitiesOfClass(Cat.class, Phantom.this.getBoundingBox().inflate(16.0), EntitySelector.ENTITY_STILL_ALIVE);
						if (!list.isEmpty()) {
							for (Cat cat : list) {
								cat.hiss();
							}

							return false;
						}
					}

					return true;
				}
			} else {
				return false;
			}
		}

		@Override
		public void start() {
		}

		@Override
		public void stop() {
			Phantom.this.setTarget(null);
			Phantom.this.attackPhase = Phantom.AttackPhase.CIRCLE;
		}

		@Override
		public void tick() {
			LivingEntity livingEntity = Phantom.this.getTarget();
			Phantom.this.moveTargetPoint = new Vec3(livingEntity.getX(), livingEntity.getY(0.5), livingEntity.getZ());
			if (Phantom.this.getBoundingBox().inflate(0.2F).intersects(livingEntity.getBoundingBox())) {
				Phantom.this.doHurtTarget(livingEntity);
				Phantom.this.attackPhase = Phantom.AttackPhase.CIRCLE;
				if (!Phantom.this.isSilent()) {
					Phantom.this.level.levelEvent(1039, Phantom.this.blockPosition(), 0);
				}
			} else if (Phantom.this.horizontalCollision || Phantom.this.hurtTime > 0) {
				Phantom.this.attackPhase = Phantom.AttackPhase.CIRCLE;
			}
		}
	}
}
