package net.minecraft.world.entity;

import com.mojang.datafixers.util.Either;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;

public interface Leashable {
	String LEASH_TAG = "leash";
	double LEASH_TOO_FAR_DIST = 10.0;
	double LEASH_ELASTIC_DIST = 6.0;

	@Nullable
	Leashable.LeashData getLeashData();

	void setLeashData(@Nullable Leashable.LeashData leashData);

	default boolean isLeashed() {
		return this.getLeashData() != null && this.getLeashData().leashHolder != null;
	}

	default boolean mayBeLeashed() {
		return this.getLeashData() != null;
	}

	default boolean canHaveALeashAttachedToIt() {
		return this.canBeLeashed() && !this.isLeashed();
	}

	default boolean canBeLeashed() {
		return true;
	}

	default void setDelayedLeashHolderId(int i) {
		this.setLeashData(new Leashable.LeashData(i));
		dropLeash((Entity)this, false, false);
	}

	@Nullable
	default Leashable.LeashData readLeashData(CompoundTag compoundTag) {
		if (compoundTag.contains("leash", 10)) {
			return new Leashable.LeashData(Either.left(compoundTag.getCompound("leash").getUUID("UUID")));
		} else {
			if (compoundTag.contains("leash", 11)) {
				Either<UUID, BlockPos> either = (Either<UUID, BlockPos>)NbtUtils.readBlockPos(compoundTag, "leash").map(Either::right).orElse(null);
				if (either != null) {
					return new Leashable.LeashData(either);
				}
			}

			return null;
		}
	}

	default void writeLeashData(CompoundTag compoundTag, @Nullable Leashable.LeashData leashData) {
		if (leashData != null) {
			Either<UUID, BlockPos> either = leashData.delayedLeashInfo;
			if (leashData.leashHolder instanceof LeashFenceKnotEntity leashFenceKnotEntity) {
				either = Either.right(leashFenceKnotEntity.getPos());
			} else if (leashData.leashHolder != null) {
				either = Either.left(leashData.leashHolder.getUUID());
			}

			if (either != null) {
				compoundTag.put("leash", either.map(uUID -> {
					CompoundTag compoundTagx = new CompoundTag();
					compoundTagx.putUUID("UUID", uUID);
					return compoundTagx;
				}, NbtUtils::writeBlockPos));
			}
		}
	}

	private static <E extends Entity & Leashable> void restoreLeashFromSave(E entity, Leashable.LeashData leashData) {
		if (leashData.delayedLeashInfo != null && entity.level() instanceof ServerLevel serverLevel) {
			Optional<UUID> optional = leashData.delayedLeashInfo.left();
			Optional<BlockPos> optional2 = leashData.delayedLeashInfo.right();
			if (optional.isPresent()) {
				Entity entity2 = serverLevel.getEntity((UUID)optional.get());
				if (entity2 != null) {
					setLeashedTo(entity, entity2, true);
					return;
				}
			} else if (optional2.isPresent()) {
				setLeashedTo(entity, LeashFenceKnotEntity.getOrCreateKnot(serverLevel, (BlockPos)optional2.get()), true);
				return;
			}

			if (entity.tickCount > 100) {
				entity.spawnAtLocation(serverLevel, Items.LEAD);
				entity.setLeashData(null);
			}
		}
	}

	default void dropLeash(boolean bl, boolean bl2) {
		dropLeash((Entity)this, bl, bl2);
	}

	private static <E extends Entity & Leashable> void dropLeash(E entity, boolean bl, boolean bl2) {
		Leashable.LeashData leashData = entity.getLeashData();
		if (leashData != null && leashData.leashHolder != null) {
			entity.setLeashData(null);
			if (entity.level() instanceof ServerLevel serverLevel) {
				if (bl2) {
					entity.spawnAtLocation(serverLevel, Items.LEAD);
				}

				if (bl) {
					serverLevel.getChunkSource().broadcast(entity, new ClientboundSetEntityLinkPacket(entity, null));
				}
			}
		}
	}

	static <E extends Entity & Leashable> void tickLeash(ServerLevel serverLevel, E entity) {
		Leashable.LeashData leashData = entity.getLeashData();
		if (leashData != null && leashData.delayedLeashInfo != null) {
			restoreLeashFromSave(entity, leashData);
		}

		if (leashData != null && leashData.leashHolder != null) {
			if (!entity.isAlive() || !leashData.leashHolder.isAlive()) {
				dropLeash(entity, true, serverLevel.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS));
			}

			Entity entity2 = entity.getLeashHolder();
			if (entity2 != null && entity2.level() == entity.level()) {
				float f = entity.distanceTo(entity2);
				if (!entity.handleLeashAtDistance(entity2, f)) {
					return;
				}

				if ((double)f > 10.0) {
					entity.leashTooFarBehaviour();
				} else if ((double)f > 6.0) {
					entity.elasticRangeLeashBehaviour(entity2, f);
					entity.checkSlowFallDistance();
				} else {
					entity.closeRangeLeashBehaviour(entity2);
				}
			}
		}
	}

	default boolean handleLeashAtDistance(Entity entity, float f) {
		return true;
	}

	default void leashTooFarBehaviour() {
		this.dropLeash(true, true);
	}

	default void closeRangeLeashBehaviour(Entity entity) {
	}

	default void elasticRangeLeashBehaviour(Entity entity, float f) {
		legacyElasticRangeLeashBehaviour((Entity)this, entity, f);
	}

	private static <E extends Entity & Leashable> void legacyElasticRangeLeashBehaviour(E entity, Entity entity2, float f) {
		double d = (entity2.getX() - entity.getX()) / (double)f;
		double e = (entity2.getY() - entity.getY()) / (double)f;
		double g = (entity2.getZ() - entity.getZ()) / (double)f;
		entity.setDeltaMovement(entity.getDeltaMovement().add(Math.copySign(d * d * 0.4, d), Math.copySign(e * e * 0.4, e), Math.copySign(g * g * 0.4, g)));
	}

	default void setLeashedTo(Entity entity, boolean bl) {
		setLeashedTo((Entity)this, entity, bl);
	}

	private static <E extends Entity & Leashable> void setLeashedTo(E entity, Entity entity2, boolean bl) {
		Leashable.LeashData leashData = entity.getLeashData();
		if (leashData == null) {
			leashData = new Leashable.LeashData(entity2);
			entity.setLeashData(leashData);
		} else {
			leashData.setLeashHolder(entity2);
		}

		if (bl && entity.level() instanceof ServerLevel serverLevel) {
			serverLevel.getChunkSource().broadcast(entity, new ClientboundSetEntityLinkPacket(entity, entity2));
		}

		if (entity.isPassenger()) {
			entity.stopRiding();
		}
	}

	@Nullable
	default Entity getLeashHolder() {
		return getLeashHolder((Entity)this);
	}

	@Nullable
	private static <E extends Entity & Leashable> Entity getLeashHolder(E entity) {
		Leashable.LeashData leashData = entity.getLeashData();
		if (leashData == null) {
			return null;
		} else {
			if (leashData.delayedLeashHolderId != 0 && entity.level().isClientSide) {
				Entity var3 = entity.level().getEntity(leashData.delayedLeashHolderId);
				if (var3 instanceof Entity) {
					leashData.setLeashHolder(var3);
				}
			}

			return leashData.leashHolder;
		}
	}

	public static final class LeashData {
		int delayedLeashHolderId;
		@Nullable
		public Entity leashHolder;
		@Nullable
		public Either<UUID, BlockPos> delayedLeashInfo;

		LeashData(Either<UUID, BlockPos> either) {
			this.delayedLeashInfo = either;
		}

		LeashData(Entity entity) {
			this.leashHolder = entity;
		}

		LeashData(int i) {
			this.delayedLeashHolderId = i;
		}

		public void setLeashHolder(Entity entity) {
			this.leashHolder = entity;
			this.delayedLeashInfo = null;
			this.delayedLeashHolderId = 0;
		}
	}
}
