package net.minecraft.world.entity;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.scores.PlayerTeam;

public abstract class TamableAnimal extends Animal implements OwnableEntity {
	public static final int TELEPORT_WHEN_DISTANCE_IS_SQ = 144;
	private static final int MIN_HORIZONTAL_DISTANCE_FROM_TARGET_AFTER_TELEPORTING = 2;
	private static final int MAX_HORIZONTAL_DISTANCE_FROM_TARGET_AFTER_TELEPORTING = 3;
	private static final int MAX_VERTICAL_DISTANCE_FROM_TARGET_AFTER_TELEPORTING = 1;
	protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(TamableAnimal.class, EntityDataSerializers.BYTE);
	protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(
		TamableAnimal.class, EntityDataSerializers.OPTIONAL_UUID
	);
	private boolean orderedToSit;

	protected TamableAnimal(EntityType<? extends TamableAnimal> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_FLAGS_ID, (byte)0);
		builder.define(DATA_OWNERUUID_ID, Optional.empty());
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		if (this.getOwnerUUID() != null) {
			compoundTag.putUUID("Owner", this.getOwnerUUID());
		}

		compoundTag.putBoolean("Sitting", this.orderedToSit);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		UUID uUID;
		if (compoundTag.hasUUID("Owner")) {
			uUID = compoundTag.getUUID("Owner");
		} else {
			String string = compoundTag.getString("Owner");
			uUID = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), string);
		}

		if (uUID != null) {
			try {
				this.setOwnerUUID(uUID);
				this.setTame(true, false);
			} catch (Throwable var4) {
				this.setTame(false, true);
			}
		}

		this.orderedToSit = compoundTag.getBoolean("Sitting");
		this.setInSittingPose(this.orderedToSit);
	}

	@Override
	public boolean canBeLeashed() {
		return true;
	}

	@Override
	public boolean handleLeashAtDistance(Entity entity, float f) {
		if (this.isInSittingPose()) {
			if (f > 10.0F) {
				this.dropLeash(true, true);
			}

			return false;
		} else {
			return super.handleLeashAtDistance(entity, f);
		}
	}

	protected void spawnTamingParticles(boolean bl) {
		ParticleOptions particleOptions = ParticleTypes.HEART;
		if (!bl) {
			particleOptions = ParticleTypes.SMOKE;
		}

		for (int i = 0; i < 7; i++) {
			double d = this.random.nextGaussian() * 0.02;
			double e = this.random.nextGaussian() * 0.02;
			double f = this.random.nextGaussian() * 0.02;
			this.level().addParticle(particleOptions, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), d, e, f);
		}
	}

	@Override
	public void handleEntityEvent(byte b) {
		if (b == 7) {
			this.spawnTamingParticles(true);
		} else if (b == 6) {
			this.spawnTamingParticles(false);
		} else {
			super.handleEntityEvent(b);
		}
	}

	public boolean isTame() {
		return (this.entityData.get(DATA_FLAGS_ID) & 4) != 0;
	}

	public void setTame(boolean bl, boolean bl2) {
		byte b = this.entityData.get(DATA_FLAGS_ID);
		if (bl) {
			this.entityData.set(DATA_FLAGS_ID, (byte)(b | 4));
		} else {
			this.entityData.set(DATA_FLAGS_ID, (byte)(b & -5));
		}

		if (bl2) {
			this.applyTamingSideEffects();
		}
	}

	protected void applyTamingSideEffects() {
	}

	public boolean isInSittingPose() {
		return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
	}

	public void setInSittingPose(boolean bl) {
		byte b = this.entityData.get(DATA_FLAGS_ID);
		if (bl) {
			this.entityData.set(DATA_FLAGS_ID, (byte)(b | 1));
		} else {
			this.entityData.set(DATA_FLAGS_ID, (byte)(b & -2));
		}
	}

	@Nullable
	@Override
	public UUID getOwnerUUID() {
		return (UUID)this.entityData.get(DATA_OWNERUUID_ID).orElse(null);
	}

	public void setOwnerUUID(@Nullable UUID uUID) {
		this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(uUID));
	}

	public void tame(Player player) {
		this.setTame(true, true);
		this.setOwnerUUID(player.getUUID());
		if (player instanceof ServerPlayer serverPlayer) {
			CriteriaTriggers.TAME_ANIMAL.trigger(serverPlayer, this);
		}
	}

	@Override
	public boolean canAttack(LivingEntity livingEntity) {
		return this.isOwnedBy(livingEntity) ? false : super.canAttack(livingEntity);
	}

	public boolean isOwnedBy(LivingEntity livingEntity) {
		return livingEntity == this.getOwner();
	}

	public boolean wantsToAttack(LivingEntity livingEntity, LivingEntity livingEntity2) {
		return true;
	}

	@Override
	public PlayerTeam getTeam() {
		if (this.isTame()) {
			LivingEntity livingEntity = this.getOwner();
			if (livingEntity != null) {
				return livingEntity.getTeam();
			}
		}

		return super.getTeam();
	}

	@Override
	protected boolean considersEntityAsAlly(Entity entity) {
		if (this.isTame()) {
			LivingEntity livingEntity = this.getOwner();
			if (entity == livingEntity) {
				return true;
			}

			if (livingEntity != null) {
				return livingEntity.considersEntityAsAlly(entity);
			}
		}

		return super.considersEntityAsAlly(entity);
	}

	@Override
	public void die(DamageSource damageSource) {
		if (this.level() instanceof ServerLevel serverLevel
			&& serverLevel.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES)
			&& this.getOwner() instanceof ServerPlayer serverPlayer) {
			serverPlayer.sendSystemMessage(this.getCombatTracker().getDeathMessage());
		}

		super.die(damageSource);
	}

	public boolean isOrderedToSit() {
		return this.orderedToSit;
	}

	public void setOrderedToSit(boolean bl) {
		this.orderedToSit = bl;
	}

	public void tryToTeleportToOwner() {
		LivingEntity livingEntity = this.getOwner();
		if (livingEntity != null) {
			this.teleportToAroundBlockPos(livingEntity.blockPosition());
		}
	}

	public boolean shouldTryTeleportToOwner() {
		LivingEntity livingEntity = this.getOwner();
		return livingEntity != null && this.distanceToSqr(this.getOwner()) >= 144.0;
	}

	private void teleportToAroundBlockPos(BlockPos blockPos) {
		for (int i = 0; i < 10; i++) {
			int j = this.random.nextIntBetweenInclusive(-3, 3);
			int k = this.random.nextIntBetweenInclusive(-3, 3);
			if (Math.abs(j) >= 2 || Math.abs(k) >= 2) {
				int l = this.random.nextIntBetweenInclusive(-1, 1);
				if (this.maybeTeleportTo(blockPos.getX() + j, blockPos.getY() + l, blockPos.getZ() + k)) {
					return;
				}
			}
		}
	}

	private boolean maybeTeleportTo(int i, int j, int k) {
		if (!this.canTeleportTo(new BlockPos(i, j, k))) {
			return false;
		} else {
			this.moveTo((double)i + 0.5, (double)j, (double)k + 0.5, this.getYRot(), this.getXRot());
			this.navigation.stop();
			return true;
		}
	}

	private boolean canTeleportTo(BlockPos blockPos) {
		PathType pathType = WalkNodeEvaluator.getPathTypeStatic(this, blockPos);
		if (pathType != PathType.WALKABLE) {
			return false;
		} else {
			BlockState blockState = this.level().getBlockState(blockPos.below());
			if (!this.canFlyToOwner() && blockState.getBlock() instanceof LeavesBlock) {
				return false;
			} else {
				BlockPos blockPos2 = blockPos.subtract(this.blockPosition());
				return this.level().noCollision(this, this.getBoundingBox().move(blockPos2));
			}
		}
	}

	public final boolean unableToMoveToOwner() {
		return this.isOrderedToSit() || this.isPassenger() || this.mayBeLeashed() || this.getOwner() != null && this.getOwner().isSpectator();
	}

	protected boolean canFlyToOwner() {
		return false;
	}

	public class TamableAnimalPanicGoal extends PanicGoal {
		public TamableAnimalPanicGoal(final double d, final TagKey<DamageType> tagKey) {
			super(TamableAnimal.this, d, tagKey);
		}

		public TamableAnimalPanicGoal(final double d) {
			super(TamableAnimal.this, d);
		}

		@Override
		public void tick() {
			if (!TamableAnimal.this.unableToMoveToOwner() && TamableAnimal.this.shouldTryTeleportToOwner()) {
				TamableAnimal.this.tryToTeleportToOwner();
			}

			super.tick();
		}
	}
}
