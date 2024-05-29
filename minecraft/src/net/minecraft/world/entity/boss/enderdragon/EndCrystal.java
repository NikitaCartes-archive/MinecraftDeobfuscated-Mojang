package net.minecraft.world.entity.boss.enderdragon;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.dimension.end.EndDragonFight;

public class EndCrystal extends Entity {
	private static final EntityDataAccessor<Optional<BlockPos>> DATA_BEAM_TARGET = SynchedEntityData.defineId(
		EndCrystal.class, EntityDataSerializers.OPTIONAL_BLOCK_POS
	);
	private static final EntityDataAccessor<Boolean> DATA_SHOW_BOTTOM = SynchedEntityData.defineId(EndCrystal.class, EntityDataSerializers.BOOLEAN);
	public int time;

	public EndCrystal(EntityType<? extends EndCrystal> entityType, Level level) {
		super(entityType, level);
		this.blocksBuilding = true;
		this.time = this.random.nextInt(100000);
	}

	public EndCrystal(Level level, double d, double e, double f) {
		this(EntityType.END_CRYSTAL, level);
		this.setPos(d, e, f);
	}

	@Override
	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.NONE;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(DATA_BEAM_TARGET, Optional.empty());
		builder.define(DATA_SHOW_BOTTOM, true);
	}

	@Override
	public void tick() {
		this.time++;
		this.checkInsideBlocks();
		this.handlePortal();
		if (this.level() instanceof ServerLevel) {
			BlockPos blockPos = this.blockPosition();
			if (((ServerLevel)this.level()).getDragonFight() != null && this.level().getBlockState(blockPos).isAir()) {
				this.level().setBlockAndUpdate(blockPos, BaseFireBlock.getState(this.level(), blockPos));
			}
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {
		if (this.getBeamTarget() != null) {
			compoundTag.put("beam_target", NbtUtils.writeBlockPos(this.getBeamTarget()));
		}

		compoundTag.putBoolean("ShowBottom", this.showsBottom());
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compoundTag) {
		NbtUtils.readBlockPos(compoundTag, "beam_target").ifPresent(this::setBeamTarget);
		if (compoundTag.contains("ShowBottom", 1)) {
			this.setShowBottom(compoundTag.getBoolean("ShowBottom"));
		}
	}

	@Override
	public boolean isPickable() {
		return true;
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (this.isInvulnerableTo(damageSource)) {
			return false;
		} else if (damageSource.getEntity() instanceof EnderDragon) {
			return false;
		} else {
			if (!this.isRemoved() && !this.level().isClientSide) {
				this.remove(Entity.RemovalReason.KILLED);
				if (!damageSource.is(DamageTypeTags.IS_EXPLOSION)) {
					DamageSource damageSource2 = damageSource.getEntity() != null ? this.damageSources().explosion(this, damageSource.getEntity()) : null;
					this.level().explode(this, damageSource2, null, this.getX(), this.getY(), this.getZ(), 6.0F, false, Level.ExplosionInteraction.BLOCK);
				}

				this.onDestroyedBy(damageSource);
			}

			return true;
		}
	}

	@Override
	public void kill() {
		this.onDestroyedBy(this.damageSources().generic());
		super.kill();
	}

	private void onDestroyedBy(DamageSource damageSource) {
		if (this.level() instanceof ServerLevel) {
			EndDragonFight endDragonFight = ((ServerLevel)this.level()).getDragonFight();
			if (endDragonFight != null) {
				endDragonFight.onCrystalDestroyed(this, damageSource);
			}
		}
	}

	public void setBeamTarget(@Nullable BlockPos blockPos) {
		this.getEntityData().set(DATA_BEAM_TARGET, Optional.ofNullable(blockPos));
	}

	@Nullable
	public BlockPos getBeamTarget() {
		return (BlockPos)this.getEntityData().get(DATA_BEAM_TARGET).orElse(null);
	}

	public void setShowBottom(boolean bl) {
		this.getEntityData().set(DATA_SHOW_BOTTOM, bl);
	}

	public boolean showsBottom() {
		return this.getEntityData().get(DATA_SHOW_BOTTOM);
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double d) {
		return super.shouldRenderAtSqrDistance(d) || this.getBeamTarget() != null;
	}

	@Override
	public ItemStack getPickResult() {
		return new ItemStack(Items.END_CRYSTAL);
	}
}
