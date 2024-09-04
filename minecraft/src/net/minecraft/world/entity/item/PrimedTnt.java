package net.minecraft.world.entity.item;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.portal.DimensionTransition;

public class PrimedTnt extends Entity implements TraceableEntity {
	private static final EntityDataAccessor<Integer> DATA_FUSE_ID = SynchedEntityData.defineId(PrimedTnt.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<BlockState> DATA_BLOCK_STATE_ID = SynchedEntityData.defineId(PrimedTnt.class, EntityDataSerializers.BLOCK_STATE);
	private static final int DEFAULT_FUSE_TIME = 80;
	private static final float DEFAULT_EXPLOSION_POWER = 4.0F;
	private static final String TAG_BLOCK_STATE = "block_state";
	private static final String TAG_FUSE = "fuse";
	private static final String TAG_EXPLOSION_POWER = "explosion_power";
	private static final ExplosionDamageCalculator USED_PORTAL_DAMAGE_CALCULATOR = new ExplosionDamageCalculator() {
		@Override
		public boolean shouldBlockExplode(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, float f) {
			return blockState.is(Blocks.NETHER_PORTAL) ? false : super.shouldBlockExplode(explosion, blockGetter, blockPos, blockState, f);
		}

		@Override
		public Optional<Float> getBlockExplosionResistance(
			Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState
		) {
			return blockState.is(Blocks.NETHER_PORTAL) ? Optional.empty() : super.getBlockExplosionResistance(explosion, blockGetter, blockPos, blockState, fluidState);
		}
	};
	@Nullable
	private LivingEntity owner;
	private boolean usedPortal;
	private float explosionPower = 4.0F;

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
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(DATA_FUSE_ID, 80);
		builder.define(DATA_BLOCK_STATE_ID, Blocks.TNT.defaultBlockState());
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
	protected double getDefaultGravity() {
		return 0.04;
	}

	@Override
	public void tick() {
		this.handlePortal();
		this.applyGravity();
		this.move(MoverType.SELF, this.getDeltaMovement());
		this.applyEffectsFromBlocks();
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
		this.level()
			.explode(
				this,
				Explosion.getDefaultDamageSource(this.level(), this),
				this.usedPortal ? USED_PORTAL_DAMAGE_CALCULATOR : null,
				this.getX(),
				this.getY(0.0625),
				this.getZ(),
				this.explosionPower,
				false,
				Level.ExplosionInteraction.TNT
			);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {
		compoundTag.putShort("fuse", (short)this.getFuse());
		compoundTag.put("block_state", NbtUtils.writeBlockState(this.getBlockState()));
		if (this.explosionPower != 4.0F) {
			compoundTag.putFloat("explosion_power", this.explosionPower);
		}
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compoundTag) {
		this.setFuse(compoundTag.getShort("fuse"));
		if (compoundTag.contains("block_state", 10)) {
			this.setBlockState(NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), compoundTag.getCompound("block_state")));
		}

		if (compoundTag.contains("explosion_power", 99)) {
			this.explosionPower = Mth.clamp(compoundTag.getFloat("explosion_power"), 0.0F, 128.0F);
		}
	}

	@Nullable
	public LivingEntity getOwner() {
		return this.owner;
	}

	@Override
	public void restoreFrom(Entity entity) {
		super.restoreFrom(entity);
		if (entity instanceof PrimedTnt primedTnt) {
			this.owner = primedTnt.owner;
		}
	}

	public void setFuse(int i) {
		this.entityData.set(DATA_FUSE_ID, i);
	}

	public int getFuse() {
		return this.entityData.get(DATA_FUSE_ID);
	}

	public void setBlockState(BlockState blockState) {
		this.entityData.set(DATA_BLOCK_STATE_ID, blockState);
	}

	public BlockState getBlockState() {
		return this.entityData.get(DATA_BLOCK_STATE_ID);
	}

	private void setUsedPortal(boolean bl) {
		this.usedPortal = bl;
	}

	@Nullable
	@Override
	public Entity changeDimension(DimensionTransition dimensionTransition) {
		Entity entity = super.changeDimension(dimensionTransition);
		if (entity instanceof PrimedTnt primedTnt) {
			primedTnt.setUsedPortal(true);
		}

		return entity;
	}
}
