package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.List;
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
import net.minecraft.world.ShulkerSharedHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Shulker extends AbstractGolem implements Enemy {
	private static final UUID COVERED_ARMOR_MODIFIER_UUID = UUID.fromString("7E0292F2-9434-48D5-A29F-9583AF7DF27F");
	private static final AttributeModifier COVERED_ARMOR_MODIFIER = new AttributeModifier(
		COVERED_ARMOR_MODIFIER_UUID, "Covered armor bonus", 20.0, AttributeModifier.Operation.ADDITION
	);
	protected static final EntityDataAccessor<Direction> DATA_ATTACH_FACE_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.DIRECTION);
	protected static final EntityDataAccessor<Optional<BlockPos>> DATA_ATTACH_POS_ID = SynchedEntityData.defineId(
		Shulker.class, EntityDataSerializers.OPTIONAL_BLOCK_POS
	);
	protected static final EntityDataAccessor<Byte> DATA_PEEK_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.BYTE);
	protected static final EntityDataAccessor<Byte> DATA_COLOR_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.BYTE);
	private float currentPeekAmountO;
	private float currentPeekAmount;
	private BlockPos oldAttachPosition = null;
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
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
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
		this.entityData.define(DATA_ATTACH_POS_ID, Optional.empty());
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
		this.entityData.set(DATA_COLOR_ID, compoundTag.getByte("Color"));
		if (compoundTag.contains("APX")) {
			int i = compoundTag.getInt("APX");
			int j = compoundTag.getInt("APY");
			int k = compoundTag.getInt("APZ");
			this.entityData.set(DATA_ATTACH_POS_ID, Optional.of(new BlockPos(i, j, k)));
		} else {
			this.entityData.set(DATA_ATTACH_POS_ID, Optional.empty());
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putByte("AttachFace", (byte)this.entityData.get(DATA_ATTACH_FACE_ID).get3DDataValue());
		compoundTag.putByte("Peek", this.entityData.get(DATA_PEEK_ID));
		compoundTag.putByte("Color", this.entityData.get(DATA_COLOR_ID));
		BlockPos blockPos = this.getAttachPosition();
		if (blockPos != null) {
			compoundTag.putInt("APX", blockPos.getX());
			compoundTag.putInt("APY", blockPos.getY());
			compoundTag.putInt("APZ", blockPos.getZ());
		}
	}

	@Override
	public void tick() {
		super.tick();
		BlockPos blockPos = (BlockPos)this.entityData.get(DATA_ATTACH_POS_ID).orElse(null);
		if (blockPos == null && !this.level.isClientSide) {
			blockPos = this.blockPosition();
			this.entityData.set(DATA_ATTACH_POS_ID, Optional.of(blockPos));
		}

		if (this.isPassenger()) {
			blockPos = null;
			float f = this.getVehicle().yRot;
			this.yRot = f;
			this.yBodyRot = f;
			this.yBodyRotO = f;
			this.clientSideTeleportInterpolation = 0;
		} else if (!this.level.isClientSide) {
			BlockState blockState = this.level.getBlockState(blockPos);
			if (!blockState.isAir()) {
				if (blockState.is(Blocks.MOVING_PISTON)) {
					Direction direction = blockState.getValue(PistonBaseBlock.FACING);
					if (this.level.isEmptyBlock(blockPos.relative(direction))) {
						blockPos = blockPos.relative(direction);
						this.entityData.set(DATA_ATTACH_POS_ID, Optional.of(blockPos));
					} else {
						this.teleportSomewhere();
					}
				} else if (blockState.is(Blocks.PISTON_HEAD)) {
					Direction direction = blockState.getValue(PistonHeadBlock.FACING);
					if (this.level.isEmptyBlock(blockPos.relative(direction))) {
						blockPos = blockPos.relative(direction);
						this.entityData.set(DATA_ATTACH_POS_ID, Optional.of(blockPos));
					} else {
						this.teleportSomewhere();
					}
				} else {
					this.teleportSomewhere();
				}
			}

			Direction direction = this.getAttachFace();
			if (!this.canAttachOnBlockFace(blockPos, direction)) {
				Direction direction2 = this.findAttachableFace(blockPos);
				if (direction2 != null) {
					this.entityData.set(DATA_ATTACH_FACE_ID, direction2);
				} else {
					this.teleportSomewhere();
				}
			}
		}

		float f = (float)this.getRawPeekAmount() * 0.01F;
		this.currentPeekAmountO = this.currentPeekAmount;
		if (this.currentPeekAmount > f) {
			this.currentPeekAmount = Mth.clamp(this.currentPeekAmount - 0.05F, f, 1.0F);
		} else if (this.currentPeekAmount < f) {
			this.currentPeekAmount = Mth.clamp(this.currentPeekAmount + 0.05F, 0.0F, f);
		}

		if (blockPos != null) {
			if (this.level.isClientSide) {
				if (this.clientSideTeleportInterpolation > 0 && this.oldAttachPosition != null) {
					this.clientSideTeleportInterpolation--;
				} else {
					this.oldAttachPosition = blockPos;
				}
			}

			this.setPosAndOldPos((double)blockPos.getX() + 0.5, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5);
			double d = 0.5 - (double)Mth.sin((0.5F + this.currentPeekAmount) * (float) Math.PI) * 0.5;
			double e = 0.5 - (double)Mth.sin((0.5F + this.currentPeekAmountO) * (float) Math.PI) * 0.5;
			Direction direction3 = this.getAttachFace().getOpposite();
			this.setBoundingBox(
				new AABB(this.getX() - 0.5, this.getY(), this.getZ() - 0.5, this.getX() + 0.5, this.getY() + 1.0, this.getZ() + 0.5)
					.expandTowards((double)direction3.getStepX() * d, (double)direction3.getStepY() * d, (double)direction3.getStepZ() * d)
			);
			double g = d - e;
			if (g > 0.0) {
				List<Entity> list = this.level.getEntities(this, this.getBoundingBox());
				if (!list.isEmpty()) {
					for (Entity entity : list) {
						if (!(entity instanceof Shulker) && !entity.noPhysics) {
							entity.move(MoverType.SHULKER, new Vec3(g * (double)direction3.getStepX(), g * (double)direction3.getStepY(), g * (double)direction3.getStepZ()));
						}
					}
				}
			}
		}
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
	public void setPos(double d, double e, double f) {
		super.setPos(d, e, f);
		if (this.entityData != null && this.tickCount != 0) {
			Optional<BlockPos> optional = this.entityData.get(DATA_ATTACH_POS_ID);
			Optional<BlockPos> optional2 = Optional.of(new BlockPos(d, e, f));
			if (!optional2.equals(optional)) {
				this.entityData.set(DATA_ATTACH_POS_ID, optional2);
				this.entityData.set(DATA_PEEK_ID, (byte)0);
				this.hasImpulse = true;
			}
		}
	}

	@Nullable
	protected Direction findAttachableFace(BlockPos blockPos) {
		for (Direction direction : Direction.values()) {
			if (this.canAttachOnBlockFace(blockPos, direction)) {
				return direction;
			}
		}

		return null;
	}

	private boolean canAttachOnBlockFace(BlockPos blockPos, Direction direction) {
		return this.level.loadedAndEntityCanStandOnFace(blockPos.relative(direction), this, direction.getOpposite())
			&& this.level.noCollision(this, ShulkerSharedHelper.openBoundingBox(blockPos, direction.getOpposite()));
	}

	protected boolean teleportSomewhere() {
		if (!this.isNoAi() && this.isAlive()) {
			BlockPos blockPos = this.blockPosition();

			for (int i = 0; i < 5; i++) {
				BlockPos blockPos2 = blockPos.offset(8 - this.random.nextInt(17), 8 - this.random.nextInt(17), 8 - this.random.nextInt(17));
				if (blockPos2.getY() > 0
					&& this.level.isEmptyBlock(blockPos2)
					&& this.level.getWorldBorder().isWithinBounds(blockPos2)
					&& this.level.noCollision(this, new AABB(blockPos2))) {
					Direction direction = this.findAttachableFace(blockPos2);
					if (direction != null) {
						this.entityData.set(DATA_ATTACH_FACE_ID, direction);
						this.playSound(SoundEvents.SHULKER_TELEPORT, 1.0F, 1.0F);
						this.entityData.set(DATA_ATTACH_POS_ID, Optional.of(blockPos2));
						this.entityData.set(DATA_PEEK_ID, (byte)0);
						this.setTarget(null);
						return true;
					}
				}
			}

			return false;
		} else {
			return true;
		}
	}

	@Override
	public void aiStep() {
		super.aiStep();
		this.setDeltaMovement(Vec3.ZERO);
		if (!this.isNoAi()) {
			this.yBodyRotO = 0.0F;
			this.yBodyRot = 0.0F;
		}
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		if (DATA_ATTACH_POS_ID.equals(entityDataAccessor) && this.level.isClientSide && !this.isPassenger()) {
			BlockPos blockPos = this.getAttachPosition();
			if (blockPos != null) {
				if (this.oldAttachPosition == null) {
					this.oldAttachPosition = blockPos;
				} else {
					this.clientSideTeleportInterpolation = 6;
				}

				this.setPosAndOldPos((double)blockPos.getX() + 0.5, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5);
			}
		}

		super.onSyncedDataUpdated(entityDataAccessor);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void lerpTo(double d, double e, double f, float g, float h, int i, boolean bl) {
		this.lerpSteps = 0;
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (this.isClosed()) {
			Entity entity = damageSource.getDirectEntity();
			if (entity instanceof AbstractArrow) {
				return false;
			}
		}

		if (super.hurt(damageSource, f)) {
			if ((double)this.getHealth() < (double)this.getMaxHealth() * 0.5 && this.random.nextInt(4) == 0) {
				this.teleportSomewhere();
			}

			return true;
		} else {
			return false;
		}
	}

	private boolean isClosed() {
		return this.getRawPeekAmount() == 0;
	}

	@Nullable
	@Override
	public AABB getCollideBox() {
		return this.isAlive() ? this.getBoundingBox() : null;
	}

	public Direction getAttachFace() {
		return this.entityData.get(DATA_ATTACH_FACE_ID);
	}

	@Nullable
	public BlockPos getAttachPosition() {
		return (BlockPos)this.entityData.get(DATA_ATTACH_POS_ID).orElse(null);
	}

	public void setAttachPosition(@Nullable BlockPos blockPos) {
		this.entityData.set(DATA_ATTACH_POS_ID, Optional.ofNullable(blockPos));
	}

	public int getRawPeekAmount() {
		return this.entityData.get(DATA_PEEK_ID);
	}

	public void setRawPeekAmount(int i) {
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

	@Environment(EnvType.CLIENT)
	public int getClientSideTeleportInterpolation() {
		return this.clientSideTeleportInterpolation;
	}

	@Environment(EnvType.CLIENT)
	public BlockPos getOldAttachPosition() {
		return this.oldAttachPosition;
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
	public boolean hasValidInterpolationPositions() {
		return this.oldAttachPosition != null && this.getAttachPosition() != null;
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public DyeColor getColor() {
		Byte byte_ = this.entityData.get(DATA_COLOR_ID);
		return byte_ != 16 && byte_ <= 15 ? DyeColor.byId(byte_) : null;
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

	class ShulkerBodyRotationControl extends BodyRotationControl {
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
			return Shulker.this.getTarget() == null && Shulker.this.random.nextInt(40) == 0;
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
