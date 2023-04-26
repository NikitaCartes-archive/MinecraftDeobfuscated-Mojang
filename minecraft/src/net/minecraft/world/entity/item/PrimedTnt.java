package net.minecraft.world.entity.item;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.Level;

public class PrimedTnt extends Entity implements TraceableEntity {
	private static final EntityDataAccessor<Integer> DATA_FUSE_ID = SynchedEntityData.defineId(PrimedTnt.class, EntityDataSerializers.INT);
	private static final int DEFAULT_FUSE_TIME = 80;
	@Nullable
	private LivingEntity owner;

	public PrimedTnt(EntityType<? extends PrimedTnt> entityType, Level level) {
		super(entityType, level);
		this.blocksBuilding = true;
	}

	public PrimedTnt(Level level, double d, double e, double f, @Nullable LivingEntity livingEntity) {
		this(EntityType.TNT, level);
		this.setPos(d, e, f);
		double g = level.random.nextDouble() * (float) (Math.PI * 2);
		this.setDeltaMovement(-Math.sin(g) * 0.02, 0.2F, -Math.cos(g) * 0.02);
		this.setFuse(80);
		this.xo = d;
		this.yo = e;
		this.zo = f;
		this.owner = livingEntity;
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(DATA_FUSE_ID, 80);
	}

	@Override
	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.NONE;
	}

	@Override
	public boolean isPickable() {
		return !this.isRemoved();
	}

	@Override
	public void tick() {
		if (!this.isNoGravity()) {
			this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
		}

		this.move(MoverType.SELF, this.getDeltaMovement());
		this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
		if (this.onGround()) {
			this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, -0.5, 0.7));
		}

		int i = this.getFuse() - 1;
		this.setFuse(i);
		if (i <= 0) {
			this.discard();
			if (!this.level().isClientSide) {
				this.explode();
			}
		} else {
			this.updateInWaterStateAndDoFluidPushing();
			if (this.level().isClientSide) {
				this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
			}
		}
	}

	private void explode() {
		float f = 4.0F;
		this.level().explode(this, this.getX(), this.getY(0.0625), this.getZ(), 4.0F, Level.ExplosionInteraction.TNT);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {
		compoundTag.putShort("Fuse", (short)this.getFuse());
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compoundTag) {
		this.setFuse(compoundTag.getShort("Fuse"));
	}

	@Nullable
	public LivingEntity getOwner() {
		return this.owner;
	}

	@Override
	protected float getEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return 0.15F;
	}

	public void setFuse(int i) {
		this.entityData.set(DATA_FUSE_ID, i);
	}

	public int getFuse() {
		return this.entityData.get(DATA_FUSE_ID);
	}
}
