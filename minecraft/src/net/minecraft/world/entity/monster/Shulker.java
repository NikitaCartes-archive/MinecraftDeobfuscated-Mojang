package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.LookControl;
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
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class Shulker extends AbstractGolem implements VariantHolder<Optional<DyeColor>>, Enemy {
	private static final ResourceLocation COVERED_ARMOR_MODIFIER_ID = ResourceLocation.withDefaultNamespace("covered");
	private static final AttributeModifier COVERED_ARMOR_MODIFIER = new AttributeModifier(COVERED_ARMOR_MODIFIER_ID, 20.0, AttributeModifier.Operation.ADD_VALUE);
	protected static final EntityDataAccessor<Direction> DATA_ATTACH_FACE_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.DIRECTION);
	protected static final EntityDataAccessor<Byte> DATA_PEEK_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.BYTE);
	protected static final EntityDataAccessor<Byte> DATA_COLOR_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.BYTE);
	private static final int TELEPORT_STEPS = 6;
	private static final byte NO_COLOR = 16;
	private static final byte DEFAULT_COLOR = 16;
	private static final int MAX_TELEPORT_DISTANCE = 8;
	private static final int OTHER_SHULKER_SCAN_RADIUS = 8;
	private static final int OTHER_SHULKER_LIMIT = 5;
	private static final float PEEK_PER_TICK = 0.05F;
	static final Vector3f FORWARD = Util.make(() -> {
		Vec3i vec3i = Direction.SOUTH.getUnitVec3i();
		return new Vector3f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
	});
	private static final float MAX_SCALE = 3.0F;
	private float currentPeekAmountO;
	private float currentPeekAmount;
	@Nullable
	private BlockPos clientOldAttachPosition;
	private int clientSideTeleportInterpolation;
	private static final float MAX_LID_OPEN = 1.0F;

	public Shulker(EntityType<? extends Shulker> entityType, Level level) {
		super(entityType, level);
		this.xpReward = 5;
		this.lookControl = new Shulker.ShulkerLookControl(this);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F, 0.02F, true));
		this.goalSelector.addGoal(4, new Shulker.ShulkerAttackGoal());
		this.goalSelector.addGoal(7, new Shulker.ShulkerPeekGoal());
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this, this.getClass()).setAlertOthers());
		this.targetSelector.addGoal(2, new Shulker.ShulkerNearestAttackGoal(this));
		this.targetSelector.addGoal(3, new Shulker.ShulkerDefenseAttackGoal(this));
	}

	@Override
	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.NONE;
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
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_ATTACH_FACE_ID, Direction.DOWN);
		builder.define(DATA_PEEK_ID, (byte)0);
		builder.define(DATA_COLOR_ID, (byte)16);
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
		this.setAttachFace(Direction.from3DDataValue(compoundTag.getByte("AttachFace")));
		this.entityData.set(DATA_PEEK_ID, compoundTag.getByte("Peek"));
		if (compoundTag.contains("Color", 99)) {
			this.entityData.set(DATA_COLOR_ID, compoundTag.getByte("Color"));
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putByte("AttachFace", (byte)this.getAttachFace().get3DDataValue());
		compoundTag.putByte("Peek", this.entityData.get(DATA_PEEK_ID));
		compoundTag.putByte("Color", this.entityData.get(DATA_COLOR_ID));
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.level().isClientSide && !this.isPassenger() && !this.canStayAt(this.blockPosition(), this.getAttachFace())) {
			this.findNewAttachment();
		}

		if (this.updatePeekAmount()) {
			this.onPeekAmountChange();
		}

		if (this.level().isClientSide) {
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
			this.setAttachFace(direction);
		} else {
			this.teleportSomewhere();
		}
	}

	@Override
	protected AABB makeBoundingBox() {
		float f = getPhysicalPeek(this.currentPeekAmount);
		Direction direction = this.getAttachFace().getOpposite();
		float g = this.getBbWidth() / 2.0F;
		return getProgressAabb(this.getScale(), direction, f).move(this.getX() - (double)g, this.getY(), this.getZ() - (double)g);
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
		float h = (f - g) * this.getScale();
		if (!(h <= 0.0F)) {
			for (Entity entity : this.level()
				.getEntities(
					this,
					getProgressDeltaAabb(this.getScale(), direction, g, f).move(this.getX() - 0.5, this.getY(), this.getZ() - 0.5),
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

	public static AABB getProgressAabb(float f, Direction direction, float g) {
		return getProgressDeltaAabb(f, direction, -1.0F, g);
	}

	public static AABB getProgressDeltaAabb(float f, Direction direction, float g, float h) {
		AABB aABB = new AABB(0.0, 0.0, 0.0, (double)f, (double)f, (double)f);
		double d = (double)Math.max(g, h);
		double e = (double)Math.min(g, h);
		return aABB.expandTowards(
				(double)direction.getStepX() * d * (double)f, (double)direction.getStepY() * d * (double)f, (double)direction.getStepZ() * d * (double)f
			)
			.contract(
				(double)(-direction.getStepX()) * (1.0 + e) * (double)f,
				(double)(-direction.getStepY()) * (1.0 + e) * (double)f,
				(double)(-direction.getStepZ()) * (1.0 + e) * (double)f
			);
	}

	@Override
	public boolean startRiding(Entity entity, boolean bl) {
		if (this.level().isClientSide()) {
			this.clientOldAttachPosition = null;
			this.clientSideTeleportInterpolation = 0;
		}

		this.setAttachFace(Direction.DOWN);
		return super.startRiding(entity, bl);
	}

	@Override
	public void stopRiding() {
		super.stopRiding();
		if (this.level().isClientSide) {
			this.clientOldAttachPosition = this.blockPosition();
		}

		this.yBodyRotO = 0.0F;
		this.yBodyRot = 0.0F;
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData
	) {
		this.setYRot(0.0F);
		this.yHeadRot = this.getYRot();
		this.setOldPosAndRot();
		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
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
				if (this.level().isClientSide && !this.isPassenger() && !blockPos2.equals(this.clientOldAttachPosition)) {
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

	boolean canStayAt(BlockPos blockPos, Direction direction) {
		if (this.isPositionBlocked(blockPos)) {
			return false;
		} else {
			Direction direction2 = direction.getOpposite();
			if (!this.level().loadedAndEntityCanStandOnFace(blockPos.relative(direction), this, direction2)) {
				return false;
			} else {
				AABB aABB = getProgressAabb(this.getScale(), direction2, 1.0F).move(blockPos).deflate(1.0E-6);
				return this.level().noCollision(this, aABB);
			}
		}
	}

	private boolean isPositionBlocked(BlockPos blockPos) {
		BlockState blockState = this.level().getBlockState(blockPos);
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
				if (blockPos2.getY() > this.level().getMinY()
					&& this.level().isEmptyBlock(blockPos2)
					&& this.level().getWorldBorder().isWithinBounds(blockPos2)
					&& this.level().noCollision(this, new AABB(blockPos2).deflate(1.0E-6))) {
					Direction direction = this.findAttachableSurface(blockPos2);
					if (direction != null) {
						this.unRide();
						this.setAttachFace(direction);
						this.playSound(SoundEvents.SHULKER_TELEPORT, 1.0F, 1.0F);
						this.setPos((double)blockPos2.getX() + 0.5, (double)blockPos2.getY(), (double)blockPos2.getZ() + 0.5);
						this.level().gameEvent(GameEvent.TELEPORT, blockPos, GameEvent.Context.of(this));
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

	@Override
	public void lerpTo(double d, double e, double f, float g, float h, int i) {
		this.lerpSteps = 0;
		this.setPos(d, e, f);
		this.setRot(g, h);
	}

	@Override
	public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
		if (this.isClosed()) {
			Entity entity = damageSource.getDirectEntity();
			if (entity instanceof AbstractArrow) {
				return false;
			}
		}

		if (!super.hurtServer(serverLevel, damageSource, f)) {
			return false;
		} else {
			if ((double)this.getHealth() < (double)this.getMaxHealth() * 0.5 && this.random.nextInt(4) == 0) {
				this.teleportSomewhere();
			} else if (damageSource.is(DamageTypeTags.IS_PROJECTILE)) {
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
			int i = this.level().getEntities(EntityType.SHULKER, aABB.inflate(8.0), Entity::isAlive).size();
			float f = (float)(i - 1) / 5.0F;
			if (!(this.level().random.nextFloat() < f)) {
				Shulker shulker = EntityType.SHULKER.create(this.level(), EntitySpawnReason.BREEDING);
				if (shulker != null) {
					shulker.setVariant(this.getVariant());
					shulker.moveTo(vec3);
					this.level().addFreshEntity(shulker);
				}
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

	private void setAttachFace(Direction direction) {
		this.entityData.set(DATA_ATTACH_FACE_ID, direction);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		if (DATA_ATTACH_FACE_ID.equals(entityDataAccessor)) {
			this.setBoundingBox(this.makeBoundingBox());
		}

		super.onSyncedDataUpdated(entityDataAccessor);
	}

	private int getRawPeekAmount() {
		return this.entityData.get(DATA_PEEK_ID);
	}

	void setRawPeekAmount(int i) {
		if (!this.level().isClientSide) {
			this.getAttribute(Attributes.ARMOR).removeModifier(COVERED_ARMOR_MODIFIER_ID);
			if (i == 0) {
				this.getAttribute(Attributes.ARMOR).addPermanentModifier(COVERED_ARMOR_MODIFIER);
				this.playSound(SoundEvents.SHULKER_CLOSE, 1.0F, 1.0F);
				this.gameEvent(GameEvent.CONTAINER_CLOSE);
			} else {
				this.playSound(SoundEvents.SHULKER_OPEN, 1.0F, 1.0F);
				this.gameEvent(GameEvent.CONTAINER_OPEN);
			}
		}

		this.entityData.set(DATA_PEEK_ID, (byte)i);
	}

	public float getClientPeekAmount(float f) {
		return Mth.lerp(f, this.currentPeekAmountO, this.currentPeekAmount);
	}

	@Override
	public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
		super.recreateFromPacket(clientboundAddEntityPacket);
		this.yBodyRot = 0.0F;
		this.yBodyRotO = 0.0F;
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

	@Nullable
	public Vec3 getRenderPosition(float f) {
		if (this.clientOldAttachPosition != null && this.clientSideTeleportInterpolation > 0) {
			double d = (double)((float)this.clientSideTeleportInterpolation - f) / 6.0;
			d *= d;
			d *= (double)this.getScale();
			BlockPos blockPos = this.blockPosition();
			double e = (double)(blockPos.getX() - this.clientOldAttachPosition.getX()) * d;
			double g = (double)(blockPos.getY() - this.clientOldAttachPosition.getY()) * d;
			double h = (double)(blockPos.getZ() - this.clientOldAttachPosition.getZ()) * d;
			return new Vec3(-e, -g, -h);
		} else {
			return null;
		}
	}

	@Override
	protected float sanitizeScale(float f) {
		return Math.min(f, 3.0F);
	}

	public void setVariant(Optional<DyeColor> optional) {
		this.entityData.set(DATA_COLOR_ID, (Byte)optional.map(dyeColor -> (byte)dyeColor.getId()).orElse((byte)16));
	}

	public Optional<DyeColor> getVariant() {
		return Optional.ofNullable(this.getColor());
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
			return livingEntity != null && livingEntity.isAlive() ? Shulker.this.level().getDifficulty() != Difficulty.PEACEFUL : false;
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
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			if (Shulker.this.level().getDifficulty() != Difficulty.PEACEFUL) {
				this.attackTime--;
				LivingEntity livingEntity = Shulker.this.getTarget();
				if (livingEntity != null) {
					Shulker.this.getLookControl().setLookAt(livingEntity, 180.0F, 180.0F);
					double d = Shulker.this.distanceToSqr(livingEntity);
					if (d < 400.0) {
						if (this.attackTime <= 0) {
							this.attackTime = 20 + Shulker.this.random.nextInt(10) * 20 / 2;
							Shulker.this.level().addFreshEntity(new ShulkerBullet(Shulker.this.level(), Shulker.this, livingEntity, Shulker.this.getAttachFace().getAxis()));
							Shulker.this.playSound(SoundEvents.SHULKER_SHOOT, 2.0F, (Shulker.this.random.nextFloat() - Shulker.this.random.nextFloat()) * 0.2F + 1.0F);
						}
					} else {
						Shulker.this.setTarget(null);
					}

					super.tick();
				}
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
			super(shulker, LivingEntity.class, 10, true, false, (livingEntity, serverLevel) -> livingEntity instanceof Enemy);
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

	class ShulkerLookControl extends LookControl {
		public ShulkerLookControl(final Mob mob) {
			super(mob);
		}

		@Override
		protected void clampHeadRotationToBody() {
		}

		@Override
		protected Optional<Float> getYRotD() {
			Direction direction = Shulker.this.getAttachFace().getOpposite();
			Vector3f vector3f = direction.getRotation().transform(new Vector3f(Shulker.FORWARD));
			Vec3i vec3i = direction.getUnitVec3i();
			Vector3f vector3f2 = new Vector3f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
			vector3f2.cross(vector3f);
			double d = this.wantedX - this.mob.getX();
			double e = this.wantedY - this.mob.getEyeY();
			double f = this.wantedZ - this.mob.getZ();
			Vector3f vector3f3 = new Vector3f((float)d, (float)e, (float)f);
			float g = vector3f2.dot(vector3f3);
			float h = vector3f.dot(vector3f3);
			return !(Math.abs(g) > 1.0E-5F) && !(Math.abs(h) > 1.0E-5F)
				? Optional.empty()
				: Optional.of((float)(Mth.atan2((double)(-g), (double)h) * 180.0F / (float)Math.PI));
		}

		@Override
		protected Optional<Float> getXRotD() {
			return Optional.of(0.0F);
		}
	}

	class ShulkerNearestAttackGoal extends NearestAttackableTargetGoal<Player> {
		public ShulkerNearestAttackGoal(final Shulker shulker2) {
			super(shulker2, Player.class, true);
		}

		@Override
		public boolean canUse() {
			return Shulker.this.level().getDifficulty() == Difficulty.PEACEFUL ? false : super.canUse();
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

		@Override
		public boolean canUse() {
			return Shulker.this.getTarget() == null
				&& Shulker.this.random.nextInt(reducedTickDelay(40)) == 0
				&& Shulker.this.canStayAt(Shulker.this.blockPosition(), Shulker.this.getAttachFace());
		}

		@Override
		public boolean canContinueToUse() {
			return Shulker.this.getTarget() == null && this.peekTime > 0;
		}

		@Override
		public void start() {
			this.peekTime = this.adjustedTickDelay(20 * (1 + Shulker.this.random.nextInt(3)));
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
