package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Shulker extends AbstractGolem implements Enemy {
	private static final UUID COVERED_ARMOR_MODIFIER_UUID = UUID.fromString("7E0292F2-9434-48D5-A29F-9583AF7DF27F");
	private static final AttributeModifier COVERED_ARMOR_MODIFIER = new AttributeModifier(
		COVERED_ARMOR_MODIFIER_UUID, "Covered armor bonus", 20.0, AttributeModifier.Operation.ADDITION
	);
	protected static final EntityDataAccessor<Direction> DATA_ATTACH_FACE_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.DIRECTION);
	protected static final EntityDataAccessor<Byte> DATA_PEEK_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.BYTE);
	protected static final EntityDataAccessor<Byte> DATA_COLOR_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.BYTE);
	private float currentPeekAmountO;
	private float currentPeekAmount;
	@Nullable
	private BlockPos clientOldAttachPosition;
	private int clientSideTeleportInterpolation;

	public Shulker(EntityType<? extends Shulker> entityType, Level level) {
		super(entityType, level);
		this.xpReward = 5;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(4, new Shulker.ShulkerAttackGoal());
		this.goalSelector.addGoal(7, new Shulker.ShulkerPeekGoal());
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this, this.getClass()).setAlertOthers());
		this.targetSelector.addGoal(2, new Shulker.ShulkerNearestAttackGoal(this));
		this.targetSelector.addGoal(3, new Shulker.ShulkerDefenseAttackGoal(this));
	}

	@Override
	protected boolean isMovementNoisy() {
		return false;
	}

	@Override
	public SoundSource getSoundSource() {
		return SoundSource.HOSTILE;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.SHULKER_AMBIENT;
	}

	@Override
	public void playAmbientSound() {
		if (!this.isClosed()) {
			super.playAmbientSound();
		}
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.SHULKER_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return this.isClosed() ? SoundEvents.SHULKER_HURT_CLOSED : SoundEvents.SHULKER_HURT;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_ATTACH_FACE_ID, Direction.DOWN);
		this.entityData.define(DATA_PEEK_ID, (byte)0);
		this.entityData.define(DATA_COLOR_ID, (byte)16);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0);
	}

	@Override
	protected BodyRotationControl createBodyControl() {
		return new Shulker.ShulkerBodyRotationControl(this);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.entityData.set(DATA_ATTACH_FACE_ID, Direction.from3DDataValue(compoundTag.getByte("AttachFace")));
		this.entityData.set(DATA_PEEK_ID, compoundTag.getByte("Peek"));
		if (compoundTag.contains("Color", 99)) {
			this.entityData.set(DATA_COLOR_ID, compoundTag.getByte("Color"));
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putByte("AttachFace", (byte)this.entityData.get(DATA_ATTACH_FACE_ID).get3DDataValue());
		compoundTag.putByte("Peek", this.entityData.get(DATA_PEEK_ID));
		compoundTag.putByte("Color", this.entityData.get(DATA_COLOR_ID));
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.level.isClientSide && !this.isPassenger() && !this.canStayAt(this.blockPosition(), this.getAttachFace())) {
			this.findNewAttachment();
		}

		if (this.updatePeekAmount()) {
			this.onPeekAmountChange();
		}

		if (this.level.isClientSide) {
			if (this.clientSideTeleportInterpolation > 0) {
				this.clientSideTeleportInterpolation--;
			} else {
				this.clientOldAttachPosition = null;
			}
		}
	}

	private void findNewAttachment() {
		Direction direction = this.findAttachableSurface(this.blockPosition());
		if (direction != null) {
			this.entityData.set(DATA_ATTACH_FACE_ID, direction);
		} else {
			this.teleportSomewhere();
		}
	}

	@Override
	protected AABB makeBoundingBox() {
		float f = getPhysicalPeek(this.currentPeekAmount);
		Direction direction = this.getAttachFace().getOpposite();
		float g = this.getType().getWidth() / 2.0F;
		return getProgressAabb(direction, f).move(this.getX() - (double)g, this.getY(), this.getZ() - (double)g);
	}

	private static float getPhysicalPeek(float f) {
		return 0.5F - Mth.sin((0.5F + f) * (float) Math.PI) * 0.5F;
	}

	private boolean updatePeekAmount() {
		this.currentPeekAmountO = this.currentPeekAmount;
		float f = (float)this.getRawPeekAmount() * 0.01F;
		if (this.currentPeekAmount == f) {
			return false;
		} else {
			if (this.currentPeekAmount > f) {
				this.currentPeekAmount = Mth.clamp(this.currentPeekAmount - 0.05F, f, 1.0F);
			} else {
				this.currentPeekAmount = Mth.clamp(this.currentPeekAmount + 0.05F, 0.0F, f);
			}

			return true;
		}
	}

	private void onPeekAmountChange() {
		this.reapplyPosition();
		float f = getPhysicalPeek(this.currentPeekAmount);
		float g = getPhysicalPeek(this.currentPeekAmountO);
		Direction direction = this.getAttachFace().getOpposite();
		float h = f - g;
		if (!(h <= 0.0F)) {
			for (Entity entity : this.level
				.getEntities(
					this,
					getProgressDeltaAabb(direction, g, f).move(this.getX(), this.getY(), this.getZ()),
					EntitySelector.NO_SPECTATORS.and(entityx -> !entityx.isPassengerOfSameVehicle(this))
				)) {
				if (!(entity instanceof Shulker) && !entity.noPhysics) {
					entity.move(
						MoverType.SHULKER,
						new Vec3((double)(h * (float)direction.getStepX()), (double)(h * (float)direction.getStepY()), (double)(h * (float)direction.getStepZ()))
					);
				}
			}
		}
	}

	public static AABB getProgressAabb(Direction direction, float f) {
		return getProgressDeltaAabb(direction, -1.0F, f);
	}

	public static AABB getProgressDeltaAabb(Direction direction, float f, float g) {
		double d = (double)Math.max(f, g);
		double e = (double)Math.min(f, g);
		return new AABB(BlockPos.ZERO)
			.expandTowards((double)direction.getStepX() * d, (double)direction.getStepY() * d, (double)direction.getStepZ() * d)
			.contract((double)(-direction.getStepX()) * (1.0 + e), (double)(-direction.getStepY()) * (1.0 + e), (double)(-direction.getStepZ()) * (1.0 + e));
	}

	@Override
	public double getMyRidingOffset() {
		return 0.1875 - this.getVehicle().getPassengersRidingOffset();
	}

	@Override
	public boolean startRiding(Entity entity, boolean bl) {
		if (this.level.isClientSide()) {
			this.clientOldAttachPosition = null;
			this.clientSideTeleportInterpolation = 0;
		}

		this.entityData.set(DATA_ATTACH_FACE_ID, Direction.DOWN);
		return super.startRiding(entity, bl);
	}

	@Override
	public void stopRiding() {
		super.stopRiding();
		if (this.level.isClientSide) {
			this.clientOldAttachPosition = this.blockPosition();
		}

		this.yBodyRotO = 0.0F;
		this.yBodyRot = 0.0F;
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor,
		DifficultyInstance difficultyInstance,
		MobSpawnType mobSpawnType,
		@Nullable SpawnGroupData spawnGroupData,
		@Nullable CompoundTag compoundTag
	) {
		this.yRot = 0.0F;
		this.yHeadRot = this.yRot;
		this.setOldPosAndRot();
		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
	}

	@Override
	public void move(MoverType moverType, Vec3 vec3) {
		if (moverType == MoverType.SHULKER_BOX) {
			this.teleportSomewhere();
		} else {
			super.move(moverType, vec3);
		}
	}

	@Override
	public Vec3 getDeltaMovement() {
		return Vec3.ZERO;
	}

	@Override
	public void setDeltaMovement(Vec3 vec3) {
	}

	@Override
	public void setPos(double d, double e, double f) {
		BlockPos blockPos = this.blockPosition();
		if (this.isPassenger()) {
			super.setPos(d, e, f);
		} else {
			super.setPos((double)Mth.floor(d) + 0.5, (double)Mth.floor(e + 0.5), (double)Mth.floor(f) + 0.5);
		}

		if (this.tickCount != 0) {
			BlockPos blockPos2 = this.blockPosition();
			if (!blockPos2.equals(blockPos)) {
				this.entityData.set(DATA_PEEK_ID, (byte)0);
				this.hasImpulse = true;
				if (this.level.isClientSide && !this.isPassenger() && !blockPos2.equals(this.clientOldAttachPosition)) {
					this.clientOldAttachPosition = blockPos;
					this.clientSideTeleportInterpolation = 6;
					this.xOld = this.getX();
					this.yOld = this.getY();
					this.zOld = this.getZ();
				}
			}
		}
	}

	@Nullable
	protected Direction findAttachableSurface(BlockPos blockPos) {
		for (Direction direction : Direction.values()) {
			if (this.canStayAt(blockPos, direction)) {
				return direction;
			}
		}

		return null;
	}

	private boolean canStayAt(BlockPos blockPos, Direction direction) {
		if (this.isPositionBlocked(blockPos)) {
			return false;
		} else {
			Direction direction2 = direction.getOpposite();
			if (!this.level.loadedAndEntityCanStandOnFace(blockPos.relative(direction), this, direction2)) {
				return false;
			} else {
				AABB aABB = getProgressAabb(direction2, 1.0F).move(blockPos).deflate(1.0E-6);
				return this.level.noCollision(this, aABB);
			}
		}
	}

	private boolean isPositionBlocked(BlockPos blockPos) {
		BlockState blockState = this.level.getBlockState(blockPos);
		if (blockState.isAir()) {
			return false;
		} else {
			boolean bl = blockState.is(Blocks.MOVING_PISTON) && blockPos.equals(this.blockPosition());
			return !bl;
		}
	}

	protected boolean teleportSomewhere() {
		if (!this.isNoAi() && this.isAlive()) {
			BlockPos blockPos = this.blockPosition();

			for (int i = 0; i < 5; i++) {
				BlockPos blockPos2 = blockPos.offset(
					Mth.randomBetweenInclusive(this.random, -8, 8), Mth.randomBetweenInclusive(this.random, -8, 8), Mth.randomBetweenInclusive(this.random, -8, 8)
				);
				if (blockPos2.getY() > this.level.getMinBuildHeight()
					&& this.level.isEmptyBlock(blockPos2)
					&& this.level.getWorldBorder().isWithinBounds(blockPos2)
					&& this.level.noCollision(this, new AABB(blockPos2).deflate(1.0E-6))) {
					Direction direction = this.findAttachableSurface(blockPos2);
					if (direction != null) {
						this.unRide();
						this.entityData.set(DATA_ATTACH_FACE_ID, direction);
						this.playSound(SoundEvents.SHULKER_TELEPORT, 1.0F, 1.0F);
						this.setPos((double)blockPos2.getX() + 0.5, (double)blockPos2.getY(), (double)blockPos2.getZ() + 0.5);
						this.entityData.set(DATA_PEEK_ID, (byte)0);
						this.setTarget(null);
						return true;
					}
				}
			}

			return false;
		} else {
			return false;
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void lerpTo(double d, double e, double f, float g, float h, int i, boolean bl) {
		this.lerpSteps = 0;
		this.setPos(d, e, f);
		this.setRot(g, h);
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (this.isClosed()) {
			Entity entity = damageSource.getDirectEntity();
			if (entity instanceof AbstractArrow) {
				return false;
			}
		}

		if (!super.hurt(damageSource, f)) {
			return false;
		} else {
			if ((double)this.getHealth() < (double)this.getMaxHealth() * 0.5 && this.random.nextInt(4) == 0) {
				this.teleportSomewhere();
			} else if (damageSource.isProjectile()) {
				Entity entity = damageSource.getDirectEntity();
				if (entity != null && entity.getType() == EntityType.SHULKER_BULLET) {
					this.hitByShulkerBullet();
				}
			}

			return true;
		}
	}

	private boolean isClosed() {
		return this.getRawPeekAmount() == 0;
	}

	private void hitByShulkerBullet() {
		Vec3 vec3 = this.position();
		AABB aABB = this.getBoundingBox();
		if (!this.isClosed() && this.teleportSomewhere()) {
			int i = this.level.getEntities(EntityType.SHULKER, aABB.inflate(8.0), Entity::isAlive).size();
			float f = (float)(i - 1) / 5.0F;
			if (!(this.level.random.nextFloat() < f)) {
				Shulker shulker = EntityType.SHULKER.create(this.level);
				DyeColor dyeColor = this.getColor();
				if (dyeColor != null) {
					shulker.setColor(dyeColor);
				}

				shulker.moveTo(vec3);
				this.level.addFreshEntity(shulker);
			}
		}
	}

	@Override
	public boolean canBeCollidedWith() {
		return this.isAlive();
	}

	public Direction getAttachFace() {
		return this.entityData.get(DATA_ATTACH_FACE_ID);
	}

	private int getRawPeekAmount() {
		return this.entityData.get(DATA_PEEK_ID);
	}

	private void setRawPeekAmount(int i) {
		if (!this.level.isClientSide) {
			this.getAttribute(Attributes.ARMOR).removeModifier(COVERED_ARMOR_MODIFIER);
			if (i == 0) {
				this.getAttribute(Attributes.ARMOR).addPermanentModifier(COVERED_ARMOR_MODIFIER);
				this.playSound(SoundEvents.SHULKER_CLOSE, 1.0F, 1.0F);
			} else {
				this.playSound(SoundEvents.SHULKER_OPEN, 1.0F, 1.0F);
			}
		}

		this.entityData.set(DATA_PEEK_ID, (byte)i);
	}

	@Environment(EnvType.CLIENT)
	public float getClientPeekAmount(float f) {
		return Mth.lerp(f, this.currentPeekAmountO, this.currentPeekAmount);
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return 0.5F;
	}

	@Override
	public int getMaxHeadXRot() {
		return 180;
	}

	@Override
	public int getMaxHeadYRot() {
		return 180;
	}

	@Override
	public void push(Entity entity) {
	}

	@Override
	public float getPickRadius() {
		return 0.0F;
	}

	@Environment(EnvType.CLIENT)
	public Optional<Vec3> getRenderPosition(float f) {
		if (this.clientOldAttachPosition != null && this.clientSideTeleportInterpolation > 0) {
			double d = (double)((float)this.clientSideTeleportInterpolation - f) / 6.0;
			d *= d;
			BlockPos blockPos = this.blockPosition();
			double e = (double)(blockPos.getX() - this.clientOldAttachPosition.getX()) * d;
			double g = (double)(blockPos.getY() - this.clientOldAttachPosition.getY()) * d;
			double h = (double)(blockPos.getZ() - this.clientOldAttachPosition.getZ()) * d;
			return Optional.of(new Vec3(-e, -g, -h));
		} else {
			return Optional.empty();
		}
	}

	private void setColor(DyeColor dyeColor) {
		this.entityData.set(DATA_COLOR_ID, (byte)dyeColor.getId());
	}

	@Nullable
	public DyeColor getColor() {
		byte b = this.entityData.get(DATA_COLOR_ID);
		return b != 16 && b <= 15 ? DyeColor.byId(b) : null;
	}

	class ShulkerAttackGoal extends Goal {
		private int attackTime;

		public ShulkerAttackGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			LivingEntity livingEntity = Shulker.this.getTarget();
			return livingEntity != null && livingEntity.isAlive() ? Shulker.this.level.getDifficulty() != Difficulty.PEACEFUL : false;
		}

		@Override
		public void start() {
			this.attackTime = 20;
			Shulker.this.setRawPeekAmount(100);
		}

		@Override
		public void stop() {
			Shulker.this.setRawPeekAmount(0);
		}

		@Override
		public void tick() {
			if (Shulker.this.level.getDifficulty() != Difficulty.PEACEFUL) {
				this.attackTime--;
				LivingEntity livingEntity = Shulker.this.getTarget();
				Shulker.this.getLookControl().setLookAt(livingEntity, 180.0F, 180.0F);
				double d = Shulker.this.distanceToSqr(livingEntity);
				if (d < 400.0) {
					if (this.attackTime <= 0) {
						this.attackTime = 20 + Shulker.this.random.nextInt(10) * 20 / 2;
						Shulker.this.level.addFreshEntity(new ShulkerBullet(Shulker.this.level, Shulker.this, livingEntity, Shulker.this.getAttachFace().getAxis()));
						Shulker.this.playSound(SoundEvents.SHULKER_SHOOT, 2.0F, (Shulker.this.random.nextFloat() - Shulker.this.random.nextFloat()) * 0.2F + 1.0F);
					}
				} else {
					Shulker.this.setTarget(null);
				}

				super.tick();
			}
		}
	}

	static class ShulkerBodyRotationControl extends BodyRotationControl {
		public ShulkerBodyRotationControl(Mob mob) {
			super(mob);
		}

		@Override
		public void clientTick() {
		}
	}

	static class ShulkerDefenseAttackGoal extends NearestAttackableTargetGoal<LivingEntity> {
		public ShulkerDefenseAttackGoal(Shulker shulker) {
			super(shulker, LivingEntity.class, 10, true, false, livingEntity -> livingEntity instanceof Enemy);
		}

		@Override
		public boolean canUse() {
			return this.mob.getTeam() == null ? false : super.canUse();
		}

		@Override
		protected AABB getTargetSearchArea(double d) {
			Direction direction = ((Shulker)this.mob).getAttachFace();
			if (direction.getAxis() == Direction.Axis.X) {
				return this.mob.getBoundingBox().inflate(4.0, d, d);
			} else {
				return direction.getAxis() == Direction.Axis.Z ? this.mob.getBoundingBox().inflate(d, d, 4.0) : this.mob.getBoundingBox().inflate(d, 4.0, d);
			}
		}
	}

	class ShulkerNearestAttackGoal extends NearestAttackableTargetGoal<Player> {
		public ShulkerNearestAttackGoal(Shulker shulker2) {
			super(shulker2, Player.class, true);
		}

		@Override
		public boolean canUse() {
			return Shulker.this.level.getDifficulty() == Difficulty.PEACEFUL ? false : super.canUse();
		}

		@Override
		protected AABB getTargetSearchArea(double d) {
			Direction direction = ((Shulker)this.mob).getAttachFace();
			if (direction.getAxis() == Direction.Axis.X) {
				return this.mob.getBoundingBox().inflate(4.0, d, d);
			} else {
				return direction.getAxis() == Direction.Axis.Z ? this.mob.getBoundingBox().inflate(d, d, 4.0) : this.mob.getBoundingBox().inflate(d, 4.0, d);
			}
		}
	}

	class ShulkerPeekGoal extends Goal {
		private int peekTime;

		private ShulkerPeekGoal() {
		}

		@Override
		public boolean canUse() {
			return Shulker.this.getTarget() == null
				&& Shulker.this.random.nextInt(40) == 0
				&& Shulker.this.canStayAt(Shulker.this.blockPosition(), Shulker.this.getAttachFace());
		}

		@Override
		public boolean canContinueToUse() {
			return Shulker.this.getTarget() == null && this.peekTime > 0;
		}

		@Override
		public void start() {
			this.peekTime = 20 * (1 + Shulker.this.random.nextInt(3));
			Shulker.this.setRawPeekAmount(30);
		}

		@Override
		public void stop() {
			if (Shulker.this.getTarget() == null) {
				Shulker.this.setRawPeekAmount(0);
			}
		}

		@Override
		public void tick() {
			this.peekTime--;
		}
	}
}
